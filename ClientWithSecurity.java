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
		this.file_Sender.sendPlainFile(this.client_cert,1024);
		System.out.println("tried");
	}
}

//long timeStarted = System.nanoTime();
//long timeTaken = System.nanoTime() - timeStarted;
//System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");