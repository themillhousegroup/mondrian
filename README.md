mondrian
============================

An extra layer supplying the classic CRUD operations for Play-ReactiveMongo objects.


## Installation

Bring in the library by adding the following to your Play project's ```build.sbt```.

##### For Scala 2.12 & Play 2.6.x (and Reactive Mongo 0.12.6):

```scala
libraryDependencies += "com.themillhousegroup" %% "mondrian" % "0.9.0"
```

## Usage

Once you have __mondrian__ added to your Play project, you can start using it like this:

#### Enable the **ReactiveMongoModule** in your `application.conf`
You may already have this if you've been using the vanilla Reactive Mongo Module:
```scala
play.modules.enabled += "play.modules.reactivemongo.ReactiveMongoModule" 
```

#### Define the `mongodb.uri` in your `application.conf`
If you're coming from Play-ReactiveMongo you'll already have something appropriate in your `application.conf`, e.g.:

```
mongodb.uri="mongodb://user:password@ds12345.mongolab.com:12345/mydb"
```


#### Define a model object that extends the `MongoEntity` trait

You may already have models defined. A `MongoEntity` simply includes an `_id: Option[MongoId]` field like this:

```scala
import com.themillhousegroup.mondrian.{MongoEntity, MongoId}

case class Vehicle(
  val _id: Option[MongoId],
  val name: String,
  val manufacturer:Manufacturer,
  val yearFirstOffered:Int,
  val yearLastOffered:Option[Int]) extends MongoEntity
```

#### Define an `implicit` Play-JSON `Format` for your new object 
Again, you may have already written a `Format` if you've been sending/receiving JSON over HTTP. 
You can extend `MongoJson` to get the implicit conversion for the `MongoId`; perhaps something like this:

```scala
import play.api.libs.json.Json

object VehicleJson extends MongoJson {
  import ManufacturerJson.manufacturerFormat
  
  implicit val vehicleFormat = Json.format[Vehicle]
}
```

#### Extend `TypedMongoService[T]` appropriately

This `Service` joins together your domain object, the name of the MongoDB collection that will hold it, and the `Format` to read/write it.

If you just want basic CRUD operations defined for your model object, you just need ONE line of code (plus the appropriate imports):

##### For Play 2.4.x:

```scala
import com.themillhousegroup.mondrian._
import models.VehicleJson._ 
 
class VehicleService extends TypedMongoService[Vehicle]("vehicles")
```

##### For Play 2.5.x (dependency injection is _required_):

```scala
import javax.inject.Inject
import com.themillhousegroup.mondrian._
import models.VehicleJson._ 
 
class VehicleService @Inject() (val reactiveMongoApi:ReactiveMongoApi) extends TypedMongoService[Vehicle]("vehicles")
```

Because of the `VehicleJson._` import, the compiler is able to find the implicit JSON `Format` it needs. You
can supply it explicitly if you prefer, as shown next.
Of course you can add extra methods that are useful; for example:


```scala
import javax.inject.Inject
import com.themillhousegroup.mondrian._
import models.VehicleJson 
 
class VehicleService @Inject() (val reactiveMongoApi:ReactiveMongoApi) extends TypedMongoService[Vehicle]("vehicles")(VehicleJson.vehicleFormat) {
  
  def findVehiclesFirstSoldIn(year:Int):Future[List[Vehicle]] = {
    listWhere(Json.obj("yearFirstOffered" -> year))
  }
}
```

