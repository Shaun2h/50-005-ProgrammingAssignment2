import java.io.*;
import java.net.Socket;
import java.util.*;
import java.lang.*;
public class sendFiles {
    Socket receipient;
    FileInputStream cert_FileInputStream; //to extract from a file.
    BufferedOutputStream bufferedFileOutputStream;//to place something into opponent
    public sendFiles(Socket target){
        this.receipient = target;
    }
    public void send(String file_loc, int byte_Array_Size){
        try{
            this.cert_FileInputStream = new FileInputStream(file_loc);
            DataOutputStream toClient = new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.
            byte[] buffer = new byte[byte_Array_Size];
            buffer =
        }
        catch(FileNotFoundException ex){
            System.out.println("ERROR UPLOADING FILE!");
            System.out.println(ex);
        }
        catch(IOException ex){
            System.out.println("ERROR IN CREATING CHANNELS");
            System.out.println(ex);
        }
    }
}
