import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.io.FileReader;
import au.com.bytecode.opencsv.CSVReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String inputFile = "C:\\Users\\valery.angelique\\IdeaProjects\\create-healthnet-test-users\\src\\main\\java\\data\\input.csv";
        String serverUrl = "http://localhost:8080/auth";
        String realm = "realm15";
        String clientId = "clientTest";
        String clientSecret = "246f56b4-c34e-4d09-b72b-278d33f489bb";

        Keycloak keycloak = KeycloakBuilder.builder() //
                .serverUrl(serverUrl) //
                .realm(realm) //
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .build();

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        CSVReader reader = new CSVReader(new FileReader(inputFile), ',', '"', 0);
        List<String[]> allRows = reader.readAll();

        for (String[] row : allRows) {
            addUser(row,usersResource);

        }
        printUserList(usersResource);

    }

    /*
    userinfo is: username, password, IDP, role1, role2
    */
    private static void addUser(String[] userInfo, UsersResource usersResource) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userInfo[0]);
        user.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));

        Response response = usersResource.create(user);
        System.out.printf("Response: %s %s%n", response.getStatus(), response.getStatusInfo());
        System.out.println(response.getLocation());
        String userId = CreatedResponseUtil.getCreatedId(response);
        System.out.printf("User created with userId: %s%n", userId);
    }


    public static void setUserPassword(String userId) {
        //todo: implement this
    }

    ;

    public static void addRoleToUser() {

    }

    public static void printUserList(UsersResource usersResource) {
        List<UserRepresentation> users = usersResource.list();
        for (UserRepresentation user : users) {
            String name = user.getUsername();
            String id = user.getId();
            System.out.println(name + " id: " + id);
        }
    }
}