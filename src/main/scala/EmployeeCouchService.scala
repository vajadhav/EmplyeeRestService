package com.ibm.employee.service.app

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import com.ibm.couchdb.{CouchDoc, CouchException, MappedDocType}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

case class Employee(name: String, band: String, location: String, skills: List[SkillSet] /*, doj:DateTime*/)

case class SkillSet(skill: String, experience: Int)

case class ResponseError(id: Int, message: String)


trait ModelToJsonmapping extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val skillsetFormat = jsonFormat2(SkillSet)
  implicit val errorResponseFormat = jsonFormat2(ResponseError)

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

    val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }

  implicit val employeeFormat = jsonFormat4(Employee)

  implicit object couchDocFormat extends RootJsonFormat[CouchDoc[Employee]] {
    def write(doc: CouchDoc[Employee]) = {
      JsObject("_id" -> JsString(doc._id),
        "_rev" -> JsString(doc._rev),
        "kind" -> JsString(doc.kind),
        "doc" -> JsObject(
          "name" -> JsString(doc.doc.name),
          "band" -> JsString(doc.doc.band),
          "location" -> JsString(doc.doc.location),
          "skills" -> JsArray(doc.doc.skills.toJson)
        ))
    }

    def read(value: JsValue) = ???
  }

}


object EmployeeCouchService extends App with EmployeeService {

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()
  override val logger = Logging(system, getClass)

  val bindingFuture = Http().bindAndHandle(endpoints, config.getString("http.interface"), config.getInt("http.port"))

  sys.ShutdownHookThread {
    println(s"unbinding port ${config.getInt("http.port")}")
    bindingFuture.flatMap(_.unbind())
    println("shutting down actor system...")
    system.terminate()
    println("system terminated...")
  }


}

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._


trait EmployeeService extends ModelToJsonmapping with CouchDAO {
  implicit val system: AnyRef
  implicit val executor: AnyRef
  implicit val materializer: AnyRef

  val logger: LoggingAdapter

  implicit def myExceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex: CouchException[Error] =>
      extractUri { uri =>
        println(s"Request to $uri resulted in an error")
        complete(StatusCodes.NotFound -> new ResponseError(StatusCodes.NotFound.intValue
          , "document with specified ID does not exist"))
      }
    case _: Throwable => complete(StatusCodes.InternalServerError -> new ResponseError(StatusCodes.InternalServerError.intValue
      , "An unexpected error occured while serving request"))
  }

  val endpoints =
    logRequestResult("employee-service") {
      pathPrefix("employee") {
          (path(Segment) & get) { id =>
            complete(read(id))
          } ~
          (pathPrefix("band") & path(Segment) & get) { band =>
            complete(readByBand(band))
          } ~
            (get) {
              complete(readAllByType())
            } ~
          (post & entity(as[Employee])) { employee =>
            val createdDoc = create(employee)
            complete(StatusCodes.Created -> createdDoc)
          } ~
          (path(Segment)) { id =>
            (put & entity(as[Employee])) { employee =>
              val createdDoc = update(id, employee)
              complete(StatusCodes.Accepted -> createdDoc)
            }
          } ~
          (path(Segment) & delete) { id =>
            remove(id)
            complete(StatusCodes.OK -> None)
          }
      }
    }
}

import com.ibm.couchdb.{CouchDb, CouchDoc, TypeMapping}
import com.typesafe.config.{Config, ConfigFactory}

trait CouchDAO {
  implicit val typeMapping = TypeMapping(classOf[Employee] -> "Employee")
  implicit val config: Config = ConfigFactory.load()
  implicit val couch = CouchDb(config.getString("database.url"), config.getInt("database.port"),
    https = false, config.getString("database.user"), config.getString("database.password"))
  val db = couch.db(config.getString("database.name"), typeMapping)

  def create(doc: Employee): CouchDoc[Employee] = {
    val createdDoc = db.docs.create(doc).unsafePerformSync
    val couchDoc = db.docs.get[Employee](createdDoc.id).unsafePerformSync
    couchDoc
  }

  def readAllByType(): List[CouchDoc[Employee]] = {
    val couchDocs = db.docs.getMany.byType[(String, String), String]("byKind", "Employee"
      , MappedDocType("Employee")).includeDocs[Employee].build.query.unsafePerformSync
    couchDocs.getDocs.toList
  }

  def readByBand(band: String): List[CouchDoc[Employee]] = {
    val couchDocs = db.docs.getMany.byType[String, String]("byBand", "Employee"
      , MappedDocType("Employee")).includeDocs[Employee].key(band).build.query.unsafePerformSync
    couchDocs.getDocs.toList
  }

  def read(id: String): CouchDoc[Employee] = {
    val couchDoc = db.docs.get[Employee](id).unsafePerformSync
    couchDoc
  }

  def update(id: String, emp: Employee): CouchDoc[Employee] = {
    var couchDoc = db.docs.get[Employee](id).unsafePerformSync
    if (couchDoc != null) {
      couchDoc = couchDoc.copy(doc = emp)
      db.docs.update(couchDoc).unsafePerformSync
    }
    couchDoc
  }

  def remove(id: String) = {
    val couchDoc = db.docs.get[Employee](id).unsafePerformSync
    if (couchDoc != null)
      db.docs.delete(couchDoc).unsafePerformSync
  }
}

