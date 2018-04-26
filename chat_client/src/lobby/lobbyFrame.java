package lobby;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;

public class lobbyFrame extends JFrame {

    private lobbyPanel panel;

    public lobbyFrame() {
        this.setSize(300, 500);
        this.setTitle("Lobby");
        panel = new lobbyPanel();
        this.add(panel);
    }

    public ComponentListener listener = new ComponentAdapter() {

        Timer timer;

        public void componentShown(ComponentEvent evt) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        panel.refresh();
                    } catch (IOException e) {
                        panel.error("Communication error");
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        panel.error("Encryption error");
                        e.printStackTrace();
                    }
                }
            },1,10000);
        }

        public void componentHidden(ComponentEvent evt) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    };

}
