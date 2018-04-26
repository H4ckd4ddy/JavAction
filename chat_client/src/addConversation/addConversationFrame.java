package addConversation;

import javax.swing.*;

public class addConversationFrame extends JFrame {

    public addConversationFrame() {
        this.setSize(250, 250);
        this.setTitle("Add conversation");
        addConversationPanel panel = new addConversationPanel();
        this.add(panel);
    }

}
