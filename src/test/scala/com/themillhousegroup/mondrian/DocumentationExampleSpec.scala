package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks

// This is the example from the README.md - checking that it works!

case class Manufacturer(val name:String, val country:String)

case class Vehicle(
  	val _id: Option[MongoId],
  	val name: String,
  	val manufacturer:Manufacturer,
  	val yearFirstOffered:Int,
  	val yearLastOffered:Option[Int]) extends MongoEntity

object ManufacturerJson {
  implicit val manufacturerFormat = Json.format[Manufacturer]
}

object VehicleJson extends MongoJson {
	import ManufacturerJson.manufacturerFormat
  implicit val converter = Json.format[Vehicle]
}

class VehicleService extends TypedMongoService[Vehicle]("vehicles")(VehicleJson.converter)

class DocumentationExampleSpec extends Specification with MongoMocks with Mockito {
  val mockReactiveApi = mock[ReactiveMongoApi]
  val mockCollection = mockedCollection("vehicles")
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

