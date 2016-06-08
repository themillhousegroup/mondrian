package com.themillhousegroup.mondrian

import java.util.Date

import org.specs2.mutable.Specification

class MongoIdSpec extends Specification {
  "MongoId Object" should {

    "be able to determine an invalid Mongo Object ID (too short)" in {
      MongoId.isValid("123") must beFalse
    }

    "be able to determine an invalid Mongo Object ID (too long)" in {
      MongoId.isValid("123123123123123123123123123") must beFalse
    }
    "be able to determine an invalid Mongo Object ID (illegal lowercase chars)" in {
      MongoId.isValid("gggggggggggggggggggggggg") must beFalse
    }

    "be able to determine an invalid Mongo Object ID (uppercase chars)" in {
      MongoId.isValid("123ABC123123123123123123") must beFalse
    }
    "be able to determine an invalid Mongo Object ID (dashes)" in {
      MongoId.isValid("123-123-123-123123123123") must beFalse
    }
    "be able to determine an invalid Mongo Object ID (spaces)" in {
      MongoId.isValid("123 123 123-123123123123") must beFalse
    }
    "be able to determine a valid Mongo Object ID (trimmed)" in {
      MongoId.isValid("  123abc123abc123def123def  ") must beTrue
    }
    "be able to determine a valid Mongo Object ID (standard)" in {
      MongoId.isValid("123abc123abc123def123def") must beTrue
    }

    "return a None if a timestamp cannot be extracted" in {
      MongoId.timestamp("123") must beNone
    }

    "return a Some if a timestamp can be extracted" in {
      MongoId.timestamp("570eee4ae137520167c1ed9c") must beSome[Long].like { case l =>
        l must beEqualTo(91287268)
      }
    }
  }
}
