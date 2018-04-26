package launch;

import connect.*;
import pseudo.*;
import lobby.*;
import addConversation.*;
import joinSecureConversation.*;
import conversation.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import org.json.*;

import javax.crypto.Cipher;
import javax.swing.text.BadLocationException;
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
import java.util.Base64;

public class launchGUI {

    public static Boolean encryptionActivation = true;

    private static KeyPair pair;
    private static PublicKey serverPublicKey;

    public static int status = 1;

    public static int errorDisplayTime = 3000;

    private static connectFrame connect_frame;
    private static pseudoFrame pseudo_frame;
    private static lobbyFrame lobby_frame;
    public static addConversationFrame addConversation_frame;
    public static joinSecureConversationFrame joinSecureConversation_frame;
    public static conversationFrame conversation_frame;

    private static Socket skt;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) throws NoSuchAlgorithmException, BadLocationException, IOException {

        launchGUI.pair = launchGUI.generateKeyPair();

        launchGUI.connect_frame = new connectFrame();
        launchGUI.connect_frame.setLocationRelativeTo(null);
        launchGUI.connect_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        launchGUI.connect_frame.setVisible(true);

        launchGUI.pseudo_frame = new pseudoFrame();
        launchGUI.pseudo_frame.setLocationRelativeTo(null);
        launchGUI.pseudo_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        launchGUI.pseudo_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                launchGUI.exit();
            }
        });
        launchGUI.pseudo_frame.setVisible(false);

        launchGUI.lobby_frame = new lobbyFrame();
        launchGUI.lobby_frame.setLocationRelativeTo(null);
        launchGUI.lobby_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        launchGUI.lobby_frame.addComponentListener(launchGUI.lobby_frame.listener);
        launchGUI.lobby_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                launchGUI.exit();
            }
        });
        launchGUI.lobby_frame.setVisible(false);

        launchGUI.addConversation_frame = new addConversationFrame();
        launchGUI.addConversation_frame.setLocationRelativeTo(null);
        launchGUI.addConversation_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        launchGUI.addConversation_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                launchGUI.refreshStatus();
            }
        });
        launchGUI.addConversation_frame.setVisible(false);

        launchGUI.joinSecureConversation_frame = new joinSecureConversationFrame();
        launchGUI.joinSecureConversation_frame.setLocationRelativeTo(null);
        launchGUI.joinSecureConversation_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        launchGUI.joinSecureConversation_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                launchGUI.refreshStatus();
            }
        });
        launchGUI.joinSecureConversation_frame.setVisible(false);

        launchGUI.conversation_frame = new conversationFrame();
        launchGUI.conversation_frame.setLocationRelativeTo(null);
        launchGUI.conversation_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        launchGUI.conversation_frame.addComponentListener(launchGUI.conversation_frame.listener);
        launchGUI.conversation_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                launchGUI.exit();
            }
        });
        launchGUI.conversation_frame.setVisible(false);

    }

    public static void exit(){
        JSONObject command = new JSONObject();
        command.put("command","close");

        JSONObject response;
        try {
            response = launchGUI.request(command);
            if(response.getString("status").intern() == "OK"){
                System.exit(0);
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void next(){
        launchGUI.status++;
        launchGUI.refreshStatus();
    }

    public static void back(){
        launchGUI.status--;
        launchGUI.refreshStatus();
    }

    public static void refreshStatus(){

        launchGUI.connect_frame.setVisible(false);
        launchGUI.pseudo_frame.setVisible(false);
        launchGUI.lobby_frame.setVisible(false);
        launchGUI.addConversation_frame.setVisible(false);
        launchGUI.joinSecureConversation_frame.setVisible(false);
        launchGUI.conversation_frame.setVisible(false);

        switch(launchGUI.status){
            case 0: System.exit(0);break;
            case 1: launchGUI.connect_frame.setVisible(true);break;
            case 2: launchGUI.pseudo_frame.setVisible(true);break;
            case 3: launchGUI.lobby_frame.setVisible(true);break;
            case 4: launchGUI.conversation_frame.setVisible(true);break;
            default: System.out.println("error");
        }
    }

    public static void connect(String IP, String port) throws IOException, GeneralSecurityException {
        launchGUI.skt = new Socket(IP, 8000);
        launchGUI.out = new PrintWriter(launchGUI.skt.getOutputStream(), true);
        launchGUI.in = new BufferedReader(new InputStreamReader(launchGUI.skt.getInputStream()));
        if(launchGUI.encryptionActivation) {
            launchGUI.keyExchange();
        }
        launchGUI.next();
    }

    public static JSONObject request(JSONObject data) throws IOException, GeneralSecurityException {

        String stringData = data.toString();
        if(launchGUI.serverPublicKey != null){
            stringData = encrypt(stringData,launchGUI.serverPublicKey);
        }
        launchGUI.out.println(stringData);

        String stringResponse = launchGUI.in.readLine();
        if(launchGUI.serverPublicKey != null){
            stringResponse = decrypt(stringResponse,launchGUI.pair.getPrivate());
        }
        JSONObject response = new JSONObject(stringResponse);

        return response;

    }

    private static void keyExchange() throws IOException, GeneralSecurityException {
        JSONObject command = new JSONObject();
        command.put("command", "keyExchange");
        command.put("clientPublicKey", launchGUI.publicKeyToBase64(launchGUI.pair.getPublic()));
        JSONObject response = launchGUI.request(command);
        if(response.getString("status").intern() == "OK"){
            launchGUI.serverPublicKey = launchGUI.Base64ToPublicKey(response.getString("serverPublicKey"));
        }
    }

    private static String privateKeyToBase64(PrivateKey thePrivateKey){
        return Base64.getEncoder().encodeToString(thePrivateKey.getEncoded());
    }
    private static String publicKeyToBase64(PublicKey thePublicKey){
        return Base64.getEncoder().encodeToString(thePublicKey.getEncoded());
    }
    private static PrivateKey Base64ToPrivateKey(String base64) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] privateBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(privateBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    private static PublicKey Base64ToPublicKey(String base64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicBytes = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    private static String encrypt(String message, PublicKey key) throws GeneralSecurityException, UnsupportedEncodingException {

        byte[] messageByte = message.getBytes("UTF-8");

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessageByte = cipher.doFinal(messageByte);

        byte[] encoded = Base64.getEncoder().encode(encryptedMessageByte);
        String encryptedMessage = new String(encoded);

        return encryptedMessage;
    }
    private static String decrypt(String encryptedMessage, PrivateKey key) throws GeneralSecurityException, UnsupportedEncodingException {

        byte[] encryptedMessageByte = Base64.getDecoder().decode(encryptedMessage);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] messageByte = cipher.doFinal(encryptedMessageByte);

        String message = new String(messageByte, "UTF-8");

        return message;
    }
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, SecureRandom.getInstance("SHA1PRNG"));
        return keyPairGenerator.generateKeyPair();
    }
    private static PublicKey getPublicKey(byte[] encodedKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
        return factory.generatePublic(encodedKeySpec);
    }

}