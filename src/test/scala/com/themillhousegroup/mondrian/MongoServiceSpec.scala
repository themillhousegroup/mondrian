package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import com.themillhousegroup.mondrian.test.Waiting
import org.specs2.specification.Scope

class MongoServiceSpec extends Specification with MongoMocks with Mockito with Waiting {

  class RemovalScope extends MongoMockScope {
    val rma = reactiveMongoApi
    val mockCollection = mockedCollection("testcollection")(scopedMockDB)
    val testMongoService = new MongoService("testcollection"){
      val reactiveMongoApi = rma
    }
  }

  "MongoService" should {

    "be able to delete an object by an id in the _id($oid = xxx) scheme" in new RemovalScope {
      givenMongoRemoveIsOK(mockCollection, Json.obj("_id" -> Json.obj("$oid" -> "f00")))

      await(testMongoService.deleteById("f00")) must beTrue
    }

    "only return true when deleting an object by an id iff it worked and one object was removed; -ve (count)" in new RemovalScope {
      givenMongoRemoveIsOKAndAffectsNDocuments(mockCollection, Json.obj("_id" -> Json.obj("$oid" -> "b00")), true, 0)

      await(testMongoService.deleteById("b00")) must beFalse
    }

    "only return true when deleting an object by an id iff it worked and one object was removed; -ve (success)" in new RemovalScope {
      givenMongoRemoveIsOKAndAffectsNDocuments(mockCollection, Json.obj("_id" -> Json.obj("$oid" -> "b00")), false, 1)

      await(testMongoService.deleteById("b00")) must beFalse
    }

    "only return true when deleting an object by an id iff it worked and one object was removed; +ve" in new RemovalScope {
      givenMongoRemoveIsOKAndAffectsNDocuments(mockCollection, Json.obj("_id" -> Json.obj("$oid" -> "b00")), true, 1)

      await(testMongoService.deleteById("b00")) must beTrue
    }

    "be able to delete by query and count number of affected objects" in new RemovalScope {

      val q = Json.obj("foo" -> "bar")

      givenMongoRemoveIsOKAndAffectsNDocuments(mockCollection, q, true, 3)

      await(testMongoService.deleteWhereAndCount(q)) must beEqualTo(true, 3)
    }
  }
}
