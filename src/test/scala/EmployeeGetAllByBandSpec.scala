import com.ibm.employee.service.app.EmployeeCouchService._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class EmployeeGetAllByBandSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Employee API" should {
    "Get to /band/{value} should fetch the employees" in {

          Get("/band/7A") ~> getByBandRoute ~> check {
            status.isSuccess() shouldEqual true
        }
   }

  }}
