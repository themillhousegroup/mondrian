package com.themillhousegroup.mondrian

import play.api.libs.json.Json

case class MongoId(val $oid: String)

object MongoJson {
  implicit val mongoIdFormat = Json.format[MongoId]
}
