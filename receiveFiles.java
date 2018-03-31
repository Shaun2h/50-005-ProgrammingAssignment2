import java.io.*;
import java.net.Socket;
import static jdk.nashorn.internal.runtime.Context.printStackTrace;

public class receiveFiles {
    private Socket sender;
    private FileOutputStream fileoutput;
    private BufferedOutputStream bufferoutputstream;
    private DataInputStream dataInputStream;
    public receiveFiles(Socket fromwho){
        this.sender = fromwho;
    }
    public void recievePlainFile(String saveLocation) {
        // If the packet is for transferring the filename
        try {
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            int packetType = this.dataInputStream.readInt();
            if (packetType == 0) {

                System.out.println("Receiving file...");
                int numBytes = this.dataInputStream.readInt();

                byte[] filename = new byte[numBytes];
                this.dataInputStream.read(filename);
                this.fileoutput = new FileOutputStream(saveLocation + new String(filename, 0, numBytes));
                this.bufferoutputstream= new BufferedOutputStream(this.fileoutput);
                // If the packet is for transferring a chunk of the file

            } else if (packetType == 1) {

                int numBytes = this.dataInputStream.readInt();
                byte[] block = new byte[numBytes];
                this.dataInputStream.read(block);

                if (numBytes > 0)
                    this.bufferoutputstream.write(block, 0, numBytes);

            } else if (packetType == 2) {

                System.out.println("Closing connection...");

                if (this.bufferoutputstream != null) this.bufferoutputstream.close();
                if (this.fileoutput != null) this.fileoutput.close();
            }
        }
        catch(IOException ex){
            printStackTrace(ex);
        }
    }
}
