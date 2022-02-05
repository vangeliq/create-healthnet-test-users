import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;

import java.io.*;

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
            configPath = "C:\\Users\\Valery\\Documents\\GitHub\\create-healthnet-test-users\\src\\main\\java\\configuration.properties";
        }
        LOG.info(String.format("Configuration file expected at '%s'.", configPath));

        init();

        for (User user : users.values()) {
            user.addToKeyCloak(usersResource);
            user.addClientRolesInKeyCloak(realmResource);
//            user.deleteFromKeycloak(usersResource);
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
        inputFile = "C:\\Users\\Valery\\Documents\\GitHub\\create-healthnet-test-users\\src\\main\\java\\data\\input.json";
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
        users = addUsersFromJSON(inputFile);
    }

    private static HashMap<String,User> addUsersFromJSON(String inputFile){
        HashMap<String,User> result = new HashMap<>();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(inputFile));
            JsonArray parser = (JsonArray) Jsoner.deserialize(reader);

            parser.forEach(entry -> {
                JsonObject jsonUser = (JsonObject) entry;
                try {
                    User user = addUserFromJSON(jsonUser);

                    if (result.containsKey(user.getUsername())) throw new Exception("duplicate username found: " + user.getUsername());
                    result.put(user.getUsername(),user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            reader.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static User addUserFromJSON(JsonObject jsonUser) throws Exception{
        String username = (String) jsonUser.get("username");
        String password = (String) jsonUser.get("password");
        if (username == null) throw new Exception("no username");

        User user = (password != null)? new User(username,password) : new User(username);

        Map<String,JsonArray> applications = (Map<String, JsonArray>) jsonUser.get("applications");
        applications.forEach((client, roles) -> roles.forEach(role -> user.recordClientRoles(client, (String) role)));

        return user;
    }

    private static void printKeycloakUserList() {
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
 