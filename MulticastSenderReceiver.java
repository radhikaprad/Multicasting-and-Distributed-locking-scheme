
import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

// Global counter(Time) for the process.

class Global{
    static int counter;
}
class sendThread extends Thread{
    private InetAddress group;
    private int multicastPort;
    private String message;
    private int processID;
    sendThread(InetAddress g, String msg, int port){
        group = g;
        message = msg;
        multicastPort = port;
        
    }
    public void run(){
        try{
            //Hit enter when all the processes are ready to recieve the message
            System.out.println("Hit return to send message");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            bufferedReader.readLine();

            //Timestamping the message
            message =  message + " " + Global.counter;
            MulticastSocket sendSocket = new MulticastSocket();
            byte[] msg = message.getBytes();

            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, multicastPort);
            
            System.out.println("Message Sent!");

            sendSocket.send(packet);
            
            sendSocket.close();

        }catch(IOException e){
            e.printStackTrace();

        }
    }
}
class readThread extends Thread{
    private InetAddress group;
    private int multicastPort;
    private int MAX_MSG_LEN = 100;
    private int mProcessID;
    private ArrayList<String> msgList = new ArrayList<>();
    readThread(InetAddress g, int port, int pid){
        group = g;
        multicastPort = port;
        mProcessID = pid;
    }

    public void run(){
        try {

            MulticastSocket readSocket = new MulticastSocket(multicastPort);
            readSocket.joinGroup(group);
            int processID,oCounter;
            String mMessage;

            while (true){
                byte[] message = new byte[MAX_MSG_LEN];

                //Reading from the multicast port.

                DatagramPacket packet = new DatagramPacket(message, message.length, group, multicastPort);
                readSocket.receive(packet);
                String msg = new String(packet.getData());

                //Temporary list to hold the message.
                List<String> holderList = Arrays.asList(msg.split(" "));
                processID = Integer.parseInt(holderList.get(0).trim());
                mMessage = holderList.get(1);
                oCounter = Integer.parseInt(holderList.get(2).trim());

                //Preventing this process from reading its own message

                if(processID != mProcessID){
                    if(Global.counter < oCounter){
                        Global.counter = oCounter + 1;
                        msgList.add(mMessage + " " + Global.counter);
                         
                    }else{
                        Global.counter = Global.counter + 1;
                        msgList.add(mMessage + " " + Global.counter);
                         
                    }

                    //Printng to the application layer after recieving all the messages.

                    if(msgList.size() == 2){
                        for(String m : msgList){

                            //Prints message and time
                            
                            System.out.println(m);
                        }
                        break;
                    }
                    // System.out.println(processID+" , "+mMessage+" , "+oCounter);
                }
                

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class MulticastSenderReceiver {
    public static void main(String[] args){
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");

            //Initializing the multicast port and the group where all the processes will be connected.

            int multicastPort = 12345;
            InetAddress group = InetAddress.getByName("225.0.0.37");
        
            //Generating a random clock value

            Random random = new Random();
            Global.counter = random.nextInt(10) + 1;
            int processID = Integer.parseInt(args[0]);

            System.out.println("Initial System Time: " + Global.counter);

            //Receiver Thread
            readThread rt = new readThread(group, multicastPort,processID);
            rt.start();

            String message = args[1];
            
            //Message to be multicasted

            String multicastMessage = processID + " " + message;

            //Sender Thread

            sendThread st = new sendThread(group,multicastMessage, multicastPort);
            st.start();
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
