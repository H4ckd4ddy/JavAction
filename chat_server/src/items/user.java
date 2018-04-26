package items;

import java.util.ArrayList;

public class user {

    private String pseudo;
    private String color;
    private Boolean connected;
    protected conversation conversation;
    private Boolean isTyping;
    protected ArrayList<message> mailbox;

    public user(){
        this.pseudo = "Anonymous";
        this.generateColor();
        this.connected = true;
        this.isTyping = false;
        this.mailbox = new ArrayList<message>();
    }

    public user(Boolean system){
        if(system){
            this.pseudo = "System";
            this.color = "#ff0000";
            this.connected = true;
            this.isTyping = false;
        }
    }

    public String getPseudo() {
        return this.pseudo;
    }

    public Boolean getIsTyping(){
        return this.isTyping;
    }

    protected void setIsTyping(Boolean typing){
        this.isTyping = typing;
    }

    protected void setPseudo(String pseudo){
        this.pseudo = pseudo;
        this.generateColor();
    }

    private void generateColor(){
        String tmpColor = "#";
        String letters = "abcedfghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_ ";
        String hex = "0123456789abcdef";
        for(int i = 0; i < 3; i++){
            int position = letters.indexOf(this.pseudo.charAt(i));
            while(position > hex.length()){
                position -= hex.length();
            }
            tmpColor += hex.charAt(position);
            tmpColor += hex.charAt(position);
        }
        this.color = tmpColor;
    }

    public String getColor(){
        return this.color;
    }

    public Boolean isConnected(){
        return this.connected;
    }

    protected void sendMessage(message theMessage){
        this.mailbox.add(theMessage);
    }

    protected boolean logout(){
        this.connected = false;
        return true;
    }

}
