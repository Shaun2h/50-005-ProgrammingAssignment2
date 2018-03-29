import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWithSecurity {

	public static void main(String[] args) {

		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		DataOutputStream toClient = null;
		DataInputStream fromClient = null;

		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedFileOutputStream = null;

		try {
			welcomeSocket = new ServerSocket(4321);
			connectionSocket = welcomeSocket.accept();
			fromClient = new DataInputStream(connectionSocket.getInputStream());
			toClient = new DataOutputStream(connectionSocket.getOutputStream());
			toClient.write(1); //send acknowledgement for ready to get cert.
			while (!connectionSocket.isClosed()) {

				int packetType = fromClient.readInt();// If the packet is for transferring the filename
				if (packetType == 0) {
					System.out.println("Receiving cert name...");
					int numBytes = fromClient.readInt();
					byte [] filename = new byte[numBytes];
					fromClient.read(filename);
					fileOutputStream = new FileOutputStream("C:/Users/User/Desktop/Server/"+new String(filename, 0, numBytes)); //save to your desktop.
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
				}
				else if (packetType == 1) {// If the packet is for transferring a chunk of the file
					int numBytes = fromClient.readInt();
					byte [] block = new byte[numBytes];
					fromClient.read(block);
					if (numBytes > 0)
						bufferedFileOutputStream.write(block, 0, numBytes);
				}
				else if (packetType == 2) {

					System.out.println("Closing connection...");

					if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
					if (bufferedFileOutputStream != null) fileOutputStream.close();
					fromClient.close();
					toClient.close();
					connectionSocket.close();
				}

			}
		} catch (Exception e) {e.printStackTrace();}

	}

}


/*
	while (!connectionSocket.isClosed()) {

				int packetType = fromClient.readInt();// If the packet is for transferring the filename
				if (packetType == 0) {
					System.out.println("Receiving cert name...");
					int numBytes = fromClient.readInt();
					byte [] filename = new byte[numBytes];
					fromClient.read(filename);
					fileOutputStream = new FileOutputStream("C:/Users/User/Desktop/Server/"+new String(filename, 0, numBytes)); //save to your desktop.
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
				}
				else if (packetType == 1) {// If the packet is for transferring a chunk of the file
					int numBytes = fromClient.readInt();
					byte [] block = new byte[numBytes];
					fromClient.read(block);
					if (numBytes > 0)
						bufferedFileOutputStream.write(block, 0, numBytes);
				}
				else if (packetType == 2) {

					System.out.println("Closing connection...");

					if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
					if (bufferedFileOutputStream != null) fileOutputStream.close();
					fromClient.close();
					toClient.close();
					connectionSocket.close();
				}

			}
 */
