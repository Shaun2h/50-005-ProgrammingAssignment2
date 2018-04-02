import java.io.IOException;
import java.net.Socket;

public class ClientWithSecurity {

	private String client_cert; //Where is my certificate?
	private int my_Port_Num;
	private Socket socket_To_Server;
	private sendFiles file_Sender;
	private receiveFiles file_Getter;
	private String target;

	public ClientWithSecurity(String My_CERT, int myPortNum, String targetIP){
		this.client_cert = My_CERT;
		this.my_Port_Num = myPortNum;
		this.target =targetIP;
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
	public void receivecert_andVerify(String their_identity){ //takes in the argument of whose identity it is. either "ALICE" or "BOB"
		System.out.println("Attempting to receive certificate");
		String their_cert_location = this.file_Getter.recievePlainFile("ClientReceived/");
		System.out.println("Completed Receiving!");
		System.out.println("Attempting to verify Cert is from:" + their_identity);
		certVerifier verifier = new certVerifier();
		System.out.println("IS INDEED FROM: " + their_identity+ " - " + verifier.verify_is_person(their_cert_location,their_identity));
	}
}

//long timeStarted = System.nanoTime();
//long timeTaken = System.nanoTime() - timeStarted;
//System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");