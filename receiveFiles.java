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

                System.out.println("Receiving unencrypted file...");
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

                System.out.println("Reception of unencrypted file is complete.");

                if (this.bufferoutputstream != null) this.bufferoutputstream.close();
                if (this.fileoutput != null) this.fileoutput.close();
            }
        }
        catch(IOException ex){
            printStackTrace(ex);
        }
    }
    public void recieveEncryptedWith_public(String saveLocation, String my_key_loc) { //so we need to decode with our private key.
        // If the packet is for transferring the filename

        try {
            File my_key_file = new File(my_key_loc);
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            BufferedInputStream key_File_Buffered_Input_Stream = new BufferedInputStream( new FileInputStream(my_key_file));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] private_key_bytes = new byte[(int) my_key_file.length() ]; //obtain a byte array that can hold my entire key.
            key_File_Buffered_Input_Stream.read(private_key_bytes); //read the entire file into this array..
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(private_key_bytes);
            PrivateKey my_Private_key = kf.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,my_Private_key); //make a cipher with your private key...

            int packetType = this.dataInputStream.readInt();
            if (packetType == 0) {

                System.out.println("Receiving file that was encrypted with my public key....");
                int numBytes = this.dataInputStream.readInt();
                byte[] filename = new byte[numBytes];
                this.dataInputStream.read(filename);
                this.fileoutput = new FileOutputStream(saveLocation + new String(filename, 0, numBytes));
                this.bufferoutputstream= new BufferedOutputStream(this.fileoutput);
                // If the packet is for transferring a chunk of the file

            }
            else if (packetType == 1) {
                int numBytes = this.dataInputStream.readInt();
                byte[] block = new byte[numBytes];
                this.dataInputStream.read(block);
                cipher.doFinal(block);
                if (numBytes > 0)
                    this.bufferoutputstream.write(block, 0, numBytes);
                System.out.print(""); //just to avoid duplicated code warning. I mean i get that it's the same code but i don't need to know all the time. Thanks tho intellij. i guess

            }
            else if (packetType == 2) {

                System.out.println("File received.. it was encrypted with my public key");

                if (this.bufferoutputstream != null) this.bufferoutputstream.close();
                if (this.fileoutput != null) this.fileoutput.close();
            }
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            printStackTrace(ex);
        }
        catch(BadPaddingException ex){
            System.out.println("BadPadding Exception");
            printStackTrace(ex);
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception");
            printStackTrace(ex);
        }
        catch(NoSuchPaddingException ex){
            System.out.println("NoSuchPadding Exception");
            printStackTrace(ex);
        }
        catch(InvalidKeySpecException ex){
            System.out.println("InvalidKeySpecException");
            printStackTrace(ex);
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("NoSuchAlgorithm Exception");
            printStackTrace(ex);
        }
        catch(IOException ex){
            System.out.println("IOException");
            printStackTrace(ex);
        }
    }
    public void recieveEncryptedWith_private(String saveLocation, String their_Cert_Loc) {
        //So we need to decode with their public key ie their cert.
        // If the packet is for transferring the filename
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

            int packetType = this.dataInputStream.readInt();
            if (packetType == 0) {

                System.out.println("Receiving file that was encrypted with someone else's private key.");
                int numofBytes = this.dataInputStream.readInt();
                byte[] filename_Buffer = new byte[numofBytes];
                this.dataInputStream.read(filename_Buffer);
                this.fileoutput = new FileOutputStream(saveLocation + new String(filename_Buffer, 0, numofBytes));
                this.bufferoutputstream= new BufferedOutputStream(this.fileoutput);
                // If the packet is for transferring a chunk of the file

            } else if (packetType == 1) {
                int numBytes = this.dataInputStream.readInt();
                byte[] block = new byte[numBytes];
                this.dataInputStream.read(block);
                cipher.doFinal(block);
                if (numBytes > 0)
                    this.bufferoutputstream.write(block, 0, numBytes);

            } else if (packetType == 2) {
                System.out.println("File received -it was encrypted with private");
                if (this.bufferoutputstream != null) this.bufferoutputstream.close();
                if (this.fileoutput != null) this.fileoutput.close();
            }
        }
        catch(BadPaddingException ex){
            System.out.println("Bad Padding Exception");
            printStackTrace(ex);
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            printStackTrace(ex);
        }
        catch(InvalidKeyException ex){
            System.out.println("Invalid Key Exception");
            printStackTrace(ex);
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("No such Algorithm Exception");
            printStackTrace(ex);
        }
        catch(NoSuchPaddingException ex){
            System.out.println("No such padding exception");
            printStackTrace(ex);
        }
        catch(CertificateException ex){
            System.out.println("Certificate Exception occurred.");
            printStackTrace(ex);
        }
        catch(IOException ex){
            System.out.println("IOException Occurred.");
            printStackTrace(ex);
        }
    }
}
