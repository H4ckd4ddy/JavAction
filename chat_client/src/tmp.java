import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import org.json.*;

public class tmp {

    static Socket skt;
    static PrintWriter out;
    static BufferedReader in;

    public static void main(String[] args) throws IOException {


        Scanner keyboard = new Scanner(System.in);
        //String name = keyboard.next();*/


        skt = new Socket("127.0.0.1", 8000);
        out = new PrintWriter(skt.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(skt.getInputStream()));

        while (true) {

            System.out.print("-> ");
            String texte = keyboard.next();
            out.println(texte);
            String response = in.readLine();
            System.out.println(response);

        }
        //{"command":"addConversation","name":"A"}
        //{"command":"synchronisation","msgToSend":["Hello","world","!!!"]}
        //{"command":"getUsersConnected"}

        //{"command":"joinConversation","conversationID":1}
        //{"command":"leaveConversation"}
        //{"command":"getConversations"}

    }

    /*public JSONObject send_command(JSONObject){

    }*/

}
