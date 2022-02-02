import org.keycloak.representations.idm.UserRepresentation;

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
        clientRoles = new HashMap<String,Set<String>>();
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
    public void addClientRoles(String clientName,String role){
        if (!clientRoles.containsKey(clientName)){
            clientRoles.put(clientName,new HashSet<String>());
        }
        clientRoles.get(clientName).add(role);
    }

    public void removeClientRoles(String clientName,String role){
        System.out.println("this isnt implemented yet");
//        todo: implement this
    }
}
