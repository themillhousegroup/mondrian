package com.themillhousegroup.mondrian

trait MongoEntity {
  val _id: Option[MongoId]
  val id: String = _id.fold("NOT_SAVED_YET")(_.$oid)
}
