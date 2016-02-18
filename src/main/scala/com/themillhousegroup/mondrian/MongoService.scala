package com.themillhousegroup.mondrian

import reactivemongo.api._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.json.{ JsValue, JsObject, Json, Format }
import play.api.libs.concurrent.Execution.Implicits._
import scala.language.existentials
import play.api.libs.iteratee.Enumerator

abstract class TypedMongoService[T <: MongoEntity](collectionName: String)(implicit val fmt:Format[T]) {
  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  implicit val defaultContext = play.api.libs.concurrent.Execution.defaultContext

  val readPreference = ReadPreference.nearest

//  implicit val fmt: Format[T] 


  protected def theCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection](collectionName)

  protected val all = Json.obj()

  protected def findWhere(jsQuery: JsValue) = theCollection.find(jsQuery.as[JsObject])

  protected def findAll = findWhere(all)

  def cursorWhere(jsQuery: JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None): Cursor[T] = {
    val qo = QueryOpts(skipN = startFrom.getOrElse(0), batchSizeN = size.getOrElse(0))
    sortWith.fold {
      findWhere(jsQuery).options(qo).cursor[T](readPreference) // (fmt, defaultContext, CursorProducer.defaultCursorProducer[T])
    } {
      findWhere(jsQuery).options(qo).sort(_).cursor[T](readPreference) //(fmt, defaultContext, CursorProducer.defaultCursorProducer[T])
    }
  }

  def listWhere(jsQuery: JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None): Future[List[T]] = {
    cursorWhere(jsQuery, size, startFrom, sortWith).collect[List]()
  }

  def listAll: Future[List[T]] = listAll(None, None)
  def listAll(size: Option[Int] = None, startFrom: Option[Int] = None): Future[List[T]] = listWhere(all, size, startFrom)

  def enumerateWhere(jsQuery: JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None): Enumerator[T] = {
    cursorWhere(jsQuery, size, startFrom, sortWith).enumerate()
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

  private def idSelector(id: String): JsObject = Json.obj("_id" -> Json.obj("$oid" -> id))

  def save(obj: T): Future[Boolean] = {
    val json = Json.toJson(obj)(fmt).as[JsObject]

    val op = obj._id.fold(theCollection.insert(json)) { id =>
      val selector = idSelector(id.$oid)
      theCollection.update(selector, json)
    }

    op.map { err =>
      err.ok
    }
  }

  /** Returns a Some(T) if successful where the _id, if it was a None, is now a Some(id) */
  def saveAndPopulate(obj: T): Future[Option[T]] = {
    save(obj).flatMap { ok =>
      if (ok) {
        val json = Json.toJson(obj)(fmt)
        findOne(json)
      } else {
        Future.successful(None)
      }
    }
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

  def deleteById(id: String): Future[Boolean] = {
    theCollection.remove(idSelector(id)).map(_.ok)
  }
}
