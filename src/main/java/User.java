import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;


public class User{
    private UserRepresentation userRepresentation;
    private String password;
    private HashMap<String, Set<String>> clientRoles;

    /**
     * constructor
     * @param username
     */
    public User(String username) {
        init();
        userRepresentation.setUsername(username);
    }

    /**
     * constructor
     * @param username
     */
    public User(String username, String password) {
        init();
        userRepresentation.setUsername(username);
        this.password = password;
    }

    /**
     * constructor helper.
     */
    public void init() {
        userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        clientRoles = new HashMap<>();
    }

    // GETTERS

    public UserRepresentation getUserRepresentation() {
        return userRepresentation;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return userRepresentation.getUsername();
    }

    /**
     * @return a collection of all the clients the user has roles in.
     */
    public Set<String> getClientIDs(){
        return clientRoles.keySet();
    }

    /**
     * @param clientID
     * @return returns a collection of user's roles in a specified client
     */
    public Set<String> getClientRoles(String clientID){
        return clientRoles.get(clientID);
    }

    /**
     * basic clientRoles getter
     */
    public HashMap<String, Set<String>> getClientRoles() {
        return clientRoles;
    }


//    SETTERS

    /**
     * sets password of this user
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * add a client Role to the User
     * @param clientName
     * @param role
     */
    public void recordClientRoles(String clientName, String role) {
        if (!clientRoles.containsKey(clientName)) {
            clientRoles.put(clientName, new HashSet<String>());
        }
        clientRoles.get(clientName).add(role);
    }
}

