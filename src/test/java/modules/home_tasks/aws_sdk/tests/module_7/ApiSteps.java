package modules.home_tasks.aws_sdk.tests.module_7;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.io.File;

import static modules.home_tasks.aws_sdk.utils.AwsHooks.public_ip;
import static modules.home_tasks.aws_sdk.utils.BaseRequest.request;

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
}