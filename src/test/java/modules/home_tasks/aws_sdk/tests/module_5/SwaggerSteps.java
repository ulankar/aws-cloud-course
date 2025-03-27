package modules.home_tasks.aws_sdk.tests.module_5;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import modules.home_tasks.aws_sdk.utils.BaseRequest;

import java.io.File;
import java.util.List;

import static io.restassured.RestAssured.given;
import static modules.home_tasks.aws_sdk.tests.module_5.S3SwaggerTest.endpoint;
import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;

public class SwaggerSteps extends BaseRequest {

    @SneakyThrows
    @Step("Get image Ids")
    public List<Integer> getImageIds(String endpoint) {
        Response response = request
                .get(endpoint);
        response.then().statusCode(200);
        logMessage("Images: " + response.body().asString());
        return response.jsonPath().getList("id");
    }

    @Step("Get image by Id")
    public Response getImageById(int imageId, String endpoint) {
        Response response = request
                .get(endpoint + "/" + imageId);
        response.then().statusCode(200);
        return response;
    }

    @Step("Upload image")
    public Response uploadImage(String path, String endpoint) {
        return given(new RequestSpecBuilder()
                        .build())
                .filter(new RequestLoggingFilter())
                .filter(new ResponseLoggingFilter())
                .filter(new AllureRestAssured())
                .contentType("multipart/form-data")
                .multiPart("upfile", new File(path))
                .post(endpoint);
    }

    @Step("Download image by Id")
    Response downloadImage(String imageId, int responseCode) {
        Response response = request
                .get(endpoint + "/file/" + imageId);
        response.then().statusCode(responseCode);
        return response;
    }

    @Step("Delete image by ID")
    public Response deleteImageById(String imageId, String endpoint) {
        Response response = request
                .delete(endpoint + "/" + imageId);
        response.then().statusCode(200);
        return response;
    }
}
