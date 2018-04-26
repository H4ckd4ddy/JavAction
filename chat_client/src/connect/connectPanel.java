package connect;

import launch.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class connectPanel extends JPanel implements ActionListener {

    private JLabel labelError;

    private JLabel labelIP;
    private JTextField inputIP;
    private JLabel labelPort;
    private JTextField inputPort;
    private JLabel labelEncryption;
    private JCheckBox checkboxEncryption;
    private JButton buttonConnect;

    public connectPanel() {

        this.setLayout(null);

        //error
        labelError = new JLabel("", SwingConstants.CENTER);
        labelError.setBounds(0,0,300,20);
        labelError.setForeground(Color.red);
        labelError.setVisible(false);
        this.add(labelError);

        //IP
        labelIP = new JLabel("IP : ");
        labelIP.setBounds(25,25,50,30);
        this.add(labelIP);
        inputIP = new JTextField(5);
        inputIP.setBounds(50,25,100,30);
        inputIP.setText("java.sellan.fr");
        this.add(inputIP);

        //port
        labelPort = new JLabel("Port : ");
        labelPort.setBounds(170,25,50,30);
        this.add(labelPort);
        inputPort = new JTextField(5);
        inputPort.setBounds(210,25,50,30);
        inputPort.setText("8000");
        this.add(inputPort);

        //encryption
        labelEncryption = new JLabel("Encryption : ");
        labelEncryption.setBounds(100,65,100,30);
        this.add(labelEncryption);
        checkboxEncryption = new JCheckBox();
        checkboxEncryption.setBounds(175,65,30,30);
        this.add(checkboxEncryption);

        //button connect
        buttonConnect = new JButton("Connexion");
        buttonConnect.setBounds(100,100,100,30);
        this.add(buttonConnect);
        buttonConnect.addActionListener(this);


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

        if(e.getSource() == this.buttonConnect) {
            launchGUI.encryptionActivation = this.checkboxEncryption.isSelected();
            try {
                launchGUI.connect(this.inputIP.getText(),this.inputPort.getText());
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
