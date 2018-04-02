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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;


public class receiveFiles {
    private Socket sender;
    private FileOutputStream fileoutput;
    private BufferedOutputStream bufferoutputstream;
    private DataInputStream dataInputStream;
    public receiveFiles(Socket fromwho){
        this.sender = fromwho;
    }
    public String recievePlainFile(String saveLocation) {
        // If the packet is for transferring the filename
        String returnvalue=null;
        try {
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            Long Length_of_File = new Long(10);
            Long totalBytesSent=new Long(0);
            while(totalBytesSent<Length_of_File) {
                int packetType = this.dataInputStream.readInt();
                System.out.println("Packet type:" + packetType);
                if (packetType == 0) {

                    System.out.println("Receiving unencrypted file...");
                    int numBytes = this.dataInputStream.readInt();
                    System.out.println("Received number of bytes for file name");
                    byte[] filename = new byte[numBytes];
                    this.dataInputStream.read(filename);
                    returnvalue = saveLocation + new String(filename, 0, numBytes);
                    this.fileoutput = new FileOutputStream(saveLocation + new String(filename, 0, numBytes));
                    System.out.println(returnvalue);
                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput);
                    // If the packet is for transferring a chunk of the file
                    Length_of_File = this.dataInputStream.readLong();

                } else if (packetType == 1) {

                    int numBytes = this.dataInputStream.readInt();
                    byte[] block = new byte[numBytes];
                    this.dataInputStream.read(block);
                    totalBytesSent += numBytes;
                    if (numBytes > 0) {
                        this.bufferoutputstream.write(block, 0, numBytes);
                        this.bufferoutputstream.flush();
                    }
                    System.out.println("Received a round of packets");
                    }

            }
            System.out.println("Reception of unencrypted file is complete.");
            if (this.bufferoutputstream != null) {
                this.bufferoutputstream.close();
            }
            if (this.fileoutput != null) {
                this.fileoutput.close();
            }

        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return returnvalue;
    }
    public String recieveEncryptedWith_public(String saveLocation, String my_key_loc) { //so we need to decode with our private key.
        // If the packet is for transferring the filename
        String returnvalue=null;
        try {
            File my_key_file = new File(my_key_loc);
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            Long Length_of_File = new Long(10);
            Long totalBytesSent=new Long(0);
            BufferedInputStream key_File_Buffered_Input_Stream = new BufferedInputStream( new FileInputStream(my_key_file));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] private_key_bytes = new byte[(int) my_key_file.length() ]; //obtain a byte array that can hold my entire key.
            key_File_Buffered_Input_Stream.read(private_key_bytes); //read the entire file into this array..
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(private_key_bytes);
            PrivateKey my_Private_key = kf.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,my_Private_key); //make a cipher with your private key...
            byte[] answers;
            while(totalBytesSent<Length_of_File) {
                int packetType = this.dataInputStream.readInt();
                if (packetType == 0) {

                    System.out.println("Receiving file that was encrypted with my public key....");
                    int numBytes = this.dataInputStream.readInt();
                    byte[] filename = new byte[numBytes];
                    this.dataInputStream.read(filename);
                    returnvalue = saveLocation + new String(filename, 0, numBytes);
                    this.fileoutput = new FileOutputStream(saveLocation + new String(filename, 0, numBytes));
                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput);
                    // If the packet is for transferring a chunk of the file
                    Length_of_File = this.dataInputStream.readLong();
                } else if (packetType == 1) {
                    int numBytes = this.dataInputStream.readInt();
                    byte[] block = new byte[numBytes];
                    this.dataInputStream.read(block);
                    System.out.println("Received byte array of length (Before Decryption) - " + block.length);
                    answers = cipher.doFinal(block);
                    System.out.println("Received byte array of length (After Decryption) - " + answers.length);
                    if (numBytes > 0) {
                        System.out.println(new String(answers));
                        this.bufferoutputstream.write(answers, 0, 117); //hard coded to proper array size. it should be 117 after decryption
                    }
                    totalBytesSent+=128;

                    System.out.println("Received a round of packets -public key encrypted type");

                }
            }
            System.out.println("File received.. it was encrypted with my public key");

            if (this.bufferoutputstream != null) {
                this.bufferoutputstream.close();
            }
            if (this.fileoutput != null) {
                this.fileoutput.close();
            }
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            ex.printStackTrace();
        }
        catch(BadPaddingException ex){
            System.out.println("BadPadding Exception");
            ex.printStackTrace();
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception");
            ex.printStackTrace();
        }
        catch(NoSuchPaddingException ex){
            System.out.println("NoSuchPadding Exception");
            ex.printStackTrace();
        }
        catch(InvalidKeySpecException ex){
            System.out.println("InvalidKeySpecException");
            ex.printStackTrace();
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("NoSuchAlgorithm Exception");
            ex.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("IOException");
            ex.printStackTrace();
        }
        return returnvalue;
    }
    public String recieveEncryptedWith_private(String saveLocation, String their_Cert_Loc) {
        //So we need to decode with their public key ie their cert.
        // If the packet is for transferring the filename
        String returnvalue=null;
        try {
            InputStream theirCert = new FileInputStream(new File(their_Cert_Loc));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate their_Certificate = (X509Certificate) cf.generateCertificate(theirCert); //made their cert
            PublicKey their_Public_Key = their_Certificate.getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,their_Public_Key);
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            Long Length_of_File = new Long(10);
            byte[] answer;
            Long totalBytesSent=new Long(0);
            while(totalBytesSent<Length_of_File) {
                int packetType = this.dataInputStream.readInt();
                if (packetType == 0) {

                    System.out.println("Receiving file that was encrypted with someone else's private key.");
                    int numofBytes = this.dataInputStream.readInt();
                    byte[] filename_Buffer = new byte[numofBytes];
                    this.dataInputStream.read(filename_Buffer);
                    returnvalue = saveLocation + new String(filename_Buffer, 0, numofBytes);
                    this.fileoutput = new FileOutputStream(saveLocation + new String(filename_Buffer, 0, numofBytes));
                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput);
                    // If the packet is for transferring a chunk of the file
                    Length_of_File = this.dataInputStream.readLong();

                } else if (packetType == 1) {
                    int numBytes = this.dataInputStream.readInt();
                    byte[] block = new byte[numBytes];
                    this.dataInputStream.read(block);
                    answer = cipher.doFinal(block);
                    if (numBytes > 0) {
                        this.bufferoutputstream.write(answer, 0, numBytes);
                    }
                    System.out.println("Received a round of packets - private key encrypted type");
                }
            }
            System.out.println("File received -it was encrypted with private");
            if (this.bufferoutputstream != null) {
                this.bufferoutputstream.close();
            }
            if (this.fileoutput != null) {
                this.fileoutput.close();
            }

        }
        catch(BadPaddingException ex){
            System.out.println("Bad Padding Exception");
            ex.printStackTrace();
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            ex.printStackTrace();
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception");
            ex.printStackTrace();
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("No such Algorithm Exception");
            ex.printStackTrace();
        }
        catch(NoSuchPaddingException ex){
            System.out.println("No such padding exception");
            ex.printStackTrace();
        }
        catch(CertificateException ex){
            System.out.println("Certificate Exception occurred.");
            ex.printStackTrace();
        }
        catch(IOException ex){
            System.out.println("IOException Occurred.");
            ex.printStackTrace();
        }
        return returnvalue;
    }
}
