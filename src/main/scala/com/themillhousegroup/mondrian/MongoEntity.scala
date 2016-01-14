package com.themillhousegroup.mondrian

/** Domain objects to be persisted in a MongoDB database should extend
  * this trait, which defines the `_id` field as required by Mongo
  * and provides a convenient `id` accessor to read it as a String
  */
trait MongoEntity {
  val _id: Option[MongoId]
  val id: String = _id.fold("NOT_SAVED_YET")(_.$oid)
}
