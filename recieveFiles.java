import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class recieveFiles {
    public void recieve() {
        int numBytes = fromClient.readInt();
        if (packetType == 0) {

            System.out.println("Receiving file...");

            int numBytes = fromClient.readInt();
            byte[] filename = new byte[numBytes];
            fromClient.read(filename);

            fileOutputStream = new FileOutputStream("recv/" + new String(filename, 0, numBytes));
            bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);

            // If the packet is for transferring a chunk of the file
        } else if (packetType == 1) {

            int numBytes = fromClient.readInt();
            byte[] block = new byte[numBytes];
            fromClient.read(block);

            if (numBytes > 0)
                bufferedFileOutputStream.write(block, 0, numBytes);

        } else if (packetType == 2) {

            System.out.println("Closing connection...");

            if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
            if (bufferedFileOutputStream != null) fileOutputStream.close();
            fromClient.close();
            toClient.close();
            connectionSocket.close();
        }
    }
}
