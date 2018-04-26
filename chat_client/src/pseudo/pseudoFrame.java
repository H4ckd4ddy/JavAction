package pseudo;

import javax.swing.*;

public class pseudoFrame extends JFrame {

    public pseudoFrame() {
        this.setSize(300, 150);
        this.setTitle("Set pseudo");
        pseudoPanel panel = new pseudoPanel();
        this.add(panel);
    }

}
