import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWithSecurity {

	public static void main(String[] args) {
		while (true) {
			ServerSocket welcomeSocket; //ServerSocket to wait...
			Socket connectionSocket; //Socket duh. you have one.
			DataOutputStream toClient; //send data to here to talk to opponent party.
			DataInputStream fromClient; //Receive data from here.
			String opponent_cert; //String to know where the opponent party's cert was stored..
			int bytes = 0; //used as a counter for the number of bytes to be sent over using ANY buffered input/output
			FileOutputStream fileOutputStream = null; //Required to save something from the opponent to my computer.
			BufferedInputStream bufferedFileInputStream; //to read something from the current server to the opponent.
			BufferedOutputStream bufferedFileOutputStream = null;//to place something into my server.
			FileInputStream cert_FileInputStream; //Where you upload things. In this case, only the cert will EVER be uploaded.
			String my_cert = "C:/Users/User/Desktop/Server/Bob_Cert.crt";// here i'll use bob's cert for the server.

			try {
				welcomeSocket = new ServerSocket(4321);
				connectionSocket = welcomeSocket.accept();
				fromClient = new DataInputStream(connectionSocket.getInputStream());
				toClient = new DataOutputStream(connectionSocket.getOutputStream());
				toClient.write(1); //send acknowledgement for ready to get cert.


				//This part is for the transfer of the certificate TO the server.
				while (true) {

					int packetType = fromClient.readInt();// If the packet is for transferring the filename
					if (packetType == 0) {
						System.out.println("Receiving cert name...");
						int numBytes = fromClient.readInt();
						byte[] filename = new byte[numBytes];
						fromClient.read(filename);
						opponent_cert = "C:/Users/User/Desktop/Server/" + new String(filename, 0, numBytes);
						fileOutputStream = new FileOutputStream(opponent_cert); //save to your desktop.
						bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
					} else if (packetType == 1) {// If the packet is for transferring a chunk of the file
						int numBytes = fromClient.readInt();
						byte[] block = new byte[numBytes];
						fromClient.read(block);
						if (numBytes > 0) {
							bufferedFileOutputStream.write(block, 0, numBytes);
						}
					} else if (packetType == 2) {
						if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
						if (bufferedFileOutputStream != null) fileOutputStream.close();
						break;
						//Now the item is saved.
					}
				}

				fromClient.readInt(); //just wait for something to come in. Any integer is esssentially a sign for ready to recieve client certificate.

				toClient.writeInt(0);
				toClient.writeInt(my_cert.getBytes().length);
				toClient.write(my_cert.getBytes());
				toClient.flush();


				//INITIATE THE CERTIFICATE SENDING TO THE CLIENT

				cert_FileInputStream = new FileInputStream(my_cert); //open the file input stream for cert
				bufferedFileInputStream = new BufferedInputStream(cert_FileInputStream);

				//generate a buffer
				byte[] fromFileBuffer = new byte[1024];

				// Send the file
				boolean fileHasEnded = false;
				while (!fileHasEnded) {
					bytes = bufferedFileInputStream.read(fromFileBuffer);
					toClient.writeInt(1);
					toClient.writeInt(bytes);
					toClient.write(fromFileBuffer);
					toClient.flush();
					fileHasEnded = bytes < fromFileBuffer.length; //Check whether The file has ended.
				}


				//TODO: CHECK THEIR SERVER VALIDITY AND EXIT IF WRONG.

				fromClient.readInt(); //wait for acknowledgement that they have completed processing of my cert.
				if (connectionSocket.isClosed()) {
					continue;
				}


			} catch (Exception e) {
				e.printStackTrace();
			}

		}
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

			}nn  '
 */
