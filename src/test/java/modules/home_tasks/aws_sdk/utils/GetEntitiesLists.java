package modules.home_tasks.aws_sdk.utils;

import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.ListGroupsRequest;
import software.amazon.awssdk.services.iam.model.ListGroupsResponse;
import software.amazon.awssdk.services.iam.model.ListPoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListPoliciesResponse;
import software.amazon.awssdk.services.iam.model.ListRolesRequest;
import software.amazon.awssdk.services.iam.model.ListRolesResponse;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.Policy;
import software.amazon.awssdk.services.iam.model.Role;
import software.amazon.awssdk.services.iam.model.User;

import java.util.List;
import java.util.logging.Logger;

public class GetEntitiesLists {
    static Logger logger = Logger.getLogger(GetEntitiesLists.class.getName());

    public static List<User> listUsers(IamClient iam) {
        ListUsersRequest request = ListUsersRequest.builder().build();
        ListUsersResponse response = iam.listUsers(request);
        logger.info("The users " + response.users().toString());
        return response.users();
    }

    public static List<Role> listRoles(IamClient iam) {
        ListRolesRequest request = ListRolesRequest.builder().build();
        ListRolesResponse response = iam.listRoles(request);
        logger.info("The roles " + response.roles().toString());
        return response.roles();
    }

    public static List<Group> listGroups(IamClient iam) {
        ListGroupsRequest request = ListGroupsRequest.builder().build();
        ListGroupsResponse response = iam.listGroups(request);
        logger.info("The groups " + response.groups().toString());
        return response.groups();
    }

    public static List<Policy> listPolicies(IamClient iam) {
        ListPoliciesRequest request = ListPoliciesRequest.builder().build();
        ListPoliciesResponse response = iam.listPolicies(request);
        logger.info("The policies " + response.policies().toString());
        return response.policies();
    }
}
