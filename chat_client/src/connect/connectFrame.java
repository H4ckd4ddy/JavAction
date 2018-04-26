package connect;

import javax.swing.*;

public class connectFrame extends JFrame {

    public connectFrame() {
        this.setSize(300, 175);
        this.setTitle("Connexion to server");
        connectPanel panel = new connectPanel();
        this.add(panel);
    }

}
