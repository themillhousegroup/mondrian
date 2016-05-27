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

abstract class MongoService(collectionName: String) {
  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]

  implicit val defaultContext = play.api.libs.concurrent.Execution.defaultContext

  val readPreference = ReadPreference.nearest

  protected def theCollection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection](collectionName)

  protected val all = Json.obj()

  protected def findWhere(jsQuery: JsValue) = theCollection.find(jsQuery.as[JsObject])

  protected def findAll = findWhere(all)

  def countWhere(jsQuery: JsValue):Future[Int] = theCollection.count(Some(jsQuery.as[JsObject]))

  def countAll:Future[Int] = theCollection.count(None)

  private def idSelector(id: String): JsObject = Json.obj("_id" -> Json.obj("$oid" -> id))

  def deleteWhere(jsQuery: JsValue): Future[Boolean] = {
    theCollection.remove(jsQuery.as[JsObject]).map(_.ok)
  }

  def deleteById(id: String): Future[Boolean] = deleteWhere(idSelector(id))
}
