import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class ClientWithSecurity {

	private String client_cert; //Where is my certificate?
	private int my_Port_Num;
	private Socket socket_To_Server;
	private sendFiles file_Sender;
	private receiveFiles file_Getter;
	private String target;
	private String their_cert_location;
	private String privateKey_loc;
	private Key Session_Key;
	private boolean instantender;


	public ClientWithSecurity(String My_CERT, int myPortNum, String targetIP,String privateKey_loc){
		this.client_cert = My_CERT;
		this.my_Port_Num = myPortNum;
		this.target =targetIP;
		this.privateKey_loc = privateKey_loc;
		try{
			this.socket_To_Server = new Socket(this.target,this.my_Port_Num); //i.e. target connection
		}
		catch(IOException ex){
			ex.printStackTrace();
			this.instantender=true; //there is no point running anything else if i can't make a socket...
		}
	}


	public boolean failtestcheck(boolean test){
		if (!test){try
		{
			this.socket_To_Server.close();
			this.instantender=true; //set instant kill flag.
			return true;
		}
		catch(IOException ex){
			System.out.println("VERIFICATION ERROR");
		}}
		return false;
	}


	public void start(){
		try {
			this.file_Sender = new sendFiles(this.socket_To_Server);
			this.file_Getter = new receiveFiles(this.socket_To_Server);
			//wait until you've gotten connected....
		} catch (Exception e) {e.printStackTrace();}
		System.out.println("Connected to Server");

	}


	public void sendplaincert(){
		if(instantender){return;} //instakill if some step was failed.

		System.out.println("Attempting to send certificate...");
		boolean success = this.file_Sender.sendPlainFile(this.client_cert,1024);
		if(this.failtestcheck(success)){ //on failure, trigger all instant kill flags, return
			System.out.println("ERROR in the process of sending certificates!");
			return;
		}
		System.out.println("Completed Cert sending Attempt");
	}

	public void sendFile(String file_to_send){
		if(instantender){return;} //instakill if some step was failed previously.
		System.out.println("Attempting to send file over...");
		boolean success = this.file_Sender.sendPlainFile(file_to_send,1024);
		if(this.failtestcheck(success)){ //on failure, trigger all instant kill flags, return.
			System.out.println("ERROR in process.");
			return;
		}
		System.out.println("Completed File sending Attempt");
	}

	public void receiveSessionKey(){
		if(instantender){return;} //instakill if some step was failed.


		this.Session_Key=this.file_Getter.receive_SessionKey_With_MY_key(this.privateKey_loc);
		if(this.Session_Key==null){this.instantender = true; return;} //failed to get key. KILL.
		System.out.println("Success in obtaining Key");
	}



	public void verify_Certs(String their_identity){
		if(instantender){return;} //instakill if some step was failed.


		System.out.println("Attempting to verify Cert is from:" + their_identity);
		certVerifier verifier = new certVerifier(this.socket_To_Server,this.their_cert_location);
		boolean ve = verifier.verify_is_person(their_identity);
		System.out.println("IS INDEED FROM: " + their_identity+ " - " + ve);
		if(this.failtestcheck(ve)){
			System.out.println("failed verification test. did you spell your server's name correctly");
			return;
		}


		System.out.println("Sending Encrypted Message..");
		verifier.send_Encrypted_Message(this.privateKey_loc); //send them a message encrypted with my private key.
		System.out.println("sent Encrypted message");


		this.clean_Streams(); //clean streams between use!


		System.out.println("Receiving Message...");
		boolean verified = verifier.verify_Cert_and_Message();
		System.out.println("Verified sender has private key to this cert : " + verified);
		if(!verified){
			try{this.socket_To_Server.close();}
			catch(IOException ex){
				System.out.println("ERROR IN VERIFICATION OF CERTIFICATE");
			}
		}
		try{
			TimeUnit.MILLISECONDS.sleep(100);
		}
		catch(InterruptedException ex){
			System.out.println("Interrupted...?");
			ex.printStackTrace();
		}
		System.out.println("Mutual Verification Phase is completed.");
		clean_Streams();
	}


	public void receivecert(){ //takes in the argument of whose identity it is. either "ALICE" or "BOB"
		if(instantender){return;} //instakill if some step beforehand was failed.

		System.out.println("Attempting to receive certificate");
		String their_cert_location = this.file_Getter.recievePlainFile("Clientreceived/");
		if(their_cert_location ==null){ //the cert didn't come through...
			this.instantender=true;
			System.out.println("Something went wrong while receiving the other party's certificate");
			return;
		}
		System.out.println("Completed Receiving their cert!");
		this.their_cert_location=their_cert_location;
	}

	public void send_file_with_AES(){
		if(instantender){return;} //instakill if some step was failed.


		if (this.their_cert_location==null){return;} //cancel if you don't have their cert.
		System.out.println("Attempting to send file Encrypted with AES Key");
		this.file_Sender.send_File_With_AES("rr.txt",this.Session_Key);
		System.out.println("COMPLETED SENDING");

	}


	public void sendWith_ServerPublicKeyEncrypted(String file){
		if(instantender){return;} //instakill if some step was failed.


		System.out.println("Attempting to send file encrypted with their public key..");
		this.file_Sender.send_File_With_certs_key(file,this.their_cert_location);

		this.clean_Streams(); //clean streams between use...

	}

	public void clean_Streams(){
		/*try{
			DataInputStream in = new DataInputStream(this.socket_To_Server.getInputStream());
			DataOutputStream out = new DataOutputStream(this.socket_To_Server.getOutputStream());
			out.flush();
			TimeUnit.MILLISECONDS.sleep(10);
			byte[] a = new byte[1000];
			while(in.available()>0){
				in.read(a);
			}
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
		*/
	}
}

