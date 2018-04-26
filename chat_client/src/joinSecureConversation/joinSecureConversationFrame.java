package joinSecureConversation;

import launch.*;

import javax.swing.*;

public class joinSecureConversationFrame extends JFrame {

    public joinSecureConversationPanel panel;

    public joinSecureConversationFrame() {
        this.setSize(250, 150);
        this.setTitle("Join secure conversation");
        this.panel = new joinSecureConversationPanel();
        this.add(this.panel);
    }

}
