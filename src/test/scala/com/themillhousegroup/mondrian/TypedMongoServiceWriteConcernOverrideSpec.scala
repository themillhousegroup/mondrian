package com.themillhousegroup.mondrian

import com.themillhousegroup.mondrian.test.{MockedConnection, MockedReactiveApi, ScopedMockedReactiveApi, Waiting}
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands._

class TypedMongoServiceWriteConcernOverrideSpec extends Specification with MongoMocks with Mockito with Waiting {

  import TestMongoEntityJson._
  val unsavedObject = TestMongoEntity(None, "foo")
  val savedObject = TestMongoEntity(Some(MongoId("123")), "foo")

  class WriteConcernScope(maybeOverriddenWriteConcern: Option[WriteConcern]) extends ScopedMockedReactiveApi {
    val self = this
    val mockCollection = mockedCollection("testcollection")(mockDB)

    givenAnyMongoInsertIsOK(mockCollection, true)
    givenAnyMongoUpdateIsOK(mockCollection, true)



    val testMongoService = maybeOverriddenWriteConcern.fold {
      new TypedMongoService[TestMongoEntity]("testcollection"){
        val reactiveMongoApi = self.mockReactiveApi
      }
    } { writeConcern =>
      new TypedMongoService[TestMongoEntity]("testcollection") {
        val reactiveMongoApi = self.mockReactiveApi
        override lazy val defaultWriteConcern = writeConcern
      }
    }
  }


  import play.api.libs.json._

  implicit val dummyOWrites = new OWrites[JsObject] {
    def writes(o: JsObject): JsObject = JsObject(Seq("key" -> JsString("dontcare")))
  }


  "TypedMongoService using the default WriteConcern" should {

    "Make insert requests using WriteConcern.Default" in new WriteConcernScope(None) {
      await(testMongoService.save(unsavedObject))


      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).insert(any[JsObject], writeConcernCaptor)(anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Default)
    }

    "Make update requests using WriteConcern.Default" in new WriteConcernScope(None) {
      await(testMongoService.save(savedObject))


      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).update(any[JsObject], any[JsObject], writeConcernCaptor, anyBoolean, anyBoolean)(anyPackWrites, anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Default)
    }
  }

  "TypedMongoService using a service-level-overridden WriteConcern" should {
    "Make insert requests using that level WriteConcern" in new WriteConcernScope(Some(WriteConcern.Journaled)) {
      await(testMongoService.save(unsavedObject))

      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).insert(any[JsObject], writeConcernCaptor)(anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Journaled)
    }

    "Make update requests using that level WriteConcern" in new WriteConcernScope(Some(WriteConcern.Journaled)) {
      await(testMongoService.save(savedObject))


      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).update(any[JsObject], any[JsObject], writeConcernCaptor, anyBoolean, anyBoolean)(anyPackWrites, anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Journaled)
    }
  }
}