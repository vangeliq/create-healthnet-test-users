import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

// model, only has basic methods like getters/setters
public class UserList implements Iterable<User> {

    private static HashMap<String,User> users;

    /**
     * constructor
     */
    public UserList() {
        users = new HashMap<>();
    }

    // GETTERS
    public  HashMap<String, User> getUsers() {
        return users;
    }

    // SETTERS
    public void addUser(User user) {
            users.put(user.getUsername(),user);
    }

    public void removeUser(User user){
        if(users.containsKey(user.getUsername())){
            users.remove(user.getUsername());
        }else{
            System.out.println("No user with that username found");
        }
    }


    /**
     * @param username
     * @return true if userList has the specified user
     */
    public boolean contains(String username){
        return users.containsKey(username);
    }


    @Override
    public Iterator<User> iterator() {
        return users.values().iterator();
    }

    @Override
    public void forEach(Consumer action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator spliterator() {
        return Iterable.super.spliterator();
    }
}
