Feature: Testing a REST API for Employee
  Users should be able to sent GET/POST/PUT/DELETE requests to a employee web service.

  Scenario: Add an Employee details
    Given A JSON Object having Employee details
    When users submits the data
    Then server returns a status of add employee

  Scenario: Employee details retrieval from a web service
    Given Employee id is provided
    When users want to get information about an employee
    Then the requested employee data is returned


  Scenario: All Employee details retrieval from a web service
    Given RESTFul url is provided
    When users wants to get all employee informations
    Then server returns a status of get all employees


  Scenario:  Employee details is  updated using a web service
    Given Employee Id is provided
    When users want to update employee informations
    Then server returns a status of update employees

  Scenario:  Employee details is  deleted using a web service
    Given Employee Id is provided
    When users want to delete employee informations
    Then server returns a status of delete employees
