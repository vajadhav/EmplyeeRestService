package junit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class JUnitManaged {

    //Constants
    private static final String RESTFUL_URL = "http://169.254.219.130:8765/employee";
    private static final String EMPLOYEE_ID = "/1fb897411f38012559b9a476644ccdb4";
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

    @Test
    public void givenJUnitManagedServer_addEmployee_thenCorrect()
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(RESTFUL_URL);
        InputStream jsonInputStream = this.getClass().getClassLoader().getResourceAsStream(JSON_FILE);
        String jsonString = convertInputStreamToString(jsonInputStream);
        StringEntity entity = new StringEntity(jsonString);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", APPLICATION_JSON);
        httpPost.setHeader("Content-type", APPLICATION_JSON);
        CloseableHttpResponse httpResponse = client.execute(httpPost);
        assertEquals(201, httpResponse.getStatusLine().getStatusCode());
        client.close();
    }

    @Test
    public void givenJUnitManagedServer_updateemployee_thenCorrect() throws IOException {
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

    @Test
    public void givenJUnitManagedServer_getAllEmployees_thenCorrect() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(RESTFUL_URL);
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals(APPLICATION_JSON, httpResponse.getFirstHeader("Content-Type").getValue());
    }

    @Test
    public void givenJUnitManagedServer_getEmployee_thenCorrect() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(RESTFUL_URL + EMPLOYEE_ID);
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals(APPLICATION_JSON, httpResponse.getFirstHeader("Content-Type").getValue());
    }

    @Ignore
    public void givenJUnitManagedServer_deleteEmployee_thenCorrect() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(RESTFUL_URL + EMPLOYEE_ID);
        HttpResponse httpResponse = httpClient.execute(request);
        String stringResponse = convertHttpResponseToString(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }


}