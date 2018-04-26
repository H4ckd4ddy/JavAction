package lobby;

import items.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;

public class server {

    public user systemUser = new user(true);

    private ArrayList<user> users;
    private ArrayList<conversation> conversations = new ArrayList<conversation>();
    private int conversation_I = 0;

    public void start() throws IOException, NoSuchAlgorithmException {
        ServerSocket lobby = new ServerSocket(8000);

        while(true){

            Socket mySocket = lobby.accept();
            try {
                new Thread(new connexion(mySocket, this)).start();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }
    }

    public ArrayList<conversation> getConversations() {
        return conversations;
    }

    public conversation getConversationByID(int ID){
        for(conversation conv:this.conversations){
            if (conv.getID() == ID){
                return conv;
            }
        }
        return null;
    }

    public void addConversation(conversation newConversation){
        this.conversations.add(newConversation);
    }

    public void removeConversation(conversation oldConversation){
        this.conversations.remove(oldConversation);
    }

    public int findNextID(){
        this.conversation_I++;
        return this.conversation_I;
    }
}
