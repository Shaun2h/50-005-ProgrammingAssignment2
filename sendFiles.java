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
        try{
            this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe to client");//debug message
            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe from client");//debug message
        }
        catch(IOException ex){
            System.out.println("failed!");
        }
    }

    public boolean sendPlainFile(String file_loc){
        try{





            this.cert_FileInputStream = new FileInputStream(file_loc);
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream);
            //open the file streams for your documents to be sent. / the file to be sent. Anything really.


            byte[] buffer = new byte[33600];
            TimeUnit.MILLISECONDS.sleep(10);
            this.PipetoClient.writeInt(0);
            //System.out.println("initiated File sending by sending int 0 over"); //debug message
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            //System.out.println("wrote file name length over.");//debug message
            this.PipetoClient.write(file_loc.getBytes());
            this.PipetoClient.flush();
            //System.out.println("wrote the file name over");//debug message
            //inform them of file name..
            File file_being_sent = new File(file_loc);
            this.PipetoClient.writeLong(file_being_sent.length());
            int no_of_bytes_sent; //tell them how many bytes was sent...
            int total_bytes_sent=0;
            while (total_bytes_sent<file_being_sent.length()) {
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.

                //TimeUnit.MILLISECONDS.sleep(1);//give them time to write out the next bit. just in case.
                this.PipetoClient.writeInt(buffer.length); //tell them the array length..
                this.PipetoClient.writeInt(no_of_bytes_sent); //tell them the actual useful bytes to write from the array
                this.PipetoClient.write(buffer);
                this.PipetoClient.flush(); //FORCE all things in buffer to be written out.

                total_bytes_sent+=no_of_bytes_sent; //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.


                //System.out.println("sent one packet over");//debug message
                //TimeUnit.MILLISECONDS.sleep(1);
            }
            //PipetoClient.writeInt(2);
            PipetoClient.flush();
            PipeFromClient.readInt();//wait for them to finish processing.
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("Ending sending of unencrypted file...");

            return true; //it was a success.

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
        return false; //if i fail the sending, instantly return false

    }



    public void send_File_With_certs_key(String file_loc, String their_cert_location){ //A method to send things with public key encryption
        try{
            int byte_Array_Size = 117;
            /*this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe to client");

            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe from client");*/

            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //certificate factory
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey(); //their public key extracted from the cert.
            //KEEP IN MIND THIS NEVER MEANS THAT THE PUBLIC KEY IS VALID.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key);

            //Cipher is now ready for use..
            this.PipetoClient.writeInt(0);
            //System.out.println("initiated File sending by sending Int 0 over...");//debug message
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            //System.out.println("wrote file name length over");//debug message
            this.PipetoClient.write(file_loc.getBytes());
            //System.out.println("Wrote file name over..");//debug message
            //this.PipetoClient.flush();
            //finished informing them of file name...



            //open their file and the required channels.
            this.cert_FileInputStream = new FileInputStream(file_loc); //open the file to be sent
            this.bufferedInputStreamForFile = new BufferedInputStream(this.cert_FileInputStream); //convert to buffered.
            byte[] buffer = new byte[byte_Array_Size];
            byte[] tosend;
            File file_being_sent = new File(file_loc);
            int totalbytes_to_besent = (int) Math.ceil((double) file_being_sent.length()/117)*128; //now we know how many bytes will be sent over.

            //System.out.println(file_being_sent.length());

            this.PipetoClient.writeLong(totalbytes_to_besent); //MATH OF TOTAL BYTES SENT IN THE END IS ABOVE.
            //told them how many bytes will be sent over, total.


            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //tell them how many bytes was sent in this round.


            while (total_bytes_sent<totalbytes_to_besent) {
                buffer = new byte[byte_Array_Size];
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);
                tosend = cipher.doFinal(buffer); //this will result in your 128 array from your 117 array


                //System.out.println("Total bytes sent over - "+ tosend.length); //for debug


                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.

                this.PipetoClient.writeInt(no_of_bytes_sent); //You are always sending arrays of 128 with padding. Send the actual number of characters to write.
                //System.out.println(tosend.length);
                this.PipetoClient.write(tosend);
                //System.out.println(tosend);

                this.PipetoClient.flush(); //ensure that it is written out, not stored in the buffer.

                total_bytes_sent+=tosend.length;
                 //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.

                //System.out.println("sent one packet over-via their public key"); //for debug
                //TimeUnit.MILLISECONDS.sleep(2);
                //System.out.println(total_bytes_sent);
            }
            this.bufferedInputStreamForFile.close();
            theircert.close();
            this.cert_FileInputStream.close(); //close the input stream of the file.
            System.out.println("Ending sending off file encrypted with someone's public key");
            this.PipeFromClient.readInt();//wait for them to finish processing
            System.out.println("Server has signalled completed receiving file. ending.");

        }
        /*catch(InterruptedException ex){
            System.out.println("Interrupted...?");
            ex.printStackTrace();
        }*/
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
            System.out.println("Generation complete.");
            /*this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe to client"); //debug message
            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe from client"); //debug message
            */

            InputStream theircert = new FileInputStream(their_cert_location); //open their certificate that was sent over.
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); //certificate factory
            X509Certificate receivedCert = (X509Certificate) cf.generateCertificate(theircert); //their cert
            PublicKey their_public_key = receivedCert.getPublicKey(); //their public key extracted from the cert.
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE,their_public_key); //initialise cipher
            //Cipher is now ready for use..

            System.out.println("Beginning to send session key over.");
            this.PipetoClient.writeInt(0);
            //System.out.println("initiated File sending by sending Int 0 over..."); //debug message
            byte[] byte_Array_To_Send = cipher.doFinal(session_Key.getEncoded());

            TimeUnit.MILLISECONDS.sleep(1); //just to let them read it, in case of conflict. this said conflict shouldn't happen, but it did happen a few times for me.

            this.PipetoClient.writeInt(byte_Array_To_Send.length); //tell them how long this key array is. although its gonna be 128 because it's RSA encrypted AES.


            //System.out.println("wrote array length over.."); //debug message
            //System.out.println("Array Length is currently: " + byte_Array_To_Send.length); //debug message
            //finished informing them of file name...
            Long total_bytes_sent= new Long(0);
            int no_of_bytes_sent; //holder value so i can tell them how many bytes was sent...

            while (total_bytes_sent<byte_Array_To_Send.length) {
                no_of_bytes_sent = byte_Array_To_Send.length;
                //System.out.println("Total bytes sent over - "+ no_of_bytes_sent); //debug message

                /*for (byte asd: byte_Array_To_Send){
                    System.out.print(asd);   //if you need to see the byte array, uncomment this entire portion.
                }
                System.out.println("");*/

                this.PipeFromClient.readInt();
                this.PipetoClient.write(byte_Array_To_Send);
                //System.out.println("sent aes to client"); //debug message
                total_bytes_sent+=no_of_bytes_sent;
                this.PipetoClient.flush();
                //tells me if there were less bytes sent then the total file buffer, meaning i touched the end of file.
                //System.out.println("sent one packet over-via their public key"); //debug message
                TimeUnit.MILLISECONDS.sleep(60);
            }
            System.out.println("Ending sending off session key encrypted with someone's public key");
            TimeUnit.MILLISECONDS.sleep(100);
            return session_Key; //return this for future use.


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
        return null; //well you didn't exactly suceed in making that key or sending it over..
    }
    public void send_File_With_AES(String file_loc,Key session_Key){ //A method to send things with private encryption
        try{
            IvParameterSpec iv =null;
            this.PipetoClient= new DataOutputStream(this.receipient.getOutputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe to client"); //debug message
            TimeUnit.SECONDS.sleep(1);
            this.PipeFromClient= new DataInputStream(this.receipient.getInputStream());//send data to here to talk to opponent party.}
            //System.out.println("Created pipe from client"); //debug message
            Cipher cipher_private = Cipher.getInstance("AES/ECB/PKCS5Padding"); //initialise cipher
            cipher_private.init(Cipher.ENCRYPT_MODE,session_Key,iv); //make a cipher with your private key...
            //ready to begin sending...
            //System.out.println(file_loc);
            this.PipetoClient.writeInt(0);
            //System.out.println("initiated File sending by sending int 0 over"); //debug message
            this.PipetoClient.writeInt(file_loc.getBytes().length);
            /*for(byte b: file_loc.getBytes()){
                System.out.print(b);
            }
            System.out.println("");*/
            //System.out.println("wrote file name length over."); //debug message
            this.PipetoClient.write(file_loc.getBytes());
            //System.out.println("wrote the file name over"); //debug message

            //informing of File name completed...
            File file_being_sent = new File(file_loc); //open up the file i want to send.
            int byte_Array_Size = 100800;
            int number_of_blocks =(int) Math.ceil((double)file_being_sent.length()/byte_Array_Size);
            int totalbytes_to_besent = (int) Math.ceil(( (double) byte_Array_Size/16 +1 )) *16*number_of_blocks ;
            //math to calculate the total number of blocks i'll be sending  over.
            //essentially: how many blocks will be sent over?
            //             how many bytes are in a single block? * total number of blocks
            // this gives me the total number of bytes i'll be sending over.


            this.bufferedInputStreamForFile = new BufferedInputStream(new FileInputStream(file_being_sent)); //new buffered input stream for the wanted file.
            byte[] buffer = new byte[byte_Array_Size];
            byte[] after_process; //byte array to hold the result of a decryption

            //Obtain the File being sent...
            //System.out.println(totalbytes_to_besent);
            this.PipetoClient.writeLong(totalbytes_to_besent);
            //inform total file length...

            Long total_bytes_sent= new Long(0);
            TimeUnit.MICROSECONDS.sleep(2);
            int no_of_bytes_sent; //tell them how many bytes was sent...
            while (total_bytes_sent<totalbytes_to_besent) {
                buffer = new byte[byte_Array_Size];
                no_of_bytes_sent = this.bufferedInputStreamForFile.read(buffer);

                after_process = cipher_private.doFinal(buffer); //decrypt

                this.PipetoClient.writeInt(1); //signal to them that i'm sending a part of the file.
                this.PipetoClient.writeInt(after_process.length); //tell them buffer size
                this.PipetoClient.writeInt(no_of_bytes_sent); //tell them the byte array length
                this.PipetoClient.write(after_process); //write bytearray out
                //System.out.println(after_process.length);
                //System.out.println(no_of_bytes_sent);
                this.PipetoClient.flush(); //FORCE it to be written out.
                /*for (byte b: after_process){
                System.out.print(b); //uncomment me to see what bytes are being written out
                }
                System.out.println("");*/

                total_bytes_sent+=after_process.length;//update the total number of bytes i've sent.

                //System.out.println("sent one packet over encrypted with session key");
                //System.out.println(total_bytes_sent);
                if(total_bytes_sent>totalbytes_to_besent){System.out.println("finished byte requirement.");} //just to inform you that i've finished the byte requirement and sent it all. uncomment if you want to know.
                //TimeUnit.MICROSECONDS.sleep(1);
            }
            System.out.println("sent all data");
            this.PipetoClient.flush(); //Just in case something hasn't been written out.
            this.PipeFromClient.readInt(); //wait for signal
            System.out.println("Received signal that server has completed processing.");
            this.bufferedInputStreamForFile.close();
            this.cert_FileInputStream.close();
            //close the input stream of the file.

            System.out.println("Ending off sending of file with session key encryption...");
            return;
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
        System.out.println("FILE SENDING FAILED!"); //well just so you know really.
        return;


    }
}
