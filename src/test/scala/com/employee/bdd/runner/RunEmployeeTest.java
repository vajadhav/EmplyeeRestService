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
public class RunEmployeeTest {
}
