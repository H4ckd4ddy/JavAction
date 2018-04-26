package addConversation;

import launch.*;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class addConversationPanel extends JPanel implements ActionListener {

    private JLabel labelError;

    private JLabel labelName;
    private JTextField inputName;
    private JLabel labelSecurity;
    private JCheckBox checkboxSecurity;
    private JLabel labelPassword;
    private JPasswordField inputPassword;
    private JButton buttonBack;
    private JButton buttonAdd;

    public addConversationPanel() {

        this.setLayout(null);

        //error
        labelError = new JLabel("", SwingConstants.CENTER);
        labelError.setBounds(0,0,250,20);
        labelError.setForeground(Color.red);
        labelError.setVisible(false);
        this.add(labelError);

        //Name
        labelName = new JLabel("Name : ");
        labelName.setBounds(25,25,100,30);
        this.add(labelName);
        inputName = new JTextField(5);
        inputName.setBounds(100,25,100,30);
        this.add(inputName);

        //Security
        labelSecurity = new JLabel("Security : ");
        labelSecurity.setBounds(25,75,100,30);
        this.add(labelSecurity);
        checkboxSecurity = new JCheckBox();
        checkboxSecurity.setBounds(135,75,30,30);
        this.add(checkboxSecurity);
        checkboxSecurity.addActionListener(this);


        //Password
        labelPassword = new JLabel("Password : ");
        labelPassword.setBounds(25,125,100,30);
        labelPassword.setVisible(false);
        this.add(labelPassword);
        inputPassword = new JPasswordField(5);
        inputPassword.setBounds(100,125,100,30);
        inputPassword.setVisible(false);
        this.add(inputPassword);


        //button back
        buttonBack = new JButton("Back");
        buttonBack.setBounds(40,175,75,30);
        this.add(buttonBack);
        buttonBack.addActionListener(this);

        //button connect
        buttonAdd = new JButton("Create");
        buttonAdd.setBounds(135,175,75,30);
        this.add(buttonAdd);
        buttonAdd.addActionListener(this);


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
            launchGUI.addConversation_frame.setVisible(false);
        }else if(e.getSource() == this.buttonAdd) {
            JSONObject command = new JSONObject();
            command.put("command","addConversation");
            command.put("name",inputName.getText());
            if(this.checkboxSecurity.isSelected()){
                command.put("password", String.valueOf(this.inputPassword.getPassword()));
            }
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
        }else if(e.getSource() == this.checkboxSecurity){
            this.labelPassword.setVisible(this.checkboxSecurity.isSelected());
            this.inputPassword.setVisible(this.checkboxSecurity.isSelected());
        }

    }

}
