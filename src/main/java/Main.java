//import org.keycloak.admin.client.resource.UserResource;
//import org.keycloak.representations.idm.ClientRepresentation;
//import org.keycloak.representations.idm.CredentialRepresentation;
//import org.keycloak.representations.idm.RoleRepresentation;

import java.io.File;
import java.io.FileNotFoundException;

//https://www.javatpoint.com/how-to-read-csv-file-in-java
import java.util.Scanner;
public class Main {

    public static void main(String[] args) throws Exception  {
        String inputFile = "data/input.csv";
        String serverUrl = "http://localhost:8080/auth";
        String realm = "realm15";
        // idm-client needs to allow "Direct Access Grants: Resource Owner Password Credentials Grant"
        String clientId = "clientTest";
        String clientSecret = "246f56b4-c34e-4d09-b72b-278d33f489bb";
        // User "idm-admin" needs at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
//        Keycloak keycloak = KeycloakBuilder.builder() //
//                .serverUrl(serverUrl) //
//                .realm(realm) //
//                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
//                .clientId(clientId) //
//                .clientSecret(clientSecret) //
//                .build();


        //		// Client "idm-client" needs service-account with at least "manage-users, view-clients, view-realm, view-users" roles for "realm-management"
//		Keycloak keycloak = KeycloakBuilder.builder() //
//				.serverUrl(serverUrl) //
//				.realm(realm) //
//				.grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
//				.clientId(clientId) //
//				.clientSecret(clientSecret).build();


        System.out.println("running the scanner");
        File file = new File("C:\\Users\\valery.angelique\\IdeaProjects\\create-healthnet-test-users\\src\\main\\java\\data\\input.txt");
        Scanner sc = new Scanner(file);

        sc.useDelimiter(",");   //sets the delimiter pattern
            while (sc.hasNext())  //returns a boolean value
            {
                System.out.print(sc.next());  //find and returns the next complete token from this scanner
            }
            sc.close();  //closes the scanner


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
}