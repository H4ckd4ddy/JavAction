package items;

import lobby.server;

import org.json.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class connexion extends user implements Runnable {

    private Socket mySocket;
    private server lobby;
    private SocketAddress IP;

    private PrintWriter out;
    private BufferedReader in;

    private KeyPair pair;
    private PublicKey clientPublicKey;

    public connexion(Socket theSocket, server theserver) throws NoSuchAlgorithmException {

        super();

        this.mySocket = theSocket;
        this.lobby = theserver;
        this.IP = this.mySocket.getRemoteSocketAddress();

        this.pair = this.generateKeyPair();

    }

    @Override
    public void run() {

        try{

            out = new PrintWriter(mySocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

            while(this.isConnected()) {

                String data = in.readLine();
                if(this.clientPublicKey != null){
                    data = decrypt(data, this.pair.getPrivate());
                }
                JSONObject requete = new JSONObject(data);
                String command = requete.getString("command");

                switch (command){
                    case "setPseudo": sendData(this.setPseudo(requete));break;
                    case "getConversations": sendData(this.getConversations());break;
                    case "addConversation": sendData(this.addConversation(requete));break;
                    case "joinConversation": sendData(this.joinConversation(requete));break;
                    case "leaveConversation": sendData(this.leaveConversation());break;
                    case "getUsersConnected": sendData(this.getUsersConnected());break;
                    case "synchronisation": sendData(this.synchronisation(requete));break;
                    case "keyExchange": out.println(this.keyExchange(requete));break;
                    case "close": sendData(this.close());break;
                    default: System.out.println(requete.toString());
                }

            }

        }catch (IOException e){
            this.close();
            //System.out.println("Error : " + e.getMessage());
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

    }

    private String close(){
        if(this.conversation != null){
            this.leaveConversation();
        }
        this.logout();
        JSONObject response = new JSONObject();
        response.put("status","OK");
        return response.toString();
    }

    private void sendData(String data) throws GeneralSecurityException, UnsupportedEncodingException {
        if(this.clientPublicKey != null){
            data = encrypt(data, this.clientPublicKey);
        }
        this.out.println(data);
    }

    private String setPseudo(JSONObject data){
        this.setPseudo(data.getString("pseudo"));
        JSONObject response = new JSONObject();
        response.put("status","OK");
        return response.toString();
    }

    private String getConversations(){

        JSONObject response = new JSONObject();
        response.put("status", "OK");
        response.put("conversations", this.lobby.getConversations());

        return response.toString();
    }

    private String addConversation(JSONObject data){

        conversation newConversation = null;

        if(data.has("password")){
            newConversation = new conversation(this.lobby.findNextID(), data.getString("name"), this, data.getString("password"));
        }else{
            newConversation = new conversation(this.lobby.findNextID(), data.getString("name"), this);
        }

        this.lobby.addConversation(newConversation);

        this.conversation = newConversation;

        JSONObject response = new JSONObject();
        response.put("status", "OK");

        return response.toString();
    }

    private String joinConversation(JSONObject data){

        JSONObject response = new JSONObject();

        if(!data.has("conversationID")){
            response.put("status", "ERROR");
            response.put("error", "Invalid conversation ID");
        }else if(this.lobby.getConversationByID(data.getInt("conversationID")) == null) {
            response.put("status", "ERROR");
            response.put("error", "Unknown conversation");
        }else if(this.lobby.getConversationByID(data.getInt("conversationID")).getSecurity()){
            if(!data.has("password")){
                response.put("status", "ERROR");
                response.put("error", "Password needed");
            }else if(this.lobby.getConversationByID(data.getInt("conversationID")).testIP(this.IP)){
                response.put("status", "ERROR");
                response.put("error", "Your are banned from this conversation");
            }else if(!this.lobby.getConversationByID(data.getInt("conversationID")).testPassword(data.getString("password"), this.IP)){
                response.put("status", "ERROR");
                response.put("error", "Wrong password");
            }
        }

        if(!response.has("error")){
            this.conversation = this.lobby.getConversationByID(data.getInt("conversationID"));
            this.conversation.addUser(this);
            for (user receiver : this.conversation.getUsersConnected()) {
                receiver.sendMessage(new message(this.getPseudo()+" join this conversation", this.lobby.systemUser));
            }
            response.put("status", "OK");
        }

        return response.toString();

    }

    private String leaveConversation() {

        JSONObject response = new JSONObject();

        if(this.conversation == null){

            response.put("status", "ERROR");
            response.put("error", "No conversation");

        }else{

            this.conversation.removeUser(this);
            if(this.conversation.getNumberOfUsers() < 1){
                this.lobby.removeConversation(this.conversation);
            }else{
                for (user receiver : this.conversation.getUsersConnected()) {
                    receiver.sendMessage(new message(this.getPseudo()+" leave this conversation", this.lobby.systemUser));
                }
            }
            this.conversation = null;
            this.mailbox.clear();

            response.put("status", "OK");

        }

        return response.toString();

    }

    private String getUsersConnected(){

        JSONObject response = new JSONObject();

        if(this.conversation == null) {

            response.put("status", "ERROR");
            response.put("error", "No conversation");

        }else {

            response.put("status", "OK");
            response.put("usersConnected", this.conversation.getUsersConnected());

        }

        return response.toString();
    }

    private String synchronisation(JSONObject data){

        JSONObject response = new JSONObject();

        if(this.conversation == null){

            response.put("status", "ERROR");
            response.put("error", "No conversation");

        }else {

            if(data.has("isTyping")){
                this.setIsTyping(data.getBoolean("isTyping"));
            }

            if(data.has("msgToSend")) {

                int numberOfMessages = data.getJSONArray("msgToSend").length();
                for (int i = 0; i < numberOfMessages; i++) {

                    message msg = new message(data.getJSONArray("msgToSend").getString(i), this);
                    if(msg.getTexte().length() > 0) {
                        if (msg.getTexte().charAt(0) == '/') {

                            String commandResponse;

                            switch (msg.getTexte()) {
                                case "/users":
                                    commandResponse = this.conversation.getUsersConnected().toString();
                                    break;
                                case "/ping":
                                    commandResponse = "Pong";
                                    break;
                                case "/showBlacklist":
                                    commandResponse = this.conversation.showBlacklist();
                                    break;
                                case "/clearBlacklist":
                                    this.conversation.clearBlacklist();
                                    commandResponse = "Blacklist is now empty";
                                    break;
                                default:
                                    commandResponse = "Command not found";
                                    break;
                            }

                            this.sendMessage(new message(commandResponse, this.lobby.systemUser));

                        } else {
                            for (user receiver : this.conversation.getUsersConnected()) {
                                receiver.sendMessage(msg);
                            }
                        }
                    }
                }
            }

            if(this.mailbox.size() > 0) {
                response.put("msgToReceive", this.mailbox);
                this.mailbox.clear();
            }

            response.put("status", "OK");

        }

        return response.toString();
    }

    private String keyExchange(JSONObject data) throws InvalidKeySpecException, NoSuchAlgorithmException {

        JSONObject response = new JSONObject();

        if(data.has("clientPublicKey")){
            this.clientPublicKey = this.Base64ToPublicKey(data.getString("clientPublicKey"));
        }

        response.put("status", "OK");

        response.put("serverPublicKey",this.publicKeyToBase64(this.pair.getPublic()));

        return response.toString();
    }


    private String privateKeyToBase64(PrivateKey thePrivateKey){
        return Base64.getEncoder().encodeToString(thePrivateKey.getEncoded());
    }
    private String publicKeyToBase64(PublicKey thePublicKey){
        return Base64.getEncoder().encodeToString(thePublicKey.getEncoded());
    }
    private PrivateKey Base64ToPrivateKey(String base64) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] privateBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(privateBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    private PublicKey Base64ToPublicKey(String base64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    private String encrypt(String message, PublicKey key) throws GeneralSecurityException, UnsupportedEncodingException {

        byte[] messageByte = message.getBytes("UTF-8");

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessageByte = cipher.doFinal(messageByte);

        byte[] encoded = Base64.getEncoder().encode(encryptedMessageByte);
        String encryptedMessage = new String(encoded);

        return encryptedMessage;
    }
    private String decrypt(String encryptedMessage, PrivateKey key) throws GeneralSecurityException, UnsupportedEncodingException {

        byte[] encryptedMessageByte = Base64.getDecoder().decode(encryptedMessage);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] messageByte = cipher.doFinal(encryptedMessageByte);

        String message = new String(messageByte, "UTF-8");

        return message;
    }
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
        return keyPairGenerator.generateKeyPair();
    }
    private PublicKey getPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
        return factory.generatePublic(encodedKeySpec);
    }

}
