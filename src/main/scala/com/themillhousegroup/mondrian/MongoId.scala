package com.themillhousegroup.mondrian

import org.apache.commons.lang3.StringUtils
import play.api.libs.json.Json


/** See https://docs.mongodb.com/manual/reference/method/ObjectId/ */
object MongoId {
  lazy val objectIdRegex = """^[a-f\d]{24}$""".r

  def withValidId(candidate:String):Option[String] = {
    val maybeId = Option(StringUtils.trimToNull(candidate))

    maybeId.flatMap { id =>
      objectIdRegex.findFirstIn(id)
    }
  }

  def isValid(candidate:String):Boolean = withValidId(candidate).nonEmpty

  def timestamp(candidate:String):Option[Long] = {
    withValidId(candidate).map { oid =>
      oid.substring(0, 7).toLong
    }
  }
}

/** Represents the particular format of the Mongo "primary key", the `$oid` */
case class MongoId(val `$oid`: String) {
  lazy val timestamp = MongoId.timestamp(`$oid`)

  lazy val isValid = MongoId.isValid(`$oid`)
}

/** Provides a Play JSON `Format` for working with MongoId instances */
class MongoJson {
  implicit val mongoIdFormat = Json.format[MongoId]
}

object MongoJson extends MongoJson