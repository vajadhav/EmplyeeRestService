package com.employee.bdd.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class EmployeeStepDefinition {
    //  http://169.254.219.130:8765/employee
    private static final String RESTFUL_URL = "http://169.254.219.130:8765/employee";
    private static final String EMPLOYEE_ID = "/36473afed12fd7ffc00f459a71028188";
    private static final String APPLICATION_JSON = "application/json";
    private static final String JSON_FILE = "employee.json";

    private static String convertHttpResponseToString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        return convertInputStreamToString(inputStream);
    }

    private static String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String string = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return string;
    }

    @Given("^A JSON Object having Employee details$")
    public void a_JSON_Object_having_Employee_details() throws Throwable {

    }

    @When("^users submits the data$")
    public void users_submits_the_data() throws Throwable {
        InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream("employee.json");
        String jsonString = new Scanner(jsonInputStream, "UTF-8").useDelimiter("\\Z").next();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost(RESTFUL_URL);
        StringEntity entity = new StringEntity(jsonString);
        request.addHeader("content-type", APPLICATION_JSON);
        request.setEntity(entity);
        HttpResponse response = httpClient.execute(request);
        assertEquals(201, response.getStatusLine().getStatusCode());
    }

    @Then("^server returns a status of add employee$")
    public void server_returns_a_status_of_add_employee() throws Throwable {

    }

    @Given("^Employee id is provided$")
    public void employee_id_is_provided() throws Throwable {

    }

    @When("^users want to get information about an employee$")
    public void users_want_to_get_information_about_an_employee() throws Throwable {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(RESTFUL_URL + EMPLOYEE_ID);
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals(APPLICATION_JSON, httpResponse.getFirstHeader("Content-Type").getValue());
    }

    @Then("^the requested employee data is returned$")
    public void the_requested_employee_data_is_returned() throws Throwable {

    }

    @Given("^RESTFul url is provided$")
    public void restful_url_is_provided() throws Throwable {

    }

    @When("^users wants to get all employee informations$")
    public void users_wants_to_get_all_employee_informations() throws Throwable {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(RESTFUL_URL);
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals(APPLICATION_JSON, httpResponse.getFirstHeader("Content-Type").getValue());
    }

    @Then("^server returns a status of get all employees$")
    public void server_returns_a_status_of_get_all_employees() throws Throwable {

    }

    @Given("^Employee Id is provided$")
    public void employee_Id_is_provided() throws Throwable {

    }

    @When("^users want to update employee informations$")
    public void users_want_to_update_employee_informations() throws Throwable {
        InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream(JSON_FILE);
        String jsonString = convertInputStreamToString(jsonInputStream);
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPut httpPost = new HttpPut(RESTFUL_URL + EMPLOYEE_ID);
        StringEntity entity = new StringEntity(jsonString, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", APPLICATION_JSON);
        httpPost.setHeader("Content-type", APPLICATION_JSON);
        CloseableHttpResponse httpResponse = client.execute(httpPost);
        assertEquals(202, httpResponse.getStatusLine().getStatusCode());
        client.close();
    }

    @Then("^server returns a status of update employees$")
    public void server_returns_a_status_of_update_employees() throws Throwable {

    }

    @When("^users want to delete employee informations$")
    public void users_want_to_delete_employee_informations() throws Throwable {

    }

    @Then("^server returns a status of delete employees$")
    public void server_returns_a_status_of_delete_employees() throws Throwable {

    }


}