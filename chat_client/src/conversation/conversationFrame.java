package conversation;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Timer;
import java.util.TimerTask;

public class conversationFrame extends JFrame {

    private conversationPanel panel;

    public conversationFrame() throws IOException, BadLocationException {
        this.setSize(500, 750);
        this.setTitle("Conversation");
        try {
            this.panel = new conversationPanel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        this.add(this.panel);
    }

    public ComponentListener listener = new ComponentAdapter() {

        java.util.Timer timer;

        public void componentShown(ComponentEvent evt) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        panel.synchronisation();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            },1,1000);
        }

        public void componentHidden(ComponentEvent evt) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    };

}
