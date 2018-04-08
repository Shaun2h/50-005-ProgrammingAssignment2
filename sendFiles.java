import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.lang.*;
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
                TimeUnit.MILLISECONDS.sleep(10);
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



    public void send_File_With_certs_key(String file_loc, String their_cert_location){ //A method to send things with public key encryption
        try{
            int byte_Array_Size = 117;
            this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe to client");

            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe from client");

            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //certificate factory
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey(); //their public key extracted from the cert.
            //KEEP IN MIND THIS NEVER MEANS THAT THE PUBLIC KEY IS VALID.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key);

            //Cipher is now ready for use..
            this.PipetoClient.writeInt(0);
            System.out.println("initiated File sending by sending Int 0 over...");
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            System.out.println("wrote file name length over");
            this.PipetoClient.write(file_loc.getBytes());
            System.out.println("Wrote file name over..");
            this.PipetoClient.flush();
            //finished informing them of file name...



            //open their file and the required channels.
            this.cert_FileInputStream = new FileInputStream(file_loc); //open the file to be sent
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream); //convert to buffered.
            byte[] buffer = new byte[byte_Array_Size];
            byte[] tosend;
            File file_being_sent = new File(file_loc);
            int addtomax=0;
            if(file_being_sent.length()%117>0){
                addtomax=1;
            }
            int totalbytes_to_besent = ((int) file_being_sent.length())/117*128 + addtomax*128; //now we know how many bytes will be sent over.



            this.PipetoClient.writeLong(totalbytes_to_besent); //MATH OF TOTAL BYTES SENT IN THE END IS ABOVE.
            //told them how many bytes will be sent over, total.


            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //tell them how many bytes was sent in this round.


            while (total_bytes_sent<totalbytes_to_besent) {

                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                tosend = cipher.doFinal(buffer); //this will result in your 128 array from your 117 array


                //System.out.println("Total bytes sent over - "+ tosend.length); //for debug


                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.

                this.PipetoClient.writeInt(tosend.length); //You area always writing arrays of length 128. Until the last array, perhaps?

                this.PipetoClient.write(tosend);
                //System.out.println(tosend);

                this.PipetoClient.flush(); //ensure that it is written out, not stored in the buffer.

                total_bytes_sent+=no_of_bytes_sent;
                 //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.

                //System.out.println("sent one packet over-via their public key"); //for debug
                TimeUnit.MILLISECONDS.sleep(60);
            }
            this.bufferedInputStreamForFile.close();
            theircert.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            System.out.println("Ending sending off file encrpyted with someone's public key");
            TimeUnit.MILLISECONDS.sleep(100);

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


    private Key AES_Gen(){  //A method to generate AES key...
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            return keygen.generateKey();
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("no such algorithm when generating AES");
            ex.printStackTrace();
        }
        return null;
    }


    public Key send_SessionKey_With_certs_key(String their_cert_location){ //A method to send things with public key encryption
        try{
            System.out.println("Generating Session key...");
            Key session_Key = AES_Gen();
            this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe to client");
            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe from client");

            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //certificate factory
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey(); //their public key extracted from the cert.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key);

            //Cipher is now ready for use..
            this.PipetoClient.writeInt(0);
            System.out.println("initiated File sending by sending Int 0 over...");
            byte[] byte_Array_To_Send = cipher.doFinal(session_Key.getEncoded());
            this.PipetoClient.writeInt(byte_Array_To_Send.length);
            System.out.println("wrote array length over..");
            System.out.println("Array Length is currently: " + byte_Array_To_Send.length);
            //finished informing them of file name...
            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (total_bytes_sent<byte_Array_To_Send.length) {
                no_of_bytes_sent = byte_Array_To_Send.length;
                System.out.println("Total bytes sent over - "+ no_of_bytes_sent);
                for (byte asd: byte_Array_To_Send){
                    System.out.print(asd);
                }
                System.out.println("");
                this.PipeFromClient.readInt();
                this.PipetoClient.write(byte_Array_To_Send);
                System.out.println("sent aes to client");
                total_bytes_sent+=no_of_bytes_sent;
                this.PipetoClient.flush();
                //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
                System.out.println("sent one packet over-via their public key");
                TimeUnit.MILLISECONDS.sleep(60);
            }
            System.out.println("Ending sending off file encrpyted with someone's public key");
            TimeUnit.MILLISECONDS.sleep(100);
            return session_Key;


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
        return null;
    }
    public void send_File_With_AES(String file_loc,Key session_Key){ //A method to send things with private encryption
        try{
            IvParameterSpec iv =null;
            int byte_Array_Size = 16000;
            this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe to client");
            TimeUnit.SECONDS.sleep(1);
            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            System.out.println("Created pipe from client");
            Cipher cipher_private = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher_private.init(Cipher.ENCRYPT_MODE,session_Key,iv); //make a cipher with your private key...
            //ready to begin sending...

            this.PipetoClient.writeInt(0);
            System.out.println("initiated File sending by sending int 0 over");
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            System.out.println("wrote file name length over.");
            this.PipetoClient.write(file_loc.getBytes());
            System.out.println("wrote the file name over");
            //informing of File name completed...
            File file_being_sent = new File(file_loc);
            int number_of_blocks = (int)file_being_sent.length()/byte_Array_Size;
            int totalbytes_to_besent = ((  byte_Array_Size/16 +1 )*16*number_of_blocks) ;
            this.bufferedInputStreamForFile = new BufferedInputStream(new FileInputStream(file_being_sent)); //new buffered input stream for the wanted file.
            byte[] buffer = new byte[byte_Array_Size];
            byte[] after_process;

            //Obtain the File being sent...

            this.PipetoClient.writeLong(totalbytes_to_besent);
            //inform total file length...
            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (total_bytes_sent<totalbytes_to_besent) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                after_process = cipher_private.doFinal(buffer);
                //after_process = Base64.getEncoder().encode(after_process);
                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                //this.PipetoClient.flush();
                this.PipetoClient.writeInt(after_process.length);
                //this.PipetoClient.flush();
                this.PipetoClient.write(after_process);
                this.PipetoClient.flush();
                for (byte b: after_process){
                System.out.print(b);
                }
                System.out.println("");
                total_bytes_sent+=no_of_bytes_sent;//tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
                //System.out.println("sent one packet over encrypted with session key");
                if(total_bytes_sent>totalbytes_to_besent){System.out.println("finished byte requirement.");}
                TimeUnit.MILLISECONDS.sleep(60);
            }
            System.out.println("sent all data");
            this.PipetoClient.flush();
            TimeUnit.SECONDS.sleep(2);
            //this.PipetoClient.writeInt(2);
            TimeUnit.SECONDS.sleep(1);
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.


            System.out.println("Ending off sending of file with session key encryption...");
        }
        catch(InvalidAlgorithmParameterException ex){
            System.out.println("Invalid Algorithm Parameter Exception");
            ex.printStackTrace();
        }
        catch(InterruptedException ex){
            System.out.println("Interrupted...?");
            ex.printStackTrace();
        }
        catch(SocketException ex){
          System.out.println("Error with connection. try again.");
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
