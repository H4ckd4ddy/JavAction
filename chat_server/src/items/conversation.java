package items;

import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class conversation {

    private int ID;
    private String name;
    private String password;
    private ArrayList<user> usersConnected;
    private ArrayList<SocketAddress> IPblacklist;

    public conversation(int ID, String name, user firstUser){
        this.ID = ID;
        this.name = name;
        this.usersConnected = new ArrayList<user>();
        this.usersConnected.add(firstUser);
        this.IPblacklist = new ArrayList<SocketAddress>();
    }

    public conversation(int ID, String name, user firstUser, String password){
        this(ID,name,firstUser);
        this.password = password;
    }

    public int getID(){
        return this.ID;
    }

    public String getName(){
        return this.name;
    }

    public Boolean getSecurity() {
        return (this.password != null);
    }

    public Boolean testIP(SocketAddress IP){
        return (java.util.Collections.frequency(this.IPblacklist, IP) >= 5);
    }

    public Boolean testPassword(String password, SocketAddress IP){
        if(!this.testIP(IP)) {
            if(!this.getSecurity()){
                return true;
            }else if(password.intern() == this.password.intern()){
                return true;
            }else{
                this.IPblacklist.add(IP);
            }
        }
        return false;
    }

    public ArrayList<user> getUsersConnected() {
        return usersConnected;
    }

    public void clearBlacklist(){
        IPblacklist.clear();
    }

    public String showBlacklist(){
        return this.IPblacklist.toString();
    }

    public void addUser(user newUser){
        this.usersConnected.add(newUser);
    }

    public void removeUser(user oldUser){
        this.usersConnected.remove(oldUser);
    }

    public int getNumberOfUsers(){
        return this.usersConnected.size();
    }

}
