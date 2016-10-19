package com.ibm.employee.service.app

import java.net.InetAddress
import javax.ws.rs._

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, RouteResult}
import akka.stream.ActorMaterializer
import com.ibm.couchdb.{CouchDoc, CouchException, MappedDocType}
import io.swagger.annotations._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

case class Employee(name: String, band: String, location: String, skills: List[SkillSet] /*, doj:DateTime*/)

case class SkillSet(skill: String, experience: Int)

case class ResponseError(id: Int, message: String)

case class VCapCloudantService(cloudantNoSQLDB: List[Service])

case class Service(credentials: DBCredentials)

case class DBCredentials(username: String, password: String, host: String, port: Int, url: String)

trait ModelToJsonmapping extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val skillsetFormat = jsonFormat2(SkillSet)
  implicit val errorResponseFormat = jsonFormat2(ResponseError)
  implicit val dbCredsFormat = jsonFormat5(DBCredentials)
  implicit val serviceFormat = jsonFormat1(Service)
  implicit val vcapsServicesFormat = jsonFormat1(VCapCloudantService)

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

  override val logger = Logging(system, getClass)

  val endpoints =
    logRequestResult("employee-service") {
      swaggerResourcesRoute ~ employeeServiceApiRoutes ~ swaggerDocServiceRoute
    }

  val localhost = InetAddress.getLocalHost
  val interface = localhost.getHostAddress
  val bindingFuture = Http().bindAndHandle(RouteResult.route2HandlerFlow(endpoints),
    interface, Option(System.getenv("PORT")).getOrElse(s"${config.getInt("http.port")}").toInt)

  sys.ShutdownHookThread {
    println(s"unbinding port ${config.getInt("http.port")}")
    bindingFuture.flatMap(_.unbind())
    println("shutting down actor system...")
    system.terminate()
    println("system terminated...")
  }

  def swaggerResourcesRoute = {
    pathPrefix("swagger") {
      getFromResourceDirectory("swagger") ~
        pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
    }
  }

  def swaggerDocServiceRoute = {
    new SwaggerDocService(interface, config.getInt("http.port"), system, materializer).routes
  }

}

import akka.event.LoggingAdapter

@Path("/employee")
@Api(value = "Employee Service (Reverse Engineered) Routes", produces = "application/json")
trait EmployeeService extends Directives with ModelToJsonmapping with CouchDAO {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

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

  def employeeServiceApiRoutes = {
    pathPrefix("employee") {
      getByIdRoute ~ getByBandRoute ~ getAllByDocTypeRoute ~ postEntityRoute ~ updateEntityRoute ~ deleteEntityRoute
    }
  }

  @GET
  @Path("/{id}")
  @ApiOperation(value = "Get employee by ID", nickname = "getEmployeeByID",
    response = classOf[CouchDoc[Employee]])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", dataType = "String", paramType = "path", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NOT FOUND")
  ))
  def getByIdRoute = (path(Segment) & get) { id =>
    complete(read(id))
  }

  @GET
  @Path("band/{band}")
  @ApiOperation(value = "Get employees by band", nickname = "getEmployeesByBand",
    response = classOf[List[CouchDoc[Employee]]], responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "band", dataType = "String", paramType = "path", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  def getByBandRoute = (pathPrefix("band") & path(Segment) & get) { band =>
    complete(readByBand(band))
  }

  @GET
  @ApiOperation(value = "Get list of all employee type documents", nickname = "getAllEmployeeKind",
    response = classOf[List[CouchDoc[Employee]]], responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK")
  ))
  def getAllByDocTypeRoute = (get) {
    complete(readAllByType())
  }

  @POST
  @ApiOperation(value = "Create new employee", nickname = "postEmployee", produces = "application/json",
    response = classOf[CouchDoc[Employee]], responseContainer = "Set")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "employee", dataType = "com.ibm.employee.service.app.Employee", paramType = "body", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "User created")
  ))
  def postEntityRoute = (post & entity(as[Employee])) { employee =>
    val createdDoc = create(employee)
    complete(StatusCodes.Created -> createdDoc)
  }

  @PUT
  @Path("/{id}")
  @ApiOperation(value = "Update new employee", nickname = "postEmployee", produces = "application/json",
    response = classOf[CouchDoc[Employee]], responseContainer = "Set")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", dataType = "String", paramType = "path", required = true),
    new ApiImplicitParam(name = "employee", dataType = "com.ibm.employee.service.app.Employee", paramType = "body", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NOT FOUND")
  ))
  def updateEntityRoute =
    (
      path(Segment)) { id =>
      (put & entity(as[Employee])) { employee =>
        val createdDoc = update(id, employee)
        complete(StatusCodes.Accepted -> createdDoc)
      }
    }

  @DELETE
  @Path("/{id}")
  @ApiOperation(value = "Delete an employee", nickname = "deleteEmployee")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", dataType = "String", paramType = "path", required = true)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "NOT FOUND")
  ))
  def deleteEntityRoute = (path(Segment) & delete) { id =>
    remove(id)
    complete(StatusCodes.OK -> None)
  }
}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka._
import com.github.swagger.akka.model.Info

import scala.reflect.runtime.{universe => r}

class SwaggerDocService(address: String, port: Int, system: ActorSystem, actorMaterializer: ActorMaterializer)
  extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = actorMaterializer
  override val apiTypes = Seq(r.typeOf[EmployeeService])
  override val host = address + ":" + port
  override val info = Info(version = "1.0")
}

import com.ibm.couchdb.{CouchDb, CouchDoc, TypeMapping}
import com.typesafe.config.{Config, ConfigFactory}

trait CouchDAO extends ModelToJsonmapping {
  implicit val typeMapping = TypeMapping(classOf[Employee] -> "Employee")
  implicit val config: Config = ConfigFactory.load()
  implicit val couch = sys.env.get("VCAP_SERVICES") match {
    case None => CouchDb(config.getString("database.url"), config.getInt("database.port"),
      https = false, config.getString("database.user"), config.getString("database.password"))
    case Some(vcaps_services) => {
      val services: VCapCloudantService = vcaps_services.stripMargin.parseJson.convertTo[VCapCloudantService]
      val bluemixCloudantDbaasCredentials: DBCredentials = services.cloudantNoSQLDB(0).credentials
      CouchDb(bluemixCloudantDbaasCredentials.host, bluemixCloudantDbaasCredentials.port,
        https = true, bluemixCloudantDbaasCredentials.username, bluemixCloudantDbaasCredentials.password)
    }
  }
  implicit val db = couch.db(config.getString("database.name"), typeMapping)

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

