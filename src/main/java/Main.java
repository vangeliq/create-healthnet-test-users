//import org.keycloak.admin.client.resource.UserResource;
//import org.keycloak.representations.idm.ClientRepresentation;
//import org.keycloak.representations.idm.CredentialRepresentation;
//import org.keycloak.representations.idm.RoleRepresentation;

import au.com.bytecode.opencsv.CSVReader;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;

//https://www.javatpoint.com/how-to-read-csv-file-in-java
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
public class Main {

    public static void main(String[] args) throws Exception  {
        String inputFile = "C:\\Users\\valery.angelique\\IdeaProjects\\create-healthnet-test-users\\src\\main\\java\\data\\input.csv";
        String serverUrl = "http://localhost:8080/auth";
        String realm = "realm15";
        String clientId = "clientTest";
        String clientSecret = "246f56b4-c34e-4d09-b72b-278d33f489bb";
        // User "idm-admin" needs at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
        Keycloak keycloak = KeycloakBuilder.builder() //
                .serverUrl(serverUrl) //
                .realm(realm) //
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .build();

        		// Client "idm-client" needs service-account with at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
//		Keycloak keycloak = KeycloakBuilder.builder() //
//				.serverUrl(serverUrl) //
//				.realm(realm) //
//				.grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
//				.clientId(clientId) //
//				.clientSecret(clientSecret).build();

        CSVReader reader = new CSVReader(new FileReader(inputFile), ',' , '"' , 0);
        List<String[]> allRows = reader.readAll();

        for(String[] row : allRows){
            UserRepresentation user = makeUser(row);

                    // Get realm
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersRessource = realmResource.users();

                    // Create user (requires manage-users role)
        Response response = usersRessource.create(user);
        System.out.printf("Repsonse: %s %s%n", response.getStatus(), response.getStatusInfo());
        System.out.println(response.getLocation());
        String userId = CreatedResponseUtil.getCreatedId(response);

        System.out.printf("User created with userId: %s%n", userId);
        }


    // Define user
//        UserRepresentation user = new UserRepresentation();
//        user.setEnabled(true);
//        user.setUsername("tester123");
//        user.setFirstName("First");
//        user.setLastName("Lalasterst");
//        user.setEmail("tom+tester1@.local");
//        user.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));
//
//        // Get realm
//        RealmResource realmResource = keycloak.realm(realm);
//        UsersResource usersRessource = realmResource.users();
//
//        // Create user (requires manage-users role)
//        Response response = usersRessource.create(user);
//        System.out.printf("Repsonse: %s %s%n", response.getStatus(), response.getStatusInfo());
//        System.out.println(response.getLocation());
//        String userId = CreatedResponseUtil.getCreatedId(response);
//
//        System.out.printf("User created with userId: %s%n", userId);
    }

    public static UserRepresentation makeUser(String[] userInfo){
        String password = userInfo[1];
        String idp = userInfo[2];
        String role1 = userInfo[3];
        String role2 = userInfo[4];

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userInfo[0]);
        user.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));
        return user;
    }
}