import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
		}
	}
	public void start(){
		try {
			this.file_Sender = new sendFiles(this.socket_To_Server);
			this.file_Getter = new receiveFiles(this.socket_To_Server);
			//wait until you've gotten connected....
		} catch (Exception e) {e.printStackTrace();}
		System.out.println("got a connection! - client");

	}
	public void sendplaincert(){
		System.out.println("Attempting to send plain certificate...");
		this.file_Sender.sendPlainFile(this.client_cert,1024);
		System.out.println("Completed Cert sending Attempt");
	}
	public void verify_Certs(String their_identity){
		System.out.println("Attempting to verify Cert is from:" + their_identity);
		certVerifier verifier = new certVerifier(this.socket_To_Server,this.their_cert_location);
		boolean ve = verifier.verify_is_person(their_identity);
		System.out.println("IS INDEED FROM: " + their_identity+ " - " + ve);
		verifier.send_Encrypted_Message(this.privateKey_loc);
		//boolean verified = verifier.verify_Cert_and_Message();
		//System.out.println("Verified sender has private key to this cert :" + verified);
	}
	public void receivecert(){ //takes in the argument of whose identity it is. either "ALICE" or "BOB"
		System.out.println("Attempting to receive certificate");
		String their_cert_location = this.file_Getter.recievePlainFile("ClientReceived/");
		System.out.println("Completed Receiving!");
		this.their_cert_location = their_cert_location;

	}
	public void clean_Streams(){
		try{
			DataInputStream in = new DataInputStream(this.socket_To_Server.getInputStream());
			DataOutputStream out = new DataOutputStream(this.socket_To_Server.getOutputStream());
			out.flush();
			TimeUnit.MILLISECONDS.sleep(100);
			byte[] a = new byte[1000];
			while(in.available()>0){
				in.read(a);
			}
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

//long timeStarted = System.nanoTime();
//long timeTaken = System.nanoTime() - timeStarted;
//System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");