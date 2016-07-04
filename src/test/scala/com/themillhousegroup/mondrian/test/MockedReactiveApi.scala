package com.themillhousegroup.mondrian.test

import com.themillhousegroup.reactivemongo.mocks.MongoMocks
import org.specs2.mock.Mockito
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands._
import reactivemongo.api.{MongoConnection, MongoConnectionOptions}

/** Presents workable mocked values for parts of the ReactiveApi interface */
trait MockedReactiveApi extends Mockito with MongoMocks {

  this: org.specs2.mutable.Specification =>

  val mockReactiveApi = mock[ReactiveMongoApi]
  mockReactiveApi.db returns mockDB

  val mockConnection = mock[MongoConnection]
  val mockConnectionOptions = mock[MongoConnectionOptions]
  mockDB.connection returns mockConnection
  mockConnection.options returns mockConnectionOptions

  mockConnectionOptions.writeConcern returns WriteConcern.Default
}
