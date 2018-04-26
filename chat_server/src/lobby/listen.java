package lobby;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class listen {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        server the_server = new server();
        try {
            the_server.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

}
