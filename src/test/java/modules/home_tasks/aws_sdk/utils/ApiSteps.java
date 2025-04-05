package modules.home_tasks.aws_sdk.utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.io.File;
import java.util.List;

import static modules.home_tasks.PropertyHandler.getProperty;
import static modules.home_tasks.aws_sdk.utils.AwsHooks.public_ip;
import static modules.home_tasks.aws_sdk.utils.BaseRequest.request;
import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;

public class ApiSteps {

    @Step("Upload image")
    public Response uploadImage(String path, String endpoint) {
        return request()
                .contentType("multipart/form-data")
                .multiPart("upfile", new File(path))
                .post(endpoint);
    }

    @Step("Delete image by ID")
    public Response deleteImageById(String imageId, String endpoint) {
        Response response = request()
                .delete(endpoint + "/" + imageId);
        response.then().statusCode(200);
        return response;
    }

    @Step("Get all subscriptions")
    public Response getAllSubscriptions() {
        Response response = request()
                .get("http://" + public_ip + "/api/notification");
        response.then().statusCode(200);
        return response;
    }

    @Step("Create a subscription")
    public Response createSubscription() {
        Response response = request()
                .post("http://" + public_ip + "/api/notification/" + getProperty("GMAIL_BOX"));
        response.then().statusCode(200);
        return response;
    }

    @Step("Delete subscription")
    public Response deleteSubscription() {
        Response response = request()
                .delete("http://" + public_ip + "/api/notification/" + getProperty("GMAIL_BOX"));
        response.then().statusCode(200);
        return response;
    }

    @Step("Get image Ids")
    public List<String> getImageIds(String endpoint) {
        Response response = request()
                .get(endpoint);
        response.then().statusCode(200);
        logMessage("Images: " + response.body().asString());
        return response.jsonPath().getList("id");
    }
}