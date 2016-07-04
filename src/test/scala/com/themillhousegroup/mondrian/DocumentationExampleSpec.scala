package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import com.themillhousegroup.mondrian.test.{MockedReactiveApi, Vehicle, VehicleJson}

class VehicleService(reactiveMongoApi:ReactiveMongoApi) extends TypedMongoService[Vehicle]("vehicles")(reactiveMongoApi)(VehicleJson.converter)

class DocumentationExampleSpec extends Specification with MongoMocks with Mockito with MockedReactiveApi {
  val mockCollection = mockedCollection("vehicles")(mockDB)

  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

	val service = new VehicleService(mockReactiveApi)

  "Documentation Example" should {
    "work" in {
      Await.result(
        service.findById("abc123"),
        Duration(2, "seconds")) must beNone

    }

  }
}

