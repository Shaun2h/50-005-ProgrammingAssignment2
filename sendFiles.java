import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.lang.*;

import static jdk.nashorn.internal.runtime.Context.printStackTrace;

public class sendFiles {
    private Socket receipient;
    private FileInputStream cert_FileInputStream; //to extract from a file.
    private BufferedInputStream bufferedInputStreamForFile;//to place something into opponent
    private sendFiles(Socket target){
        this.receipient = target;
    }
    public void sendPlain(String file_loc, int byte_Array_Size){
        try{
            this.cert_FileInputStream = new FileInputStream(file_loc);
            DataOutputStream PipetoClient = new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream);
            byte[] buffer = new byte[byte_Array_Size];
            boolean fileHasEnded = false;
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (!fileHasEnded) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                PipetoClient.writeInt(no_of_bytes_sent);
                PipetoClient.write(buffer);
                PipetoClient.flush();
                fileHasEnded = no_of_bytes_sent < buffer.length; //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
            }
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.



            System.out.println("Ending off file sending...");
            PipetoClient.writeInt(2);
            PipetoClient.flush();

        }
        catch(FileNotFoundException ex){
            System.out.println("ERROR UPLOADING FILE!");
            printStackTrace(ex);
        }
        catch(IOException ex){
            System.out.println("ERROR IN CREATING CHANNELS");
            printStackTrace(ex);
        }
    }



    public void send_File_With_PublicKey_Encrypted(String file_loc, int byte_Array_Size, String their_cert_location){ //A method to send things via public key encryption
        try{
            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key);
            this.cert_FileInputStream = new FileInputStream(file_loc);
            DataOutputStream PipetoClient = new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream);
            byte[] buffer = new byte[byte_Array_Size];
            boolean fileHasEnded = false;
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (!fileHasEnded) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                cipher.doFinal(buffer);
                PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                PipetoClient.writeInt(no_of_bytes_sent);
                PipetoClient.write(buffer);
                PipetoClient.flush();
                fileHasEnded = no_of_bytes_sent < buffer.length; //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
            }
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.



            System.out.println("Ending off file sending...");
            PipetoClient.writeInt(2);
            PipetoClient.flush();

        }
        catch(IllegalBlockSizeException ex){
            System.out.println("Illegal block size!");
            printStackTrace(ex);
        }
        catch(BadPaddingException ex){
            System.out.println("Bad padding exception occurred.");
            printStackTrace(ex);
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception while encrypting file to send over...");
            printStackTrace(ex);
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("NO SUCH ALGORITHM EXCEPTION");
            printStackTrace(ex);
        }
        catch(NoSuchPaddingException ex){
            System.out.println("NO SUCH PADDING EXCEPTION");
            printStackTrace(ex);
        }
        catch(CertificateException ex){
            System.out.println("ERROR IN CERTIFICATE FACTORY INSTANCE CREATION");
            printStackTrace(ex);
        }
        catch(FileNotFoundException ex){
            System.out.println("ERROR UPLOADING FILE!");
            printStackTrace(ex);
        }
        catch(IOException ex){
            System.out.println("ERROR IN CREATING CHANNELS");
            printStackTrace(ex);
        }
    }
}
