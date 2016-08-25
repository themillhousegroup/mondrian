package com.themillhousegroup.mondrian

import play.api.Logger
import play.api.cache.CacheApi

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Mix this trait in with your MongoService to get simple caching via the Play Framework's CacheApi:
  * https://www.playframework.com/documentation/2.4.x/ScalaCache
  *
  */
trait MongoCaching[ME <: MongoEntity] {
  this:TypedMongoService[ME] =>

  val cache: CacheApi
  val logger:Logger

  type EntityMap = Map[String, ME]
  val idCacheName:String
  val cacheExpirationTime:Duration

  /**
    * Caches the result of performing "listAll" into an id -> entity Map,
    * and then uses that to lookup results.
    */
  def withIdCache[R](f: EntityMap => R): Future[R] = {
    withOneToOneCache[R, String](entity => entity.id, idCacheName)(f)
  }

  /**
    * If each ME object can be *uniquely* identified by a key of type K, store a Map
    * of (K -> ME) and use that for lookups.
    */
  def withOneToOneCache[R, K](keyFn: ME => K, cacheName:String)(f: Map[K, ME] => R): Future[R] = {
    withCache[R, K, ME](all => all.map(e => keyFn(e) -> e).toMap, cacheName)(f)
  }

  /**
    * Given a cached thing of type Map[K, V], returns a Future[R] by looking it up.
    *
    * Provide a method of putting all the MEs from listAll into a Map[K, V]
    *
    * Best used in a curried style. e.g.:
    *
    * def withPhoneNumberCache = withCache[User, PhoneNumber](_.phoneNumber, "allNumbers") _
    */
  def withCache[R, K, V](mappingFn: Seq[ME] => Map[K, V], cacheName:String)(f: Map[K, V] => R): Future[R] = {
    withCachePopulatedBy(listAll)(mappingFn, cacheName)(f)
  }

  /**
    * Given a cached thing of type Map[K, V], returns a Future[R] by looking it up.
    *
    * Provide a method of putting all the MEs returned from loadQuery() into a Map[K, V]
    *
    * Best used in a curried style. e.g.:
    *
    * def withPhoneNumberCache = withCache[User, PhoneNumber](_.phoneNumber, "allNumbers") _
    */
  def withCachePopulatedBy[R, K, V](loadQuery: => Future[Seq[ME]])(mappingFn: Seq[ME] => Map[K, V], cacheName:String)(f: Map[K, V] => R): Future[R] = {
    val maybeMap = cache.get[Map[K, V]](cacheName)
    maybeMap.fold {
      logger.trace(s"$cacheName cache miss")
      loadQuery.map { results =>
        val theMap = mappingFn(results)
        cache.set(cacheName, theMap, cacheExpirationTime)
        f(theMap)
      }
    } { theMap =>
      logger.trace(s"$cacheName cache hit")
      Future.successful(f(theMap))
    }
  }
}
