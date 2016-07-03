package com.themillhousegroup.mondrian

import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import org.specs2.mock.Mockito
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import com.themillhousegroup.mondrian.test.Waiting
import reactivemongo.api.{DefaultDB, MongoConnectionOptions, MongoConnection}
import reactivemongo.api.commands.WriteConcern
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.specification.Scope

case class TestMongoEntity(_id: Option[MongoId], name:String) extends MongoEntity


object TestMongoEntityJson extends MongoJson {
  implicit val converter = Json.format[TestMongoEntity]
}

class TypedMongoServiceSpec extends Specification with MongoMocks with Mockito with Waiting {

  val mockReactiveApi = mock[ReactiveMongoApi]
  val mockCollection = mockedCollection("testcollection")(mockDB)
  mockReactiveApi.db returns mockDB

  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

  val simpleObject = TestMongoEntity(None, "foo")

  val testMongoService = new TypedMongoService[TestMongoEntity]("testcollection")(TestMongoEntityJson.converter) {
    override lazy val reactiveMongoApi = mockReactiveApi
  }

  "TypedMongoService" should {
    "use an implicit Format to do internal JSON conversion" in {
      await(testMongoService.findById("abc123")) must beNone
    }

    "return a None from a findOne on an empty collection" in {
      await(testMongoService.findOne(simpleObject)) must beNone

    }

    "return a Nil from a listAll on an empty collection" in {
      await(testMongoService.listAll) must beEmpty

    }

    "return a Nil from a enumerateWhere on an empty collection" in {
      val e = testMongoService.enumerateWhere(Json.obj())
      e must not beNull
    }
  }
}

class TypedMongoServiceImplicitFormatSpec extends Specification with MongoMocks with Mockito with Waiting {
  val mockReactiveApi = mock[ReactiveMongoApi]
  val mockCollection = mockedCollection("testcollection")(mockDB)
  mockReactiveApi.db returns mockDB


  val simpleObject = TestMongoEntity(None, "foo")

  givenMongoCollectionFindAnyReturns[List](mockCollection, Nil)

  import TestMongoEntityJson._

  val testMongoService = new TypedMongoService[TestMongoEntity]("testcollection") {
    override lazy val reactiveMongoApi = mockReactiveApi
  }

  "TypedMongoService using an implicit Format" should {
    "use an implicit Format to do internal JSON conversion" in {
      await(testMongoService.findById("abc123")) must beNone

    }

    "return a None from a findOne on an empty collection" in {
      await(testMongoService.findOne(simpleObject)) must beNone

    }

    "return a Nil from a listAll on an empty collection" in {
      await(testMongoService.listAll) must beEmpty

    }
  }
}

class TypedMongoServiceWriteConcernOverrideSpec extends Specification with MongoMocks with Mockito with Waiting {


  import TestMongoEntityJson._

  class WriteConcernScope(writeConcern:WriteConcern) extends Scope {
    val mockDB = mock[DefaultDB]
    val mockReactiveApi = mock[ReactiveMongoApi]
    val mockCollection = mockedCollection("testcollection")(mockDB)
    mockReactiveApi.db returns mockDB


    val mockConnection = mock[MongoConnection]
    val mockConnectionOptions = mock[MongoConnectionOptions]
    mockDB.connection returns mockConnection
    mockConnection.options returns mockConnectionOptions

    mockConnectionOptions.writeConcern returns writeConcern

    val unsavedObject = TestMongoEntity(None, "foo")
    val savedObject = TestMongoEntity(Some(MongoId("123")), "foo")


    givenAnyMongoInsertIsOK(mockCollection, true)
    givenAnyMongoUpdateIsOK(mockCollection, true)

    val testMongoService = new TypedMongoService[TestMongoEntity]("testcollection") {
      override lazy val reactiveMongoApi = mockReactiveApi
    }

  }


  import play.api.libs.json._

  implicit val dummyOWrites = new OWrites[JsObject] {
    def writes(o: JsObject): JsObject = JsObject(Seq("key" -> JsString("dontcare")))
  }


  "TypedMongoService using the default WriteConcern" should {

    "Make insert requests using WriteConcern.Default" in new WriteConcernScope(WriteConcern.Default){
      await(testMongoService.save(unsavedObject))


      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).insert(any[JsObject], writeConcernCaptor)(anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Default)
    }

    "Make update requests using WriteConcern.Default" in new WriteConcernScope(WriteConcern.Default){
      await(testMongoService.save(savedObject))


      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).update(any[JsObject], any[JsObject], writeConcernCaptor, anyBoolean, anyBoolean)(anyPackWrites, anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Default)
    }
  }

  "TypedMongoService using an overridden WriteConcern" should {
    "Make insert requests using that level WriteConcern" in new WriteConcernScope(WriteConcern.Journaled) {
      await(testMongoService.save(unsavedObject))

      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).insert(any[JsObject], writeConcernCaptor)(anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Journaled)
    }

    "Make update requests using that level WriteConcern" in new WriteConcernScope(WriteConcern.Journaled){
      await(testMongoService.save(savedObject))


      val writeConcernCaptor = capture[WriteConcern]

      there was one(mockCollection).update(any[JsObject], any[JsObject], writeConcernCaptor, anyBoolean, anyBoolean)(anyPackWrites, anyPackWrites, anyEC)

      writeConcernCaptor.value must beEqualTo(WriteConcern.Journaled)
    }

  }
}
