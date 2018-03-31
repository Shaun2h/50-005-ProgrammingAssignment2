import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.lang.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import static jdk.nashorn.internal.runtime.Context.printStackTrace;

public class sendFiles {
    private Socket receipient;
    private FileInputStream cert_FileInputStream; //to extract from a file.
    private BufferedInputStream bufferedInputStreamForFile; //file buffer
    private DataOutputStream PipetoClient =null; //to place something into opponent
    public sendFiles(Socket target){
        this.receipient = target;
    }


    public void sendPlainFile(String file_loc, int byte_Array_Size){
        try{
            if(this.PipetoClient ==null){
                this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            }
            //i.e if it has not been initated before, initate.
            this.cert_FileInputStream = new FileInputStream(file_loc);
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream);
            byte[] buffer = new byte[byte_Array_Size];
            boolean fileHasEnded = false;


            this.PipetoClient.writeInt(0);
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            //inform them of file name..


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



            System.out.println("Ending sending of unencrypted file...");
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



    public void send_File_With_certs_key(String file_loc, int byte_Array_Size, String their_cert_location){ //A method to send things with public key encryption
        try{
            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //certificate factory
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey(); //their public key extracted from the cert.
            //KEEP IN MIND THIS NEVER MEANS THAT THE PUBLIC KEY IS VALID.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key);

            //Cipher is now ready for use..
            this.PipetoClient.writeInt(0);
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            //inform them of the file name..

            //open their file and the required channels.
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



            System.out.println("Ending sending off file encrpyted with someone's public key");
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
    public void send_File_With_PrivateKey_Encrypted(String file_loc, int byte_Array_Size, String my_key_location){ //A method to send things with private encryption
        try{


            File key_file = new File(my_key_location);
            BufferedInputStream key_File_Input_Stream = new BufferedInputStream(new FileInputStream(key_file)); //obtain a buffered input stream of your private key.
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] private_key_bytes = new byte[(int) key_file.length() ]; //obtain a byte array that can hold my entire key.
            key_File_Input_Stream.read(private_key_bytes); //read the entire file into this array..
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(private_key_bytes);
            PrivateKey my_Private_key = kf.generatePrivate(keySpec);


            Cipher cipher_private = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher_private.init(Cipher.ENCRYPT_MODE,my_Private_key); //make a cipher with your private key...

            //ready to begin sending...


            this.PipetoClient.writeInt(0);
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            //inform them of the file name..


            DataOutputStream pipe_To_Client = new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream); //new buffered input stream for the wanted file.
            byte[] buffer = new byte[byte_Array_Size];
            boolean fileHasEnded = false;
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (!fileHasEnded) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                cipher_private.doFinal(buffer);
                pipe_To_Client.writeInt(1); //signal to them that i'm sending a part of the file.
                pipe_To_Client.writeInt(no_of_bytes_sent);
                pipe_To_Client.write(buffer);
                pipe_To_Client.flush();
                fileHasEnded = no_of_bytes_sent < buffer.length; //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
            }
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            key_File_Input_Stream.close();


            System.out.println("Ending off sending of file with MY private key encryption...");
            pipe_To_Client.writeInt(2);
            pipe_To_Client.flush();

        }
        catch(InvalidKeySpecException ex){
            System.out.println("Invalid key spec");
            printStackTrace(ex);
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
