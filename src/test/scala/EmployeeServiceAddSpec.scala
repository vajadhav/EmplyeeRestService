/**
  * Created by Vandana on 9/27/2016.
  */

import com.ibm.employee.service.app.EmployeeCouchService._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}
import akka.http.scaladsl.model._


 class EmployeeServiceAddSpec extends WordSpec with Matchers with ScalatestRouteTest {


  "Employee API" should {
    "Posting to /employee should add the employee" in {
      //Creating a test employee JSON object to insert
      val jsonRequest = ByteString(
        s"""
          {
           |
           |"id": "1",
           |"name": "Tony Ananad",
           |"band": "7A",
           |"doj" : "2015-12-12",
           |"location":"Silokhera Gurgaon",
           |"skills": [
           |    {
           |        "skill":"Spring Boot",
           |        "experience":5
           |    },
           |    {
           |        "skill":"Cloudant",
           |        "experience":6
           |    },
           |    {
           |        "skill":"Scala",
           |        "experience":0
           |    },
           |    {
           |        "skill":"Akka",
           |        "experience":0
           |    }
           |],
           |"kind":"Employee"
            |}
        """.stripMargin)
      //Preparing the request object
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "employee",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      //sending http post request to add a particular employee records
      //postEntityRoute is being used
      //check blocks validate the return status
      postRequest ~> postEntityRoute ~> check {
        status.isSuccess() shouldEqual true
      }
    }

 }


}



