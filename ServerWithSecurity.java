import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ServerWithSecurity {
    private String my_cert; //Where is the server's certificate stored?
    private int my_Port_Num;
    private ServerSocket serverSocket;
    private Socket socket_To_Client;
    private sendFiles file_Sender;
    private receiveFiles file_Getter;
    private String their_cert_location;
    private String privateKey_loc;
    //"C:/Users/User/Desktop/Server/Bob_Cert.crt" my public cert is stored here

    public ServerWithSecurity(String My_CERT, int myPortNum, String privateKey){
	    this.my_cert = My_CERT;
	    this.my_Port_Num = myPortNum;
	    this.privateKey_loc = privateKey;
	    try{
	        this.serverSocket = new ServerSocket(this.my_Port_Num);
        }
        catch(IOException ex){
	        ex.printStackTrace();
        }
    }


	public void start() {
		try {
			this.socket_To_Client= this.serverSocket.accept();
            this.file_Sender = new sendFiles(this.socket_To_Client);
            this.file_Getter = new receiveFiles(this.socket_To_Client);
            //wait until you've gotten connected....
		} catch (Exception e) {
		    e.printStackTrace();
		}
        System.out.println("got a connection! - server");

	}

	public void receieve_file_with_SERVER_PrivateKey(){
        if (this.their_cert_location==null){return;} //cancel if you don't have their cert.
        System.out.println("Attempting to send file Encrypted with MY private Key");
        this.file_Getter.recieveEncryptedWith_public("Serverreceived/",this.privateKey_loc);


    }
    public void sendplaincert(){
        System.out.println("Attempting to send plain certificate...");
        this.file_Sender.sendPlainFile(this.my_cert,1024);
        System.out.println("Completed Cert sending Attempt");
    }
    public void verify_Certs(String their_identity){
        System.out.println("Attempting to verify Cert is from: " + their_identity);
        certVerifier verifier = new certVerifier(this.socket_To_Client,their_cert_location);
        boolean ve = verifier.verify_is_person(their_identity);
        System.out.println("IS INDEED FROM: " + their_identity+ " - " + ve);
        boolean verified = verifier.verify_Cert_and_Message();
        System.out.println("Verified sender has private key to this cert :" + verified);

        System.out.println("Sending Encrypted message..");
        verifier.send_Encrypted_Message(this.privateKey_loc);
        System.out.println("Completed sending of encrypted message.");
        if(!verified || !ve){
            try{this.socket_To_Client.close();}
            catch(IOException ex){
                System.out.println("ERROR IN VERIFICATION");
            }
        }
        clean_Streams();
        try{
            TimeUnit.MILLISECONDS.sleep(100);
        }
        catch(InterruptedException ex){
            System.out.println("Interrupted...?");
            ex.printStackTrace();
        }
        System.out.println("Mutual Verification Phase is completed.");
    }
    public void receivecert(){ //takes in the argument of whose identity it is. either "ALICE" or "BOB"
        System.out.println("Attempting to receive certificate");
        String their_cert_location = this.file_Getter.recievePlainFile("Serverreceived/");
        System.out.println("Completed Receiving!");
        this.their_cert_location=their_cert_location;
    }
    public void clean_Streams(){
        try{
            DataInputStream in = new DataInputStream(this.socket_To_Client.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket_To_Client.getOutputStream());
            out.flush();
            TimeUnit.MILLISECONDS.sleep(10);
            in.skipBytes(in.available());
        }
        catch(InterruptedException ex){
            System.out.println("INTERRUPTED");
            ex.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("IOEXCEPTION CLEANING STREAM");
            ex.printStackTrace();
        }
    }



}
