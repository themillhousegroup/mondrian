package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import com.themillhousegroup.mondrian.test.{MockedReactiveApi, Waiting}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoConnectionOptions}
import reactivemongo.api.commands.WriteConcern

import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.specification.Scope

case class TestMongoEntity(_id: Option[MongoId], name:String) extends MongoEntity


object TestMongoEntityJson extends MongoJson {
  implicit val converter = Json.format[TestMongoEntity]
}

class TypedMongoServiceSpec extends Specification with MongoMocks with Mockito with Waiting with MockedReactiveApi {


  val mockCollection = mockedCollection("testcollection")(mockDB)


  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

  val simpleObject = TestMongoEntity(None, "foo")

  val testMongoService = new TypedMongoService[TestMongoEntity]("testcollection")(TestMongoEntityJson.converter) {
    val reactiveMongoApi = mockReactiveApi
  }

  "TypedMongoService" should {
    "use an implicit Format to do internal JSON conversion" in {
      await(testMongoService.findById("abc123")) must beNone
    }

    "return a None from a findOne on an empty collection" in {
      await(testMongoService.findOne(simpleObject)) must beNone

    }

    "return a Nil from a listAll on an empty collection" in {
      await(testMongoService.listAll) must beEmpty

    }

    "return a Nil from a enumerateWhere on an empty collection" in {
      val e = testMongoService.enumerateWhere(Json.obj())
      e must not beNull
    }
  }
}

class TypedMongoServiceImplicitFormatSpec extends Specification with MongoMocks with Mockito with Waiting with MockedReactiveApi {
  val mockCollection = mockedCollection("testcollection")(mockDB)

  val simpleObject = TestMongoEntity(None, "foo")

  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

  import TestMongoEntityJson._

  val testMongoService = new TypedMongoService[TestMongoEntity]("testcollection")(TestMongoEntityJson.converter) {
    val reactiveMongoApi = mockReactiveApi
  }

  "TypedMongoService using an implicit Format" should {
    "use an implicit Format to do internal JSON conversion" in {
      await(testMongoService.findById("abc123")) must beNone

    }

    "return a None from a findOne on an empty collection" in {
      await(testMongoService.findOne(simpleObject)) must beNone

    }

    "return a Nil from a listAll on an empty collection" in {
      await(testMongoService.listAll) must beEmpty

    }
  }
}