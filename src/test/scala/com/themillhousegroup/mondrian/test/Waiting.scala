package com.themillhousegroup.mondrian.test

import scala.concurrent.{Awaitable, Await}
import scala.concurrent.duration.Duration

trait Waiting {

  val defaultWait = Duration(2, "seconds")


  def await[T](a:Awaitable[T]) = Await.result(a, defaultWait)
}
