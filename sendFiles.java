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
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class sendFiles {
    private Socket receipient;
    private FileInputStream cert_FileInputStream; //to extract from a file.
    private BufferedInputStream bufferedInputStreamForFile; //file buffer
    private DataOutputStream PipetoClient =null; //to place something into opponent
    private DataInputStream PipeFromClient =null;
    public sendFiles(Socket target){
        this.receipient = target;
    }


    public void sendPlainFile(String file_loc, int byte_Array_Size){
        try{
            this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe to client");

            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe from client");

            //i.e if it has not been initated before, initate.
            this.cert_FileInputStream = new FileInputStream(file_loc);
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream);
            byte[] buffer = new byte[byte_Array_Size];
            this.PipetoClient.writeInt(0);
            System.out.println("initiated File sending by sending int 0 over");
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            System.out.println("wrote file name length over.");
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            System.out.println("wrote the file name over");
            //inform them of file name..
            File file_being_sent = new File(file_loc);
            this.PipetoClient.writeLong(file_being_sent.length());
            int no_of_bytes_sent; //tell them how many bytes was sent...
            int total_bytes_sent=0;
            while (total_bytes_sent<file_being_sent.length()) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                this.PipetoClient.writeInt(no_of_bytes_sent);
                this.PipetoClient.write(buffer);
                this.PipetoClient.flush();
                total_bytes_sent+=no_of_bytes_sent; //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
                System.out.println("sent one packet over");
                TimeUnit.MILLISECONDS.sleep(100);
            }
            PipetoClient.writeInt(2);
            PipetoClient.flush();
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("Ending sending of unencrypted file...");



        }
        catch(InterruptedException ex){
            System.out.println("Interrupted..?");
            ex.printStackTrace();
        }
        catch(FileNotFoundException ex){
            System.out.println("ERROR UPLOADING FILE!");
            ex.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("ERROR IN CREATING CHANNELS");
            ex.printStackTrace();
        }
    }



    public void send_File_With_certs_key(String file_loc, int byte_Array_Size, String their_cert_location){ //A method to send things with public key encryption
        try{
            if(this.PipetoClient ==null){
                this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            }
            if(this.PipeFromClient==null){
                this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            }
            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //certificate factory
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey(); //their public key extracted from the cert.
            //KEEP IN MIND THIS NEVER MEANS THAT THE PUBLIC KEY IS VALID.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key);

            //Cipher is now ready for use..
            this.PipetoClient.writeInt(0);
            this.PipetoClient.flush();
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            this.PipetoClient.flush();
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            //inform them of the file name..

            //open their file and the required channels.
            this.cert_FileInputStream = new FileInputStream(file_loc);
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream);

            File file_being_sent = new File(file_loc);
            this.PipetoClient.writeLong(file_being_sent.length());
            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (total_bytes_sent<file_being_sent.length()) {
                byte[] buffer = new byte[byte_Array_Size];
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                cipher.doFinal(buffer);
                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                this.PipetoClient.flush();
                this.PipetoClient.writeInt(no_of_bytes_sent);
                this.PipetoClient.flush();
                this.PipetoClient.write(buffer);
                System.out.println(buffer);
                this.PipetoClient.flush();
                total_bytes_sent+=no_of_bytes_sent;
                 //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
                System.out.println("sent one packet over-via their public key");
                TimeUnit.MILLISECONDS.sleep(10);
            }
            this.bufferedInputStreamForFile.close();
            theircert.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            System.out.println("Ending sending off file encrpyted with someone's public key");
            TimeUnit.MILLISECONDS.sleep(10);

        }
        catch(InterruptedException ex){
            System.out.println("Interrupted...?");
            ex.printStackTrace();
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("Illegal block size!");
            ex.printStackTrace();
        }
        catch(BadPaddingException ex){
            System.out.println("Bad padding exception occurred.");
            ex.printStackTrace();
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception while encrypting file to send over...");
            ex.printStackTrace();
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("NO SUCH ALGORITHM EXCEPTION");
            ex.printStackTrace();
        }
        catch(NoSuchPaddingException ex){
            System.out.println("NO SUCH PADDING EXCEPTION");
            ex.printStackTrace();
        }
        catch(CertificateException ex){
            System.out.println("ERROR IN CERTIFICATE FACTORY INSTANCE CREATION");
            ex.printStackTrace();
        }
        catch(FileNotFoundException ex){
            System.out.println("ERROR UPLOADING FILE!");
            ex.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("ERROR IN CREATING CHANNELS");
            ex.printStackTrace();
        }
    }
    public void send_File_With_PrivateKey_Encrypted(String file_loc, int byte_Array_Size, String my_key_location){ //A method to send things with private encryption
        try{
            if(this.PipetoClient ==null){
                this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            }
            if(this.PipeFromClient==null){
                this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            }

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
            this.PipetoClient.flush();
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            this.PipetoClient.flush();
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            //inform them of the file name..
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream); //new buffered input stream for the wanted file.
            byte[] buffer = new byte[byte_Array_Size];
            File file_being_sent = new File(file_loc);
            this.PipetoClient.writeLong(file_being_sent.length());
            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (total_bytes_sent<file_being_sent.length()) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                cipher_private.doFinal(buffer);
                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                this.PipetoClient.flush();
                this.PipetoClient.writeInt(no_of_bytes_sent);
                this.PipetoClient.flush();
                this.PipetoClient.write(buffer);
                System.out.println(buffer);
                this.PipetoClient.flush();
                total_bytes_sent+=no_of_bytes_sent;//tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
                System.out.println("sent one packet over-via my private key");
                TimeUnit.MILLISECONDS.sleep(10);
            }
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            key_File_Input_Stream.close();

            TimeUnit.MILLISECONDS.sleep(10);
            System.out.println("Ending off sending of file with MY private key encryption...");
        }
        catch(InterruptedException ex){
            System.out.println("Interrupted...?");
            ex.printStackTrace();
        }
        catch(InvalidKeySpecException ex){
            System.out.println("Invalid key spec");
            ex.printStackTrace();
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("Illegal block size!");
            ex.printStackTrace();
        }
        catch(BadPaddingException ex){
            System.out.println("Bad padding exception occurred.");
            ex.printStackTrace();
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception while encrypting file to send over...");
            ex.printStackTrace();
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("NO SUCH ALGORITHM EXCEPTION");
            ex.printStackTrace();
        }
        catch(NoSuchPaddingException ex){
            System.out.println("NO SUCH PADDING EXCEPTION");
            ex.printStackTrace();
        }
        catch(FileNotFoundException ex){
            System.out.println("ERROR UPLOADING FILE!");
            ex.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("ERROR IN CREATING CHANNELS");
            ex.printStackTrace();
        }
    }
}
