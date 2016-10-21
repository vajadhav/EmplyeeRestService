
/**
  * Created by IBM_ADMIN on 9/29/2016.
  */
import com.ibm.employee.service.app.EmployeeCouchService._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model._
import shapeless.PolyDefns.~>

class EmployeeDeleteSpec extends WordSpec with Matchers with ScalatestRouteTest {
  "Employee API" should {
    "Posting /{id} should delete the employee" in {
        //sending http delete request to delete a specific record
      //deleteEntityRoute is being used
      //check blocks validate the return status
        Delete("/251a1731a841c07c3b0e7bfc31b1f334") ~> deleteEntityRoute ~> check {
        //responseAs[Employee].name shouldEqual("Seema Anand")
        status.isSuccess() shouldEqual true

      }
    }
  }


}


