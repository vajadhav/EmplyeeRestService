import com.ibm.employee.service.app.EmployeeCouchService._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model._
import shapeless.PolyDefns.~>

class EmployeeGetAllSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Employee API" should {
    "Posting to /employee should fetch all employees" in {
      //sending http get request to fetch all employee records
      //getAllByDocTypeRoute is being used
      //check blocks validate the return status
          Get("/employee") ~> getAllByDocTypeRoute ~> check {
            status.isSuccess() shouldEqual true
        }
   }

  }}
