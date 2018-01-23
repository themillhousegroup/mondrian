package com.themillhousegroup.mondrian

import javax.inject.Inject

import play.api.Logger
import play.api.libs.concurrent.Akka
import reactivemongo.api._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._

import scala.concurrent.{Future, Promise}
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import scala.language.existentials
import play.api.libs.iteratee.Enumerator

import scala.concurrent.duration.FiniteDuration
import akka.actor._

abstract class TypedMongoService[T <: MongoEntity] @Inject() (actorSystem: ActorSystem) (collectionName: String)(implicit val fmt:Format[T]) extends MongoService(collectionName) {

  private val logger = Logger(classOf[TypedMongoService[T]])
  /**
    *
    * `@Inject()` this into your instance in the normal Play DI way
    */
  val reactiveMongoApi:ReactiveMongoApi

  /** The level of write concern to use for this collection; if not overridden, this will be the
    * ReactiveMongo connection-wide level as defined in MongoConnectionOptions - which can be globally set
    * in your application.conf using the key `mongodb.options.writeConcern` with possible values:
    * unacknowledged / acknowledged / journaled / default
    */
  lazy val defaultWriteConcern = reactiveMongoApi.db.connection.options.writeConcern

  def cursorWhere(jsQuery: JsValue,
                  size: Option[Int] = None,
                  startFrom: Option[Int] = None,
                  sortWith: Option[JsObject] = None,
                  jsProjection: Option[JsValue] = None): Cursor[T] = {
    val qo = QueryOpts(skipN = startFrom.getOrElse(0), batchSizeN = size.getOrElse(0))
    sortWith.fold {
      findWhere(jsQuery).options(qo).cursor[T](readPreference) // (fmt, defaultContext, CursorProducer.defaultCursorProducer[T])
    } {
      findWhere(jsQuery).options(qo).sort(_).cursor[T](readPreference) //(fmt, defaultContext, CursorProducer.defaultCursorProducer[T])
    }
  }

  def listWhere(jsQuery: JsValue,
                size: Option[Int] = None,
                startFrom: Option[Int] = None,
                sortWith: Option[JsObject] = None,
                jsProjection: Option[JsValue] = None): Future[Seq[T]] = {
    cursorWhere(jsQuery, size, startFrom, sortWith, jsProjection).collect[Seq]()
  }

  def listAll: Future[Seq[T]] = listAll(None, None, None)

  def listAll(size: Option[Int] = None,
              startFrom: Option[Int] = None,
              jsProjection: Option[JsValue] = None): Future[Seq[T]] = listWhere(all, size, startFrom, None, jsProjection)

  def enumerateWhere(jsQuery: JsValue,
                     size: Option[Int] = None,
                     startFrom: Option[Int] = None,
                     sortWith: Option[JsObject] = None,
                     jsProjection: Option[JsValue] = None): Enumerator[T] = {
    cursorWhere(jsQuery, size, startFrom, sortWith, jsProjection).enumerate()
  }

  def findOne(jsQuery: JsValue): Future[Option[T]] = {
    findWhere(jsQuery).one[T](fmt, defaultContext)
  }

  def findOne(example: T): Future[Option[T]] = {
    findOne(Json.toJson(example)(fmt))
  }

  def findById(id: String): Future[Option[T]] = {
    findOne(idSelector(id))
  }

  protected def inIdsSelector(ids:Iterable[String]):JsValue = {
    import play.api.libs.json.Json._
    Json.obj(
      "_id" ->
        Json.obj(
          "$in" ->
            JsArray(ids.toSeq.map(idOf))
        )
    )
  }

  /**
    * Attempt to find as many matches as possible for the supplied IDs.
    * The length of the resultant Iterable may be shorter than the supplied
    * Iterable if we fail to find some objects. Also, the order of the
    * returned Iterable is not guaranteed to match the IDs supplied.
    * If you need a more strictly-correct finder (at the expense of speed)
    * try findByIdInOrder(ids)
    */
  def findById(ids:Iterable[String]):Future[Iterable[T]] = {
    listWhere(inIdsSelector(ids))
  }

  /**
    * Attempt to find each object denoted by its ID.
    * The positions of objects in the returned Iterable will *exactly
    * match* the positions of IDs in the incoming Iterable,
    * being a None if no object was found.
    */
  def findByIdInOrder(ids:Iterable[String]):Future[Iterable[Option[T]]] = {
    findById(ids).map { results =>
      val resultsIdMap = results.map(r => r.id -> r).toMap

      ids.map { id =>
        resultsIdMap.get(id)
      }
    }
  }

