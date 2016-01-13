mondrian
============================

An extra layer supplying the classic CRUD operations for Play-ReactiveMongo objects.


## Installation

Bring in the library by adding the following to your Play project's ```build.sbt```. 

  - The release repository: 

```
   resolvers ++= Seq(
     "Millhouse Bintray"  at "http://dl.bintray.com/themillhousegroup/maven"
   )
```
  - The dependency itself: 

```
   libraryDependencies ++= Seq(
     "com.themillhousegroup" %% "mondrian" % "0.1.3"
   )

```

## Usage

Once you have __mondrian__ added to your Play project, you can start using it like this:

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

#### Define a Play-JSON `Format` for your new object
Perhaps something like this:

```
import play.api.libs.json.Json

object VehicleJson {
  import com.themillhousegroup.mondrian.MongoJson.mongoIdFormat
  import ManufacturerJson.manufacturerFormat
  
  val vehicleFormat = Json.format[Vehicle]
}
```

#### Extend `TypedMongoService[T]` appropriately

This `Service` joins together your domain object, the name of the MongoDB collection that will hold it, and the `Format` to read/write it.

If you just want basic CRUD operations defined for your model object, you just need three lines:  

```
import com.themillhousegroup.mondrian._
 
class VehicleService extends TypedMongoService[Vehicle]("vehicles") {
  val fmt=VehicleJson.vehicleFormat
}
```  

But of course you can add extra methods that are useful; for example:


```
import com.themillhousegroup.mondrian._
 
class VehicleService extends TypedMongoService[Vehicle]("vehicles") {
  val fmt=VehicleJson.vehicleFormat
  
  def findVehiclesFirstSoldIn(year:Int):Future[List[Vehicle]] = {
    listWhere(Json.obj("yearFirstOffered" -> year))
  }
}
```  

In the above example, the `listWhere(JsValue)` function from the superclass is being used, but there are many more that you can utilize, for example `findOne(JsValue)`, `cursorWhere(JsValue)` and `enumerateWhere(JsValue)`. Check `MongoService.scala` for the details.

#### Inject your new `Service` into your `Controller` classes

For example:

```
class VehicleController @Inject()(val vehicleService:VehicleService) extends Controller {

  ...
}

```


## Credits

- [Play-ReactiveMongo](https://github.com/ReactiveMongo/Play-ReactiveMongo) does the hard work.