Note how the query is built by using the [Play JSON library](https://www.playframework.com/documentation/2.5.x/ScalaJson) to create an object.


In the above example, the `listWhere(JsValue)` function from the superclass is being used, but there are many more that you can utilize, for example `findOne(JsValue)`, `cursorWhere(JsValue)` and `enumerateWhere(JsValue)`. See the next section for the details.

#### Inject your new `Service` into your `Controller` classes

For example:

```scala
class VehicleController @Inject()(val vehicleService:VehicleService) extends Controller {
  // ...
}

```

## Available Methods

#### Public Methods
As soon as you extend `TypedMongoService[T]`, your `Service` will have the following **public** methods available. All of them return `Future`s of some sort, in keeping with the Reactive philosophy.

##### Creation / Update

|       Method            |          Returns              |                                                        Description                                                         |
|-------------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------|
|`save(obj:T)`            | `Future[Boolean]`             | Persist `obj`. If its `_id` is `None`, will **insert**. Else will **update**                                               |
|`save(objs:Iterable[T])` | `Future[Iterable[Boolean]]`   | Persist each of the  `objs` as per `save`                                                                                  |
|`saveAndPopulate(obj:T)` | `Future[Option[T]]`           | Persist `obj` as per `save`, and return it with the `_id` field populated                                                  |
|`saveAndPopulate(objs:Iterable[T])` | `Future[Iterable[Option[T]]]` | Persist each of the  `objs` as per `saveAndPopulate`                                                                       |
|`saveIfNew(obj:T)`       | `Future[Option[T]]`           | If no similar object found, save `obj` as per `saveAndPopulate`. Otherwise, return the existing object from the collection |

##### Retrieval

|                                                   Method                                                                     |       Returns       |                            Description                             |
|------------------------------------------------------------------------------------------------------------------------------|---------------------|--------------------------------------------------------------------|
|`countAll`                                                                                                                    | `Future[Int]`       | Count the number of objects in the collection                      |
|`countWhere(jsQuery:JsValue)`                                                                                                 | `Future[Int]`       | Count the number of matches for `jsQuery`                          |
|`cursorWhere(jsQuery:JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None, jsProjection: Option[JsValue] = None)`    | `Cursor[T]`         | Returns a `reactivemongo.api.Cursor` of `jsQuery` matches          |
|`enumerateWhere(jsQuery:JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None)` | `Enumerator[T]`     | Returns a `play.api.libs.iteratee.Enumerator` of `jsQuery` matches |
|`findOne(jsQuery:JsValue)`                                                                                                    | `Future[Option[T]]` | Attempt to find one object that matches `jsQuery`                  |
|`findOne(example:T)`                                                                                                          | `Future[Option[T]]` | Attempt to find one object that matches the `example`              |
|`findById(id:String)`                                                                                                         | `Future[Option[T]]` | Attempt to find the object identified by `id`                      |
|`findById(ids:Iterable[String])`                                                                                                         | `Future[Iterable[T]]` | Attempt to find as many matches as possible for the supplied IDs         |
|`findByIdInOrder(ids:Iterable[String])`                                                                                                         | `Future[Iterable[Option[T]]]` | Attempt to find each object denoted by its ID - a `None` in any position means no object was found for the corresponding ID  |
|`listAll`                                                                                                                     | `Future[Seq[T]]`    | Returns all in the collection                                      |
|`listAll(size: Option[Int] = None, startFrom: Option[Int] = None)`                                                            | `Future[Seq[T]]`    | Returns all in the collection, paginated                           |
|`listWhere(jsQuery:JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = NonejsProjection: Option[JsValue] = None)`      | `Future[Seq[T]]`    | Returns `jsQuery` matches with optional pagination & sorting       |

##### Deletion

|            Method             |      Returns      |            Description                |
|-------------------------------|-------------------|---------------------------------------|
|`deleteById(id:String)`        | `Future[Boolean]` | Delete the object identified by `id`  |
|`deleteWhere(jsQuery:JsValue)` | `Future[Boolean]` | Delete all objects matching `jsQuery` |


#### Protected Methods
Your `Service` that extends `TypedMongoService[T]` also gets access to `protected` methods to make writing additional `T`-specific methods easy:
##### Retrieval

|         Method              |                       Returns                        |                   Description                    |
|-----------------------------|------------------------------------------------------|--------------------------------------------------|
|`findAll`                    | `reactivemongo.api.collections.GenericQueryBuilder`  | Get a GQB for all objects in the collection      |
|`findWhere(jsQuery:JsValue)` | `reactivemongo.api.collections.GenericQueryBuilder`  | Get a GQB for matching objects in the collection |

## A note about Write Concerns
By default, Mondrian uses the same [MongoDB Write Concern](https://docs.mongodb.com/manual/reference/write-concern/) as Play-ReactiveMongo - which is ([currently](https://github.com/ReactiveMongo/ReactiveMongo/blob/master/driver/src/main/scala/api/commands/rwcommands.scala#L44)) `Acknowledged`. 

Should you wish to specify a different level of Write Concern, override the `defaultWriteConcern` in your `Service`, like this:

```scala
import com.themillhousegroup.mondrian._
import models.VehicleJson 
import reactivemongo.api.commands.WriteConcern
 
class VehicleService extends TypedMongoService[Vehicle]("vehicles")(VehicleJson.vehicleFormat) {

  // Writes will be written to the journal and we'll wait until
  // one Mongo instance has acknowledged this write.
  override val defaultWriteConcern = WriteConcern.Journaled 
  
  // ...
}
```

## Examples
For an entity defined as:

```scala
case class ExampleEntity (
  _id: Option[MongoId],
  createdAt: DateTime,
  foo: Long,
  bar: String,
  bazzes: Seq[HeavyweightBaz]
) extends MongoEntity

```
Here's how some common service operations might look:

```scala
class ExampleEntityService @Inject() (val reactiveMongoApi:ReactiveMongoApi)
  extends TypedMongoService[ExampleEntity]("exampleEntities") {

  // Return the 'n' most recently-created documents:
  def listMostRecent(howMany:Option[Int]): Future[Seq[ExampleEntity]] = {
    listWhere(
      Json.obj(),
      howMany,
      None,
      Some(Json.obj("createdAt" -> -1)),
      Some(Json.obj("bazzes" -> 0)) // Drop this heavyweight array - saves a lot of latency
    )   
  }
  
  // Find documents that have a 'bar' value of "Cat" OR "Dog"
  def listCatsAndDogs: Future[Seq[ExampleEntity]] = {
  
    // Create a MongoDB '$or' using standard Play JSON:
    val catsOrDogsQuery = Json.obj(
      "$or" -> Json.arr (
        Json.obj ("bar" -> JsString("Cat")),
        Json.obj ("bar" -> JsString("Dog"))
      )
    )
    
    listWhere(catsOrDogsQuery)
  }
} 
  
```

## Logging

Add the following to your `logback.xml` to enable the maximum level of logging in Mondrian. In particular you'll see the gory details of the `save` and `saveAndPopulate` functions, which might help in debugging issues with persistence:

```
<logger name="com.themillhousegroup.mondrian" level="TRACE" />
```


## Credits

- [Play-ReactiveMongo](https://github.com/ReactiveMongo/Play-ReactiveMongo) does the hard work.


