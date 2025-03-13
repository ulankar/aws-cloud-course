package modules.home_tasks.aws_sdk.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import modules.home_tasks.BasicHooks;


public abstract class BaseRequest extends BasicHooks {

    protected final RequestSpecification request;

    public BaseRequest() {
        request = RestAssured.given()
                             .filter(new RequestLoggingFilter())
                             .filter(new ResponseLoggingFilter())
                             .filter(new AllureRestAssured());
    }
}
