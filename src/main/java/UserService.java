import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;



public class UserService {
    private UserList userList;

    private String inputFile;
    private Keycloak keycloak;
    private RealmResource realmResource;
    private UsersResource usersResource;


    /**
     * constructor for UserService
     * @param configPath the path to the configuration file
     */
    public UserService(String configPath){
        userList = new UserList();
        try {
            setConfigurations(configPath);
        }catch (IOException e){
            e.printStackTrace();
        }
        addUsersFromJSON();
        for(User user: userList){
            System.out.println(user.getUsername());
        }
    }

    /**
     * basic getter
     * @return userList
     */
    public UserList getUserList() {
        return userList;
    }


    /**
     adds a user to the UserList  model
     @param user user to add
     **/
    public void addToUserList(User user){
        userList.addUser(user);
    }

    /**
     * sets up the inputFile path and keycloak. Also sets up the RealmResource
     * and its corresponding usersResource.
     * @param configPath
     * @throws IOException
     */
    public void setConfigurations(String configPath) throws IOException {
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
        inputFile = "C:\\Users\\Valery\\Documents\\GitHub\\create-healthnet-test-users\\src\\main\\java\\data\\input.json";
        String serverURL = configProperties.getProperty("serverURL");
        checkMandatory(serverURL);
        String realm = configProperties.getProperty("realm");
        checkMandatory(realm);
        String clientID = configProperties.getProperty("clientID");
        checkMandatory(clientID);
        String clientSecret = configProperties.getProperty("clientSecret");
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
    }

    /**
     * checks whether or not String is blank, and throws
     * @param value
     */
    private static void checkMandatory(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(String.format("Value is mandatory but was '%s'.", value));
        }
    }

    // JSON methods

    /**
     * adds all users from the specified inputFile to the userList
     */
    public void addUsersFromJSON(){
        try {
            Reader reader = Files.newBufferedReader(Paths.get(inputFile));
            JsonArray parser = (JsonArray) Jsoner.deserialize(reader);

            parser.forEach(entry -> {
                JsonObject jsonUser = (JsonObject) entry;
                try {
                    User user = getUserFromJSON(jsonUser);

                    if (userList.contains(user.getUsername())) throw new Exception("duplicate username found: " + user.getUsername());
                    userList.addUser(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Makes a user out of a JSON object
     * @param jsonUser
     * @return a User
     * @throws Exception when no username is found
     */
    private User getUserFromJSON(JsonObject jsonUser) throws Exception{
        String username = (String) jsonUser.get("username");
        String password = (String) jsonUser.get("password");
        if (username == null) throw new Exception("no username");

        User user = (password != null)? new User(username,password) : new User(username);

        Map<String,JsonArray> applications = (Map<String, JsonArray>) jsonUser.get("applications");
        applications.forEach((client, roles) -> roles.forEach(role -> user.recordClientRoles(client, (String) role)));

        return user;
    }

    // keycloak methods

    /**
     * adds all the Users in UserList to keycloak
     */
    public void addAllToKeyCloak(){
        for (User user: userList){
            addToKeyCloak(user.getUserRepresentation());
        }
    }

    /**
     * adds a single user to Keycloak.
     * @param userRepresentation
     */
    public void addToKeyCloak(UserRepresentation userRepresentation) {
        try {
            Response response = usersResource.create(userRepresentation);
            System.out.printf("Response: %s %s%n", response.getStatus(), response.getStatusInfo());
            System.out.println(response.getLocation());
            String userId = CreatedResponseUtil.getCreatedId(response);
            userRepresentation.setId(userId);
            System.out.printf(userRepresentation.getUsername() + " created with userId: %s%n", userId);
        }catch(WebApplicationException e){
            e.printStackTrace();
        }
    }


    /**
     * deletes all users in UserList from keycloak
     */
    public void deleteAllFromKeyCloak(){
        for (User user: userList){
            deleteFromKeycloak(user);
        }
    }

    /**
     * deletes specified used from Keycloak
     * @param user
     */
    public void deleteFromKeycloak(User user ) {
        try {
            String userID = getUserID(user);
            usersResource.delete(userID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * adds all the client roles of all the users in UserList to Keycloak
     */
    public void addAllClientRolesInKeyCloak(){
        for (User user: userList){
            addClientRolesInKeyCloak(user);
        }    }

    /**
     * adds all of the specified user's client roles to Keycloak
     * @param user
     */
    public void addClientRolesInKeyCloak(User user) {
        try{//try looking for the user
            String userID = getUserID(user);
            for(String clientID: user.getClientIDs()){
                try {
                    String clientUUID = getClientUUID(clientID);

                    ClientResource clientResource = realmResource.clients().get(clientUUID);
                    List<RoleRepresentation> rolesToAdd = getRolesToAdd(user.getClientRoles(clientID) ,clientResource);

                    UserResource userResource = realmResource.users().get(userID);
                    RoleScopeResource roleScopeResource = userResource.roles().clientLevel(clientUUID);

                    roleScopeResource.add(rolesToAdd);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * gets the userID of the user
     * @param user
     * @return String representation of the UserID
     * @throws Exception if multiple usernames/no usernames found
     */
    public String getUserID(User user) throws Exception {
        String result = user.getUserRepresentation().getId();
        if (result != null) return result;

        List<UserRepresentation> users = usersResource.search(user.getUserRepresentation().getUsername());
        if (users.size() > 1) {
            throw new Exception("error: more than 1 user found");
        } else if (users.size() == 0) {
            throw new Exception("no users with that username found");
        } else {
            return users.get(0).getId();
        }
    }

    /**
     * gets the Client's UUID from a clientID
     * @param clientID
     * @return String representation of the clientUUID
     * @throws Exception when multiple clients or no clients found
     */
    public String getClientUUID(String clientID) throws Exception {
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


    /**
     * converts a collection of role names
     * to a collection of its RoleRepresentations
     * @param roles a set of role names to add
     * @param clientResource Keycloak API's interface for the current client
     * @return a list of RoleRepresentations
     */
    public List<RoleRepresentation> getRolesToAdd(Set<String> roles, ClientResource clientResource){
        List<RoleRepresentation> result = new ArrayList<>();
        RolesResource rolesResource = clientResource.roles();
        for(String roleName:roles){
            try {
                RoleRepresentation roleRepresentationList = rolesResource.get(roleName).toRepresentation();
                result.add(roleRepresentationList);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }
}

