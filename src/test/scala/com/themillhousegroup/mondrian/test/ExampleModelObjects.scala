package com.themillhousegroup.mondrian.test

import play.api.libs.json.Json
import com.themillhousegroup.mondrian._

// These are the example model objects from the README.md example
// checking that it all actually works!

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
