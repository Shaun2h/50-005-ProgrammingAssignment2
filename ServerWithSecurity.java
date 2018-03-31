import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import static jdk.nashorn.internal.runtime.Context.printStackTrace;

public class ServerWithSecurity {
    private String my_cert; //Where is the server's certificate stored?
    private int my_Port_Num;
    private ServerSocket serverSocket;
    private DataInputStream fromClient;
    private DataOutputStream toClient;
    private Socket socket_To_Client;
    private sendFiles file_Sender;

    //"C:/Users/User/Desktop/Server/Bob_Cert.crt" my public cert is stored here

    public ServerWithSecurity(String My_CERT, int myPortNum){
	    this.my_cert = My_CERT;
	    this.my_Port_Num = myPortNum;
	    try{
	        this.serverSocket = new ServerSocket(this.my_Port_Num);
        }
        catch(IOException ex){
	        printStackTrace(ex);
        }
    }


	public void start(String[] args) {
		try {
			this.socket_To_Client= this.serverSocket.accept();
            file_Sender = new sendFiles(this.socket_To_Client);



		} catch (Exception e) {e.printStackTrace();}

	}

}
