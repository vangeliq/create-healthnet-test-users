import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

public class User {
    private UserRepresentation userRepresentation;
    private String password;
    private HashMap<String, Set<String>> clientRoles;


    public User(){
        init();
    }

    public User(String username){
        init();
        userRepresentation.setUsername(username);
    }

    public User(String username,String password){
        init();
        userRepresentation.setUsername(username);
        this.password = password;
    }

    public void init(){
        userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        clientRoles = new HashMap<>();
    }

    public UserRepresentation getUserRepresentation() {
        return userRepresentation;
    }
    public String getPassword() {
        return password;
    }
    public HashMap<String, Set<String>> getClientRoles() {
        return clientRoles;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public void recordClientRoles(String clientName,String role){
        if (!clientRoles.containsKey(clientName)){
            clientRoles.put(clientName,new HashSet<String>());
        }
        clientRoles.get(clientName).add(role);
    }



    // keycloak methods

    public void addToKeyCloak(UsersResource usersResource) {
        try {
            Response response = usersResource.create(userRepresentation);
            System.out.printf("Response: %s %s%n", response.getStatus(), response.getStatusInfo());
            System.out.println(response.getLocation());
            String userId = CreatedResponseUtil.getCreatedId(response);
            userRepresentation.setId(userId);
            System.out.printf("User created with userId: %s%n", userId);
        }catch(WebApplicationException e){
            e.printStackTrace();
        }
    }

    public void deleteFromKeycloak(UsersResource usersResource) {
        try {
            String userID = getUserID(usersResource);
            usersResource.delete(userID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserID(UsersResource usersResource) throws Exception {
        String result = userRepresentation.getId();
        if (result != null) return result;

        List<UserRepresentation> users = usersResource.search(userRepresentation.getUsername());
        if (users.size() > 1) {
            throw new Exception("error: more than 1 user found");
        } else if (users.size() == 0) {
            throw new Exception("no users with that username found");
        } else {
            return users.get(0).getId();
        }
    }

    public void addClientRolesInKeyCloak(RealmResource realmResource) {
        try{//try looking for the user
            String userID = getUserID(realmResource.users());
            for(String clientID: clientRoles.keySet()){
                try {
                    String clientUUID = getClientUUID(clientID, realmResource);

                    ClientResource clientResource = realmResource.clients().get(clientUUID);
                    List<RoleRepresentation> rolesToAdd = getRolesToAdd(clientRoles.get(clientID),clientResource);

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

    private static String getClientUUID(String clientID,RealmResource realmResource) throws Exception {
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

    private static List<RoleRepresentation> getRolesToAdd(Set<String> roles, ClientResource clientResource){// todo: test this
        List<RoleRepresentation> result = new ArrayList<>();
        RolesResource rolesResource = clientResource.roles();
        for(String roleName:roles){
            try {
                List<RoleRepresentation> roleRepresentationList = rolesResource.list(roleName, true);
                if (roleRepresentationList.size() > 1)
                    throw new Exception("multiple roles with name " + roleName + " found");
                if (roleRepresentationList.size() < 1) throw new Exception(roleName + " not found");
                result.addAll(roleRepresentationList);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }
}
