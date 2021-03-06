package com.themillhousegroup.mondrian

import reactivemongo.api._
import scala.concurrent.Future
import play.api.libs.json.{ JsValue, JsObject, Json }
import play.api.libs.concurrent.Execution.Implicits._
import scala.language.existentials
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import play.modules.reactivemongo.ReactiveMongoApi
import play.api.Logger

abstract class MongoService(collectionName: String) {

  val reactiveMongoApi:ReactiveMongoApi

  private val logger = Logger(classOf[MongoService])

  implicit val defaultContext = play.api.libs.concurrent.Execution.defaultContext

  val readPreference = ReadPreference.nearest

  protected def theCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection](collectionName)

  protected val all = Json.obj()

  protected def findWhere(jsQuery: JsValue, jsProjection:Option[JsValue] = None) = {
    jsProjection.fold {
      theCollection.find(jsQuery.as[JsObject])
    } { jsProj =>
      theCollection.find(jsQuery.as[JsObject], jsProj.as[JsObject])
    }
  }

  protected def findAll = findWhere(all)

  protected def findAllWithProjection(jsProjection:JsValue) = findWhere(all, Some(jsProjection))

  def countWhere(jsQuery: JsValue):Future[Int] = theCollection.count(Some(jsQuery.as[JsObject]))

  def countAll:Future[Int] = theCollection.count(None)

  protected def idOf(id:String):JsValue = Json.obj("$oid" -> id)
  protected def idSelector(id: String): JsObject = Json.obj("_id" -> idOf(id))

  def deleteWhere(jsQuery: JsValue): Future[Boolean] = {
    theCollection.remove(jsQuery.as[JsObject]).map(_.ok)
  }

  /**
    * @param jsQuery the query that selects the objects to be deleted
    * @return the overall success of the command, and the number that were actually deleted
    */
  def deleteWhereAndCount(jsQuery: JsValue): Future[(Boolean, Int)] = {
    theCollection.remove(jsQuery.as[JsObject]).map(wr => wr.ok -> wr.n)
  }

  /**
    * @param id the identifier (in simple String form) of the object to be deleted
    * @return true iff the operation succeeded AND exactly one object was deleted
    */
  def deleteById(id: String): Future[Boolean] = deleteWhereAndCount(idSelector(id)).map { case (ok, n) =>
    val deletedExactlyOne = ok && n == 1
    logger.trace(s"Deletion status: OK: $ok Number actually deleted: $n; So returning $deletedExactlyOne")
    deletedExactlyOne
  }
}
