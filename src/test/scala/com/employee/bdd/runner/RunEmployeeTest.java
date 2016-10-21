package com.employee.bdd.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)

@CucumberOptions(
        format = {"pretty", "html:target/cucumber"},
        glue = "com.employee.bdd.steps",
        features = "classpath:cucumber/employee.feature"
)
//This class is necessary for running the cucumber test cases(employee.feature, employeeStepDefinition.java)
//This is an empty class
public class RunEmployeeTest {
}
