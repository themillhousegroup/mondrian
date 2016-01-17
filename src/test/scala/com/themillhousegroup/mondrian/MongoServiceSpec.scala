package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks

case class TestMongoEntity(_id: Option[MongoId], name:String) extends MongoEntity


object TestMongoEntityJson extends MongoJson {
  val converter = Json.format[TestMongoEntity]
}

class MongoServiceSpec extends Specification with MongoMocks with Mockito {

  val mockReactiveApi = mock[ReactiveMongoApi]
  val mockCollection = mockedCollection("testcollection")
  mockReactiveApi.db returns mockDB

  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

  val testMongoService = new TypedMongoService[TestMongoEntity]("testcollection") {
    override lazy val reactiveMongoApi = mockReactiveApi
    val fmt = TestMongoEntityJson.converter
  }

  "TypedMongoService" should {
    "use an implicit Format to do internal JSON conversion" in {
      Await.result(
        testMongoService.findById("abc123"),
        Duration(2, "seconds")) must beNone

    }

    "return a None from a findOne on an empty collection" in {
      Await.result(
        testMongoService.findOne(TestMongoEntity(None, "foo")),
        Duration(2, "seconds")) must beNone

    }
  }
}
