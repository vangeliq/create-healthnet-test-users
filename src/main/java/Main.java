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

        // todo: adding users
        for (String[] row : allRows) {
            addUser(row,usersResource);
            setUserPassword(row[0],row[1],usersResource);

        }
        printUserList(usersResource);


        //todo: deleting users
        for (String[] row: allRows) {
//            deleteUser(row[0], usersResource);
        }
        printUserList(usersResource);

    }




    // HELPERS:

    // userinfo is: username, password, IDP, role1, role2
    private static String getUserID(String userName, UsersResource usersResource) throws Exception{
        List<UserRepresentation> users = usersResource.search(userName);
        if(users.size() > 1){
            throw new Exception("error: more than 1 user found");
        }else if (users.size() == 0){
            throw new Exception("no users with that username found");
        } else {
            return users.get(0).getId();
        }
    }

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

    // todo: should throw exception instead?
    private static void deleteUser(String userName, UsersResource usersResource) {
        String userID;
        try {
            userID = getUserID(userName, usersResource);
            usersResource.delete(userID);
        } catch (Exception e) {e.printStackTrace();}
    }

    public static void setUserPassword(String userName, String password, UsersResource usersResource) {
        try{
            String userID = getUserID(userName,usersResource);
            UserResource userResource =  usersResource.get(userID);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setCredentialData(password);
            credentialRepresentation.setValue(password);
            credentialRepresentation.setSecretData(password);

            userResource.resetPassword(credentialRepresentation);
            System.out.println(userName + "'s password has been reset.");
        }catch (Exception e){e.printStackTrace();
        }
    }

    public static void addRoleToUser() {

    }

    public static void printUserList(UsersResource usersResource) {
        List<UserRepresentation> users = usersResource.list();
        System.out.println("list of all users:");

        for (UserRepresentation user : users) {
            String name = user.getUsername();
            String id = user.getId();
            System.out.println("\t" + name + " id: " + id);
        }
    }
}