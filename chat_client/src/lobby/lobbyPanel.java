package lobby;

import launch.*;
import addConversation.*;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class lobbyPanel extends JPanel implements ActionListener {

    private JLabel labelError;

    private DefaultListModel DBConversations;
    private JList listConversations;
    private JSONArray JSONConversations;

    private JButton buttonJoin;
    private JLabel labelOr;
    private JButton buttonAdd;

    public lobbyPanel() {

        this.setLayout(null);

        //error
        labelError = new JLabel("", SwingConstants.CENTER);
        labelError.setBounds(0,0,300,20);
        labelError.setForeground(Color.red);
        labelError.setVisible(false);
        this.add(labelError);

        DBConversations = new DefaultListModel();

        listConversations = new JList(DBConversations);

        listConversations.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listConversations.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        listConversations.setBounds(10,30,280,300);
        this.add(listConversations);

        JScrollPane scrollpaneConversations = new JScrollPane(listConversations);
        scrollpaneConversations.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollpaneConversations.setBounds(10,30,280,300);
        this.add(scrollpaneConversations);



        buttonJoin = new JButton("Join");
        buttonJoin.setBounds(100,335,100,30);
        this.add(buttonJoin);
        buttonJoin.addActionListener(this);

        labelOr = new JLabel("or");
        labelOr.setBounds(140,375,20,30);
        this.add(labelOr);

        buttonAdd = new JButton("Create");
        buttonAdd.setBounds(100,415,100,30);
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

    public void refresh() throws IOException, GeneralSecurityException {
        JSONObject command = new JSONObject();
        command.put("command","getConversations");

        JSONObject convs = launchGUI.request(command);

        if(convs.getString("status").intern() == "OK"){
            if(convs.has("conversations")) {
                this.DBConversations.clear();
                this.JSONConversations = new JSONArray();
                int numberOfConversations = convs.getJSONArray("conversations").length();
                for (int i = 0; i < numberOfConversations; i++) {
                    JSONObject conv = convs.getJSONArray("conversations").getJSONObject(i);
                    this.DBConversations.addElement("["+conv.getInt("numberOfUsers")+"] " + (conv.getBoolean("security")?"\uD83D\uDD12":"") + conv.getString("name"));
                    this.JSONConversations.put(conv);
                }
            }
        }else{
            this.error(convs.getString("error"));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e){

        if(e.getSource() == this.buttonJoin){
            if(this.listConversations.getSelectedIndex() >= 0){

                int index = this.listConversations.getSelectedIndex();
                int ID = this.JSONConversations.getJSONObject(index).getInt("ID");
                if(this.JSONConversations.getJSONObject(index).getBoolean("security")){
                    launchGUI.joinSecureConversation_frame.setVisible(true);
                    launchGUI.joinSecureConversation_frame.panel.conversationID = ID;
                }else{
                    JSONObject command = new JSONObject();
                    command.put("command","joinConversation");
                    command.put("conversationID", ID);

                    JSONObject response;
                    try {
                        response = launchGUI.request(command);
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
            }else{
                this.error("No selection");
            }
        }else if(e.getSource() == this.buttonAdd) {
            launchGUI.addConversation_frame.setVisible(true);
        }

    }

}
