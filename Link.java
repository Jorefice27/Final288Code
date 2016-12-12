package pgk28gui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;


/**
 *
 * @author John
 */
public class Link {
    
    private Socket link;
    private OutputStreamWriter out;
    private InputStreamReader isr;
    private BufferedReader in;
    private final String SERVER_NAME = "192.168.1.1";
    private final int PORT_NUM = 42880;
    
    public Link() throws IOException
    {
        //Create a connection with the bot
        link = new Socket(SERVER_NAME, PORT_NUM);
        //OSW to send messages to bot
        out = new OutputStreamWriter(new BufferedOutputStream(link.getOutputStream()));
        isr = new InputStreamReader(link.getInputStream());
        //BR to read messages from bot
        in = new BufferedReader(isr);

        System.out.println("Sent");       
        
    }
    
    //Sends a one byte instruction to the bot
    public boolean sendByte(char c)
    {
        try {
            out.write(c);    
            out.flush();
            return true;
        } 
        catch (IOException ex) 
        {
            
            return false;
        }
    }
    
    //Recieves a message from the bot as a string
    public String recieveMessage() throws IOException
    {
        return in.readLine();
    }
    
    //Returns true if a connecttion to the bot exists
    public boolean isConnected()
    {
        return link.isConnected();
    }
}
