import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.*;

import au.com.bytecode.opencsv.CSVReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Main {
    private static String configPath;

    private static String inputFile;
    private static String serverURL;
    private static String realm;
    private static String clientID;
    private static String clientSecret;

    private static Keycloak keycloak;
    private static RealmResource realmResource;
    private static UsersResource usersResource;
    private static HashMap<String,User> users;

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        if (args != null && args.length != 0) {
            configPath = args[0];
        } else {
            configPath = "C:\\Users\\valery.angelique\\IdeaProjects\\create-healthnet-test-users\\src\\main\\java\\configuration.properties";
        }
        LOG.info(String.format("Configuration file expected at '%s'.", configPath));

        init();

        for (User user : users.values()) {
//            deleteUserFromKeyCloak(user.getUserRepresentation().getUsername());
            addUserToKeyCloak(user.getUserRepresentation());
            addUserClientRoles(user);
        }
    }

    private static void init() throws IOException {
        Properties configProperties = new Properties();
        File file = new File(configPath);

        InputStream inputStream;
        if (file.exists()) {
            inputStream = new FileInputStream(file);
        } else {
            inputStream = Main.class.getResourceAsStream(configPath);
        }
        Objects.requireNonNull(inputStream, String.format("Configuration file not found at '%s'.", configPath));
        configProperties.load(inputStream);

        inputFile = configProperties.getProperty("inputFile");
        checkMandatory(inputFile);
        serverURL = configProperties.getProperty("serverURL");
        checkMandatory(serverURL);
        realm = configProperties.getProperty("realm");
        checkMandatory(realm);
        clientID = configProperties.getProperty("clientID");
        checkMandatory(clientID);
        clientSecret = configProperties.getProperty("clientSecret");
        checkMandatory(clientSecret);


        keycloak =  KeycloakBuilder.builder() //
                .serverUrl(serverURL) //
                .realm(realm) //
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
                .clientId(clientID) //
                .clientSecret(clientSecret) //
                .build();

        realmResource = keycloak.realm(realm);
        usersResource = realmResource.users();
//        users = toUsers(inputFile);
        users = toUsersFromJSON(inputFile);
    }

    private static HashMap<String,User> toUsersFromJSON(String inputFile){
        HashMap<String,User> result = new HashMap<String, User>();
        try {
            // create a reader
            Reader reader = Files.newBufferedReader(Paths.get(inputFile));
            // create parser
            JsonArray parser = (JsonArray) Jsoner.deserialize(reader);

            parser.forEach(entry -> {
                JsonObject jsonUser = (JsonObject) entry;
                String username = (String) jsonUser.get("username");
                String password = (String) jsonUser.get("password");
                User user = new User(username,password);

                Map<String,JsonArray> applications = (Map<String, JsonArray>) jsonUser.get("applications");
                applications.forEach((client, roles) -> {
                    roles.forEach(role -> user.addClientRoles(client, (String) role));
                });

                try{
                    if (result.containsKey(username)) throw new Exception("duplicate username found: " + username);
                    result.put(username,user);
                }catch(Exception e) {e.printStackTrace();}
            });

            //close reader
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    // adds user to realm, and updates id value on the user's UserRepresentation
    private static void addUserToKeyCloak(UserRepresentation user) {
        try {
            Response response = usersResource.create(user);
            System.out.printf("Response: %s %s%n", response.getStatus(), response.getStatusInfo());
            System.out.println(response.getLocation());
            String userId = CreatedResponseUtil.getCreatedId(response);
            user.setId(userId);
            System.out.printf("User created with userId: %s%n", userId);
        }catch(WebApplicationException e){
            e.printStackTrace();
        }
    }

    // todo: should throw exception instead? should it ignore if not found?
    private static void deleteUserFromKeyCloak(String userName) {
        try {
            String userID = getUserID(userName);
            usersResource.delete(userID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // todo: what to do if user not found
    private static void addUserClientRoles(User user){
        try{//try looking for the user
            String userID = getUserID(user);
            HashMap<String, Set<String>> clientRoles =  user.getClientRoles();

            for(String clientID: clientRoles.keySet()){
                String clientUUID = getClientUUID(clientID);

                ClientResource clientResource = realmResource.clients().get(clientUUID);
                List<RoleRepresentation> rolesToAdd = getRolesToAdd(clientRoles.get(clientID),clientResource);

                UserResource userResource = realmResource.users().get(userID);
                RoleMappingResource roleMappingResource = userResource.roles();
                RoleScopeResource roleScopeResource = roleMappingResource.clientLevel(clientUUID);
                roleScopeResource.add(rolesToAdd);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static List<RoleRepresentation> getRolesToAdd(Set<String> roles, ClientResource clientResource) throws Exception {// todo: test this
        List<RoleRepresentation> result = new ArrayList<>();
        RolesResource rolesResource = clientResource.roles();
        for(String roleName:roles){
            List<RoleRepresentation> roleRepresentationList = rolesResource.list(roleName,true);
            if(roleRepresentationList.size() > 1) throw new Exception("multiple roles with name " + roleName + " found");
            if(roleRepresentationList.size() < 1) throw new Exception(roleName + " not found");
            result.addAll(roleRepresentationList);
        }
        return result;
    }
    private static String getClientUUID(String clientID) throws Exception {
        ClientsResource clientsResource = realmResource.clients();
        List<ClientRepresentation> clientRepresentationList = clientsResource.findByClientId(clientID);
        if (clientRepresentationList.size() > 1) {
            throw new Exception("more than 1 client found");
        } else if (clientRepresentationList.size() < 1) {
            throw new Exception("client not found");
        } else {
            String clientUUID = clientRepresentationList.get(0).getId();
            return clientUUID;
        }
    }

    private static String getUserID(User user) throws Exception {
        String result = user.getUserRepresentation().getId();
        if(result != null) return result;
        return getUserID(user.getUserRepresentation().getUsername());
    }
    private static String getUserID(String userName) throws Exception{
        List<UserRepresentation> users = usersResource.search(userName);
        if(users.size() > 1){
            throw new Exception("error: more than 1 user found");
        }else if (users.size() == 0){
            throw new Exception("no users with that username found");
        } else {
            return users.get(0).getId();
        }
    }

    private static void printUserList() {
        List<UserRepresentation> users = usersResource.list();
        System.out.println("list of all users:");

        for (UserRepresentation user : users) {
            String name = user.getUsername();
            String id = user.getId();
            System.out.println("\t" + name + " id: " + id);
        }
    }

    private static void checkMandatory(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(String.format("Value is mandatory but was '%s'.", value));
        }
    }
}
