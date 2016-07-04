package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import com.themillhousegroup.mondrian.test.Waiting

class MongoServiceSpec extends Specification with MongoMocks with Mockito with Waiting {

  val mockReactiveApi = mock[ReactiveMongoApi]
  val mockCollection = mockedCollection("testcollection")(mockDB)
  mockReactiveApi.db returns mockDB
  val testMongoService = new MongoService("testcollection")(mockReactiveApi) {}

  "MongoService" should {

    "be able to delete an object by an id in the _id($oid = xxx) scheme" in {
      givenMongoRemoveIsOK(mockCollection, Json.obj("_id" -> Json.obj("$oid" -> "f00")))

      await(testMongoService.deleteById("f00")) must beTrue
    }

//    "be able to count all the items in the collection" in {
//
//      await(testMongoService.countAll) must beEqualTo(3)
//    }
  }
}
