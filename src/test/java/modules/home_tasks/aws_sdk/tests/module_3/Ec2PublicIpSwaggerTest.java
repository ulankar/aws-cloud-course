package modules.home_tasks.aws_sdk.tests.module_3;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import modules.home_tasks.aws_sdk.utils.BaseRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static modules.home_tasks.PropertyHandler.getProperty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EC2 test suite with Swagger")
public class Ec2PublicIpSwaggerTest extends BaseRequest {

    @Test
    @DisplayName("Verify body from EC2 GET response")
    public void testGetResponse() {
        Response response = request
                .get("http://" + getProperty("PublicInstancePublicIp") + "/");
        response.then().statusCode(200);
        assertAll(
                () -> assertEquals("eu-central-1a", response.jsonPath().getString("availability_zone")),
                () -> assertEquals(getProperty("PublicInstancePrivateIp"), response.jsonPath().getString("private_ipv4")),
                () -> assertEquals("eu-central-1", response.jsonPath().getString("region")));
    }

    @Test
    @DisplayName("Verify headers from EC2 GET response")
    public void testHeaders() {
        Response response = request
                .get("http://" + getProperty("PublicInstancePublicIp") + "/");
        Headers headers = response.getHeaders();
        assertAll(
                () -> assertEquals("close", headers.getValue("connection")),
                () -> assertEquals("104", headers.getValue("content-length")),
                () -> assertEquals("application/json", headers.getValue("content-type")),
                () -> assertEquals("Werkzeug/2.2.3 Python/3.7.16", headers.getValue("server"))
        );
    }
}
