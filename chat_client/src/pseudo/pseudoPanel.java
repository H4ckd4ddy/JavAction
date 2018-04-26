package pseudo;

import launch.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

public class pseudoPanel extends JPanel implements ActionListener {

    private JLabel labelError;

    private JLabel labelPseudo;
    private JTextField inputPseudo;
    private JButton buttonOK;

    public pseudoPanel() {

        this.setLayout(null);

        //error
        labelError = new JLabel("", SwingConstants.CENTER);
        labelError.setBounds(0,0,300,20);
        labelError.setForeground(Color.red);
        labelError.setVisible(false);
        this.add(labelError);

        //pseudo
        labelPseudo = new JLabel("Pseudo : ");
        labelPseudo.setBounds(25,25,75,30);
        this.add(labelPseudo);
        inputPseudo = new JTextField(5);
        inputPseudo.setBounds(100,25,150,30);
        this.add(inputPseudo);


        //button ok
        buttonOK = new JButton("OK");
        buttonOK.setBounds(100,75,100,30);
        this.add(buttonOK);
        buttonOK.addActionListener(this);


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

        if(e.getSource() == buttonOK) {

            Pattern p = Pattern.compile("[^a-z0-9A-Z_ ]", Pattern.CASE_INSENSITIVE);

            if(inputPseudo.getText().length() < 3){
                this.error("Your pseudo must be at least 3 characters");
            }else if(inputPseudo.getText().length() > 20){
                this.error("Your pseudo must be less than 20 characters");
            }else if(p.matcher(inputPseudo.getText()).find()){
                this.error("Only letters, numbers and spaces");
            }else if(inputPseudo.getText().intern() == "System"){
                this.error("This pseudo is reserved for the system");
            }else{
                JSONObject command = new JSONObject();
                command.put("command", "setPseudo");
                command.put("pseudo", inputPseudo.getText());
                try {
                    JSONObject response = launchGUI.request(command);
                    if (response.getString("status").intern() == "OK") {
                        launchGUI.next();
                    } else {
                        this.error(response.getString("error"));
                    }
                } catch (IOException e1) {
                    this.error("Communication error");
                    e1.printStackTrace();
                } catch (GeneralSecurityException e1) {
                    this.error("Encryption error");
                    e1.printStackTrace();
                }
            }
        }

    }

}
