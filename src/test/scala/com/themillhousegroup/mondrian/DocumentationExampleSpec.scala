package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import com.themillhousegroup.mondrian.test.Vehicle
import com.themillhousegroup.mondrian.test.VehicleJson

class VehicleService extends TypedMongoService[Vehicle]("vehicles")(VehicleJson.converter)

class DocumentationExampleSpec extends Specification with MongoMocks with Mockito {
  val mockReactiveApi = mock[ReactiveMongoApi]
  val mockCollection = mockedCollection("vehicles")(mockDB)
  mockReactiveApi.db returns mockDB

  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

	val service = new VehicleService() {
    override lazy val reactiveMongoApi = mockReactiveApi
	}

  "Documentation Example" should {
    "work" in {
      Await.result(
        service.findById("abc123"),
        Duration(2, "seconds")) must beNone

    }

  }
}

