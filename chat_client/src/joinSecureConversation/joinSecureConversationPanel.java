package joinSecureConversation;

import launch.*;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class joinSecureConversationPanel extends JPanel implements ActionListener {

    public int conversationID;

    private JLabel labelError;

    private JLabel labelPassword;
    private JPasswordField inputPassword;
    private JButton buttonBack;
    private JButton buttonJoin;

    public joinSecureConversationPanel() {

        this.setLayout(null);

        //error
        labelError = new JLabel("", SwingConstants.CENTER);
        labelError.setBounds(0,0,250,20);
        labelError.setForeground(Color.red);
        labelError.setVisible(false);
        this.add(labelError);

        //Password
        labelPassword = new JLabel("Password : ");
        labelPassword.setBounds(25,25,100,30);
        this.add(labelPassword);
        inputPassword = new JPasswordField(5);
        inputPassword.setBounds(100,25,100,30);
        this.add(inputPassword);


        //button back
        buttonBack = new JButton("Back");
        buttonBack.setBounds(40,75,75,30);
        this.add(buttonBack);
        buttonBack.addActionListener(this);

        //button join
        buttonJoin = new JButton("Join");
        buttonJoin.setBounds(135,75,75,30);
        this.add(buttonJoin);
        buttonJoin.addActionListener(this);


    }

    public void error(String errorText){
        labelError.setText(errorText);
        labelError.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(launchGUI.errorDisplayTime);
                labelError.setVisible(false);
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == this.buttonBack){
            launchGUI.joinSecureConversation_frame.setVisible(false);
        }else if(e.getSource() == this.buttonJoin) {
            JSONObject command = new JSONObject();
            command.put("command","joinConversation");
            command.put("conversationID",this.conversationID);
            command.put("password", String.valueOf(this.inputPassword.getPassword()));
            try {
                JSONObject response = launchGUI.request(command);
                if(response.getString("status").intern() == "OK"){
                    launchGUI.next();
                }else{
                    this.error(response.getString("error"));
                }
            } catch (IOException e1) {
                this.error("Communication error");
                //e1.printStackTrace();
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
            }
        }

    }

}
