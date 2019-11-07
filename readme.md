# Automatic script for [stackoverflow](https://stackoverflow.com)
***
### Used frameworks:
* Maven (automation building projects)
* Selenium (imitation of using browser)
* Allure (test report)

### Preparation
Go to ```\src\test\java\com\stackOverFlow\StackOverFlowTest.java```
and config this line in setUp() 
```
System.setProperty("", "");
``` 
to your configuration
for example
```
System.setProperty("webdriver.chrome.driver", "/path/to/driver");
```
### Run test
```
mvn clean test
mvn allure:serve
```