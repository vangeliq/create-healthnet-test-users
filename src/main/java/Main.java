import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;

import javax.management.relation.Role;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.FileReader;
import au.com.bytecode.opencsv.CSVReader;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // todo: turn these into arguments.
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

        // reading the file
        CSVReader reader = new CSVReader(new FileReader(inputFile), ',', '"', 0);
        List<String[]> entries = reader.readAll();

        // todo: adding users
        for (String[] entry : entries) {
//            addUser(entry,usersResource);
            setUserPassword(entry[0],entry[1],usersResource);
            addUserIDP(entry[0],entry[2],usersResource);
            addUserRealmRole(entry[0],entry[3],realmResource); // todo: fix this
            addUserClientRole(entry[0],entry[4],entry[5],realmResource);
        }
        printUserList(usersResource);


        //todo: deleting users
//        for (String[] row: allRows) deleteUser(entry[0], usersResource);
//        printUserList(usersResource);
    }




    // todo: what to do if user is already there
    private static void addUser(String[] userInfo, UsersResource usersResource) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userInfo[0]);
//        user.setAttributes(Collections.singletonMap("origin", Arrays.asList("demo")));
        try {
            Response response = usersResource.create(user);
            System.out.printf("Response: %s %s%n", response.getStatus(), response.getStatusInfo());
            System.out.println(response.getLocation());
            String userId = CreatedResponseUtil.getCreatedId(response);
            System.out.printf("User created with userId: %s%n", userId);
        }catch(WebApplicationException e){
            e.printStackTrace();
        }
    }

    // todo: should throw exception instead? should it ignore if not found?
    private static void deleteUser(String userName, UsersResource usersResource) {
        String userID;
        try {
            userID = getUserID(userName, usersResource);
            usersResource.delete(userID);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    // todo: what to do if user not found
    public static void setUserPassword(String userName, String password, UsersResource usersResource) {
        try{
            String userID = getUserID(userName,usersResource);
            UserResource userResource =  usersResource.get(userID);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(password);

            userResource.resetPassword(credentialRepresentation);
            System.out.println(userName + "'s password has been reset.");
        }catch (Exception e){
//            e.printStackTrace();
        }
    }

    // todo: what to do if user not found
    private static void addUserIDP(String userName, String idp, UsersResource usersResource) {
        try{
            String userID = getUserID(userName,usersResource);
            UserResource userResource =  usersResource.get(userID);

            FederatedIdentityRepresentation federatedIdRepresentation = new FederatedIdentityRepresentation();
            federatedIdRepresentation.setIdentityProvider(idp);
            federatedIdRepresentation.setUserId(userName);
            federatedIdRepresentation.setUserName(userName);

            userResource.addFederatedIdentity(idp,federatedIdRepresentation);
            System.out.println(federatedIdRepresentation.getIdentityProvider() + " added to " + userName + "'s profile.");
        }catch (Exception e){
//            e.printStackTrace();
        }
    }

    // todo: what to do if user not found
    private static void addUserRealmRole(String userName, String role, RealmResource realmResource) {
        try {
            String userID = getUserID(userName, realmResource.users());

            RolesResource rolesResource =  realmResource.roles();
            List<RoleRepresentation> roleRepresentationList =  rolesResource.list(role,true);
            if(roleRepresentationList.size() > 1){ throw new Exception("more than 1 option found");
            }else if (roleRepresentationList.size() <1){ throw new Exception("role not found");
            }else{
                UserResource userResource = realmResource.users().get(userID);
                RoleMappingResource roleMappingResource =  userResource.roles();
                RoleScopeResource realmRoleScopeResource = roleMappingResource.realmLevel();
                realmRoleScopeResource.add(roleRepresentationList);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // todo: what to do if user not found
    private static void addUserClientRole(String userName, String clientID, String roleName,
                                          RealmResource realmResource) {
        try {
            String userID = getUserID(userName, realmResource.users());

            ClientsResource clientsResource =  realmResource.clients();
            List<ClientRepresentation> clientRepresentationList = clientsResource.findByClientId(clientID);

            if(clientRepresentationList.size() > 1){ throw new Exception("more than 1 option found");
            }else if (clientRepresentationList.size() <1){ throw new Exception("role not found");
            }else{
                String clientUUID = clientRepresentationList.get(0).getId();

                ClientResource clientResource =  clientsResource.get(clientUUID);
                RolesResource rolesResource = clientResource.roles();
                List<RoleRepresentation> roleRepresentationList = rolesResource.list(roleName,true);
                System.out.println("names of roles found");
                for(RoleRepresentation roleRepresentation: roleRepresentationList) System.out.println("\t" + roleRepresentation.getName());
                if(roleRepresentationList.size() >1) {
                    throw new Exception();
                }else if (roleRepresentationList.size() < 1){
                    throw new Exception();
                }else {
                    UserResource userResource = realmResource.users().get(userID);
                    RoleMappingResource roleMappingResource = userResource.roles();

                    RoleScopeResource roleScopeResource = roleMappingResource.clientLevel(clientUUID);
                    roleScopeResource.add(roleRepresentationList);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    // todo: what to do if user not found
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