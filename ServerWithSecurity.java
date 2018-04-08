import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
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
    private Key Session_Key;
    private boolean instantender=false; //quick boolean flag for instantly killing the program, preventing any more communication if any check is failed.
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
            this.instantender=true; //there is no point running anything else if i can't make a socket...
		}
        System.out.println("Connected to Client");

	}
    public void receiveFile(){ //takes in the argument of whose identity it is. either "ALICE" or "BOB"
        if(instantender){return;} //instakill if some step beforehand was failed.
        System.out.println("Attempting to receive a file");
        String filename = this.file_Getter.recievePlainFile("Serverreceived/");
        if(their_cert_location ==null){//the file didn't come through...
            this.instantender=true;
            System.out.println("Something went wrong while receiving the other party's file");
            return;
        }
        System.out.println("Completed Receiving file: " + filename);
    }



    public void receieve_file_with_AES(){
        if(instantender){return;} //instakill if some step was failed.


        if (this.their_cert_location==null){return;} //cancel if you don't have their cert.
        System.out.println("Attempting to receive file Encrypted with AES Key");
        this.file_Getter.recieveEncryptedWith_AES("Serverreceived/",this.Session_Key);
        System.out.println("Completed reception of file.");
    }


	public void receieve_file_with_SERVER_PrivateKey(){
        if(instantender){return;} //instakill if some step was failed.


        if (this.their_cert_location==null){return;} //cancel if you don't have their cert.
        System.out.println("Attempting to send file Encrypted with MY private Key");
        String a = this.file_Getter.recieveEncryptedWith_public("Serverreceived/",this.privateKey_loc);
        System.out.println("Saved file Location = "+ a);
        this.clean_Streams();
    }



    public void shareSessionKey(){
        if(instantender){return;} //instakill if some step was failed.


	    this.Session_Key = this.file_Sender.send_SessionKey_With_certs_key(this.their_cert_location);

        if(this.Session_Key==null){ //if failed to get session key.
            this.instantender=true; //set instant kill flag.
            System.out.println("FAILED TO GET SESSION KEY.");
            return;
        }
	    this.clean_Streams(); //clean the streams between usage.

        System.out.println("Success in Sharing Key..");
    }


    public void sendplaincert(){
        if(instantender){return;} //instakill if some step was failed.
        System.out.println("Attempting to send certificate unencrypted...");
        boolean success = this.file_Sender.sendPlainFile(this.my_cert,1024);
        if(this.failtestcheck(success)){ //on failure, trigger all instant kill flags, return.
            System.out.println("ERROR in process.");
            return;
        }
        System.out.println("Completed Cert sending Attempt");
    }




    private boolean failtestcheck(boolean test){
        if (!test){try
        {
            this.socket_To_Client.close();
            this.instantender=true; //set instant kill flag.
            return true;
        }
        catch(IOException ex){
            System.out.println("VERIFICATION ERROR");
        }}

        return false;
    }


    public void verify_Certs(String their_identity){
        if(instantender){return;} //instakill if some step was failed.


        System.out.println("Attempting to verify Cert is from: " + their_identity);
        certVerifier verifier = new certVerifier(this.socket_To_Client,their_cert_location);
        boolean ve = verifier.verify_is_person(their_identity);
        System.out.println("IS INDEED FROM: " + their_identity+ " - " + ve);
        if(failtestcheck(ve)){return;} //upon failure to verify is alice/bob, kill entire program, closing the socket.



        boolean verified = verifier.verify_Cert_and_Message();
        System.out.println("Verified sender has private key to this cert :" + verified);
        if(failtestcheck(verified)){return;} //if they fail the verification test (for ownership) , instantly KILL.


        System.out.println("Sending Encrypted message..");
        verifier.send_Encrypted_Message(this.privateKey_loc);
        System.out.println("Completed sending of encrypted message.");

        clean_Streams(); //clean the streams in between send and receives.


        try{
            TimeUnit.MILLISECONDS.sleep(100); //wait...
        }
        catch(InterruptedException ex){
            System.out.println("Interrupted...?");
            ex.printStackTrace();
        }

        System.out.println("Mutual Verification Phase is completed.");


    }
    public void receivecert(){ //takes in the argument of whose identity it is. either "ALICE" or "BOB"
        if(instantender){return;} //instakill if some step beforehand was failed.


        System.out.println("Attempting to receive certificate");
        String their_cert_location = this.file_Getter.recievePlainFile("Serverreceived/");
        if(their_cert_location ==null){//the cert didn't come through...
            this.instantender=true;
            System.out.println("Something went wrong while receiving the other party's certificate");
            return;
        }
        System.out.println("Completed Receiving!");
        this.their_cert_location=their_cert_location;
    }



    public void clean_Streams(){
        if(instantender){return;} //instakill if some step was failed. anyway the socket would be closed..
        try{
            DataInputStream in = new DataInputStream(this.socket_To_Client.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket_To_Client.getOutputStream());
            out.flush();
            TimeUnit.MILLISECONDS.sleep(10);
            in.skipBytes(in.available());
            //System.out.println("Stream Cleaned"); //debug message. uncomment if you want to know when it's being cleaned
            TimeUnit.MILLISECONDS.sleep(10);
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