  /**
   * Attempt to persist the given object.
   * If the _id field is None, an `insert` operation will be used, and the database will generate the ID.
   * If you need to know this ID, use the `saveAndPopulate` method, which will return a copy of your `obj`
   * with the `_id` field populated.
   * If the _id is a Some, then an `update` will be performed. The `upsert` flag will be passed
   * to the database, so that if there is no existing object, it will be inserted instead of updated.
   *
   * @param obj the T to be inserted/updated
   * @return a Future containing a Boolean representing the success of the save operation
   */
  def save(obj: T): Future[Boolean] = {
    val json = Json.toJson(obj)(fmt).as[JsObject]

    val op = obj._id.fold {
      theCollection.insert(json, defaultWriteConcern)
    } { id =>
      val selector = idSelector(id.$oid)
      theCollection.update(selector, json, defaultWriteConcern, true)
    }

    op.map { err =>
      val ok = err.ok

      if (!ok) {
        logger.error(s"Save of $obj NOT OK: code ${err.code}; errors: ${err.writeErrors.map(we => we.errmsg)}")
      }
      ok
    }
  }

  /** Inserts or Updates each 'T' in the provided collection */
  def save(objs: Iterable[T]): Future[Iterable[Boolean]] = {
    // TODO: should use bulkInsert and the collection selector of update to do this more efficiently
    Future.sequence(objs.map(save))
  }

  /** Returns a Some(T) if successful where the _id, if it was a None, is now a Some(id) */
  def saveAndPopulate(obj: T): Future[Option[T]] = {
    save(obj).flatMap { ok =>
      if (ok) {
        logger.debug(s"Save of $obj was OK; retrieving the object to determine its ID")
        val json = Json.toJson(obj)(fmt)
		    listWhere(json).flatMap { results =>
          logger.trace(s"Found ${results.size} candidate object(s) for $obj")
          if (results.size < 2) {
            findSingleObjectWithRetry(results, json)
          } else {
            findMostRecentlyInsertedObject(results)
          }
        }
      } else {
        logger.warn(s"Save of $obj was NOT OK.")
        Future.successful(None)
      }
    }
  }

  // If we've found no objects, try again
  // FIXME: Allow this to be configured.
  // We should allow injection of a Configuration object
  // to control this

  val retryDelayMillis = 100
  lazy val delayDuration = FiniteDuration(retryDelayMillis, scala.concurrent.duration.MILLISECONDS)

  def findSingleObjectWithRetry(candidates: Seq[T], exampleJson:JsValue):Future[Option[T]] = {
    candidates.headOption.fold {

      val promise = Promise[Option[T]]

      logger.debug(s"We didn't find any persisted objects like $exampleJson - will try again in $retryDelayMillis ms")
      actorSystem.scheduler.scheduleOnce(delayDuration) {
        val fRetriedResult = findOne(exampleJson)

        fRetriedResult.map { retriedResult =>
          logger.debug(s"Result of retried query: $retriedResult")
          retriedResult
        }

        promise.completeWith(fRetriedResult)
      }

      promise.future
    } (first => Future.successful(Some(first)))
  }


  // There are multiple objects that look like the one
  // we have saved and so we need to "try harder" to find the new one..
  private def findMostRecentlyInsertedObject(candidates:Seq[T]):Future[Option[T]] = Future.successful {
    logger.debug(s"Sorting $candidates to find the most recent")
    candidates.sortBy { candidate =>
      candidate._id.getOrElse(MongoId.dummyMongoId)
    }.lastOption
  }

  /** Inserts or Updates each 'T' in the provided collection,
    * returning a Some(T) if successful where the _id, if it was a None, is now a Some(id) */
  def saveAndPopulate(objs: Iterable[T]): Future[Iterable[Option[T]]] = {
    // TODO: should use bulkInsert and the collection selector of update to do this more efficiently
    Future.sequence(objs.map(saveAndPopulate))
  }

  /**
   * If there is no similar object in the collection,
   * save it and return a Some(object).
   * Otherwise, return the existing object from the collection
   */
  def saveIfNew(obj: T): Future[Option[T]] = {
    findOne(obj).flatMap { maybeMatch =>
      maybeMatch.fold(saveAndPopulate(obj)) { found =>
        Future.successful(Some(found))
      }
    }
  }

  /**
   * Save the object, ensuring that after the save is done, only
   * the newly-saved object satisfies the given condition - i.e.
   * other matches of the condition will be deleted
   */
  def saveEnsuring(obj:T, condition:JsValue):Future[Boolean] = {
    Future.failed(new NotImplementedError("This method isn't ready yet"))
  }
}
