package com.themillhousegroup.mondrian.test

import org.specs2.mock.mockito.{MockitoStubs, MocksCreation}
import org.specs2.specification.Scope
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands._
import reactivemongo.api.{DefaultDB, MongoConnection, MongoConnectionOptions}

/** Presents workable mocked values for parts of the ReactiveApi interface */
trait MockedReactiveApi extends MocksCreation with MockitoStubs with MockedConnection {
  val mockDB:DefaultDB
  val mockReactiveApi = mock[ReactiveMongoApi]
  mockReactiveApi.db returns mockDB
  mockDB.connection returns mockConnection
}

/** Presents workable mocked values for parts of the ReactiveApi interface, contained in a
  * Specs2 Scope to prevent "leakage" of mocked behaviour */
trait ScopedMockedReactiveApi extends Scope with MocksCreation with MockitoStubs with MockedConnection {
  val mockDB = mock[DefaultDB]
  val mockReactiveApi = mock[ReactiveMongoApi]
  mockReactiveApi.db returns mockDB
  mockDB.connection returns mockConnection
}

trait MockedConnection extends MocksCreation with MockitoStubs {

  val mockConnection = mock[MongoConnection]
  val mockConnectionOptions = mock[MongoConnectionOptions]
  mockConnection.options returns mockConnectionOptions
  mockConnectionOptions.writeConcern returns WriteConcern.Default
}