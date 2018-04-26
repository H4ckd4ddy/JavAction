package conversation;

import launch.launchGUI;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class conversationPanel extends JPanel implements ActionListener {

    private JTextPane theConversation;
    private HTMLEditorKit kit;
    private HTMLDocument doc;

    private ArrayList<String> msgToSend;
    private JButton buttonSend;

    private JTextField inputMsg;
    private JButton buttonBack;

    public conversationPanel() throws IOException, BadLocationException {

        this.setLayout(null);

        msgToSend = new ArrayList<String>();

        theConversation = new JTextPane();
        theConversation.setBounds(10,30,480,500);
        theConversation.setContentType("text/html");
        theConversation.setEditable(false);
        kit = new HTMLEditorKit();
        doc = new HTMLDocument();
        theConversation.setEditorKit(kit);
        theConversation.setDocument(doc);
        kit.insertHTML(doc, doc.getLength(), "<br/>", 0, 0, null);
        this.add(theConversation);

        //send
        inputMsg = new JTextField(5);
        inputMsg.setBounds(150,600,250,30);
        this.add(inputMsg);
        buttonSend = new JButton("Send");
        buttonSend.setBounds(425,600,75,30);
        this.add(buttonSend);
        buttonSend.addActionListener(this);

        //button back
        buttonBack = new JButton("Back");
        buttonBack.setBounds(25,600,75,30);
        this.add(buttonBack);
        buttonBack.addActionListener(this);

    }

    public void synchronisation() throws IOException, BadLocationException, GeneralSecurityException {
        JSONObject command = new JSONObject();
        command.put("command","synchronisation");
        if(this.msgToSend != null){
            command.put("msgToSend", this.msgToSend);
            this.msgToSend.clear();
        }

        JSONObject response = launchGUI.request(command);
        if(response.getString("status").intern() == "OK"){
            if(response.has("msgToReceive")) {
                int numberOfMessages = response.getJSONArray("msgToReceive").length();
                JSONArray msgs = response.getJSONArray("msgToReceive");
                for (int i = 0; i < numberOfMessages; i++) {
                    JSONObject msg = msgs.getJSONObject(i);
                    JSONObject sender = msg.getJSONObject("sender");
                    String html = ("<span style='color: "+sender.getString("color")+";'>"+sender.getString("pseudo")+" : </span>"+msg.getString("texte")+"<br/>");
                    this.kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
                }
            }
        }else{

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == this.buttonBack){
            JSONObject command = new JSONObject();
            command.put("command","leaveConversation");
            try {
                JSONObject response = launchGUI.request(command);
                if(response.getString("status").intern() == "OK"){
                    this.theConversation.setText("");
                    launchGUI.back();
                }else{
                    //error
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
            }
        }else if(e.getSource() == this.buttonSend) {
            msgToSend.add(inputMsg.getText());
            inputMsg.setText("");
            try {
                synchronisation();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
            }
        }

    }

}
