import com.ibm.employee.service.app.EmployeeCouchService._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class EmployeeGetAllByBandSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Employee API" should {
    "Get /band/{value} should fetch the employees" in {
      //sending http get request to fetch records by band
      //getByBandRoute is being used
      //check blocks validate the return status
          Get("/band/7A") ~> getByBandRoute ~> check {
            status.isSuccess() shouldEqual true
        }
   }

  }}
