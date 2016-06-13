mondrian
============================

An extra layer supplying the classic CRUD operations for Play-ReactiveMongo objects.


## Installation

Bring in the library by adding the following to your Play project's ```build.sbt```. 
The release repository: 

```
   resolvers ++= Seq(
     "Millhouse Bintray"  at "http://dl.bintray.com/themillhousegroup/maven"
   )
```
And the dependency itself: 

##### For Play 2.4.x:

```
   libraryDependencies ++= Seq(
     "com.themillhousegroup" %% "mondrian" % "0.2.21"
   )

```
##### For Play 2.5.x:

```
   libraryDependencies ++= Seq(
     "com.themillhousegroup" %% "mondrian" % "0.3.30"
   )

```

## Usage

Once you have __mondrian__ added to your Play project, you can start using it like this:

#### Enable the **ReactiveMongoModule** in your `application.conf`
You may already have this if you've been using the vanilla Reactive Mongo Module:
```
play.modules.enabled += "play.modules.reactivemongo.ReactiveMongoModule" 
```

#### Define the `mongodb.uri` in your `application.conf`
If you're coming from Play-ReactiveMongo you'll already have something appropriate in your `application.conf`, e.g.:

```
mongodb.uri="mongodb://user:password@ds12345.mongolab.com:12345/mydb"
```


#### Define a model object that extends the `MongoEntity` trait

A `MongoEntity` simply includes an `_id: Option[MongoId]` field like this:

```
  import com.themillhousegroup.mondrian.{MongoEntity, MongoId}
  
  case class Vehicle(
  	val _id: Option[MongoId],
  	val name: String,
  	val manufacturer:Manufacturer,
  	val yearFirstOffered:Int,
  	val yearLastOffered:Option[Int]) extends MongoEntity
```

#### Define an `implicit` Play-JSON `Format` for your new object 
You can extend `MongoJson` to get the implicit conversion for the `MongoId`; perhaps something like this:

```
import play.api.libs.json.Json

object VehicleJson extends MongoJson {
  import ManufacturerJson.manufacturerFormat
  
  implicit val vehicleFormat = Json.format[Vehicle]
}
```

#### Extend `TypedMongoService[T]` appropriately

This `Service` joins together your domain object, the name of the MongoDB collection that will hold it, and the `Format` to read/write it.

If you just want basic CRUD operations defined for your model object, you just need ONE line of code (plus the appropriate imports):  

```
import com.themillhousegroup.mondrian._
import models.VehicleJson._ 
 
class VehicleService extends TypedMongoService[Vehicle]("vehicles")
```  

Because of the `VehicleJson._` import, the compiler is able to find the implicit JSON `Format` it needs. You
can supply it explicitly if you prefer, as shown next.
Of course you can add extra methods that are useful; for example:


```
import com.themillhousegroup.mondrian._
import models.VehicleJson 
 
class VehicleService extends TypedMongoService[Vehicle]("vehicles")(VehicleJson.vehicleFormat) 
  
  def findVehiclesFirstSoldIn(year:Int):Future[List[Vehicle]] = {
    listWhere(Json.obj("yearFirstOffered" -> year))
  }
}
```  

In the above example, the `listWhere(JsValue)` function from the superclass is being used, but there are many more that you can utilize, for example `findOne(JsValue)`, `cursorWhere(JsValue)` and `enumerateWhere(JsValue)`. See the next section for the details.

#### Inject your new `Service` into your `Controller` classes

For example:

```
class VehicleController @Inject()(val vehicleService:VehicleService) extends Controller {

  ...
}

```

## Available Methods

#### Public Methods
As soon as you extend `TypedMongoService[T]`, your `Service` will have the following **public** methods available:

Method | Returns | Description
--- | ---
`countAll` | `Future[Int]` | Count the number of objects in the collection
`countWhere(jsQuery:JsValue)` | `Future[Int]` | Count the number of matches for `jsQuery`
`cursorWhere(jsQuery:JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None)` | `Cursor[T]` | Returns a `reactivemongo.api.Cursor` of `jsQuery` matches
`deleteById(id:String)` | `Future[Boolean]` | Delete the object identified by `id`
`deleteWhere(jsQuery:JsValue)` | `Future[Boolean]` | Delete all objects matching `jsQuery`
`enumerateWhere(jsQuery:JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None)` | `Enumerator[T]` | Returns a `play.api.libs.iteratee.Enumerator` of `jsQuery` matches
`findOne(jsQuery:JsValue)` | `Future[Option[T]]` | Attempt to find one object that matches `jsQuery`
`findOne(example:T)` | `Future[Option[T]]` | Attempt to find one object that matches the `example`
`findById(id:String)` | `Future[Option[T]]` | Attempt to find the object identified by `id`
`listAll` | `Future[Seq[T]]` | Returns all in the collection
`listAll(size: Option[Int] = None, startFrom: Option[Int] = None)` | `Future[Seq[T]]` | Returns all in the collection, paginated
`listWhere(jsQuery:JsValue, size: Option[Int] = None, startFrom: Option[Int] = None, sortWith: Option[JsObject] = None)` | `Future[Seq[T]]` | Returns `jsQuery` matches with optional pagination & sorting
`save(obj:T)` | `Future[Boolean]` | Persist `obj`. If its `_id` is `None`, will **insert**. Else will **update**
`save(objs:Iterable[T])` | `Future[Iterable[Boolean]` | Persist each of the  `objs` as per `save`
`saveAndPopulate(obj:T)` | `Future[Option[T]]` | Persist `obj` as per `save`, and return it with the `_id` field populated
`save(objs:Iterable[T])` | `Future[Iterable[Option[T]]]` | Persist each of the  `objs` as per `saveAndPopulate`
`saveIfNew(obj:T)` | `Future[Option[T]]` | If no similar object found, save `obj` as per `saveAndPopulate`. Otherwise, return the existing object from the collection

## Credits

- [Play-ReactiveMongo](https://github.com/ReactiveMongo/Play-ReactiveMongo) does the hard work.


