import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientWithSecurity {

	private String client_cert; //Where is my certificate?
	private int my_Port_Num;
	private ServerSocket serverSocket;
	private Socket socket_To_Server;
	private sendFiles file_Sender;
	private receiveFiles file_Getter;

	public ClientWithSecurity(String My_CERT, int myPortNum){
		this.client_cert = My_CERT;
		this.my_Port_Num = myPortNum;
		try{
			this.serverSocket = new ServerSocket(this.my_Port_Num);
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
	public void start(){
		try {
			this.socket_To_Server= this.serverSocket.accept();
			this.file_Sender = new sendFiles(this.socket_To_Server);
			this.file_Getter = new receiveFiles(this.socket_To_Server);
			//wait until you've gotten connected....
		} catch (Exception e) {e.printStackTrace();}
		System.out.println("got a connection! - client");
		
	}
}

//long timeStarted = System.nanoTime();
//long timeTaken = System.nanoTime() - timeStarted;
//System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");