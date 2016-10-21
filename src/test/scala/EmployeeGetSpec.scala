import com.ibm.employee.service.app.EmployeeCouchService._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class EmployeeGetSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Employee API" should {
    "Get /{id} should fetch an employee" in {
      //sending http get request to fetch a particular employee records
      //getByIdRoute is being used
      //check blocks validate the return status
          Get("/251a1731a841c07c3b0e7bfc31b6e52f") ~> getByIdRoute ~> check {
            status.isSuccess() shouldEqual true
        }
   }

  }}
