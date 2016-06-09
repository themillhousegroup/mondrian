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
      java.lang.Long.parseLong(oid.substring(0, 8), 16)
    }
  }
}

/** Represents the particular format of the Mongo "primary key", the `$oid` */
case class MongoId(val `$oid`: String) extends Ordered[MongoId] {
  lazy val timestamp = MongoId.timestamp(`$oid`)

  lazy val isValid = MongoId.isValid(`$oid`)

  def compare(that:MongoId):Int = {
    val o = for {
      thisTs <- this.timestamp
      thatTs <- that.timestamp
    } yield {
      (thisTs - thatTs).toInt
    }

    o.getOrElse(0)
  }
}

/** Provides a Play JSON `Format` for working with MongoId instances */
class MongoJson {
  implicit val mongoIdFormat = Json.format[MongoId]
}

object MongoJson extends MongoJson
