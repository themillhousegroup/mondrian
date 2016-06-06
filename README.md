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
TBA


## Credits

- [Play-ReactiveMongo](https://github.com/ReactiveMongo/Play-ReactiveMongo) does the hard work.


