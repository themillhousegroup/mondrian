package com.themillhousegroup.mondrian

import play.api.libs.json.Json

/** Represents the particular format of the Mongo "primary key", the `$oid` */
case class MongoId(val $oid: String)

/** Provides a Play JSON `Format` for working with MongoId instances */
object MongoJson {
  implicit val mongoIdFormat = Json.format[MongoId]
}
