import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.TimeUnit;


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
                    TimeUnit.MILLISECONDS.sleep(10);
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
        catch(InterruptedException ex){
            ex.printStackTrace();
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

                    //System.out.println("Receiving file that was encrypted with my public key....");
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
                    //System.out.println("Received byte array of length (Before Decryption) - " + block.length);
                    answers = cipher.doFinal(block);
                    //System.out.println("Received byte array of length (After Decryption) - " + answers.length);
                    if (numBytes > 0) {
                        System.out.println(new String(answers));
                        this.bufferoutputstream.write(answers, 0, 117); //hard coded to proper array size. it should be 117 after decryption
                    }
                    totalBytesSent+=128;
                    TimeUnit.MILLISECONDS.sleep(20);

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
        catch(InterruptedException ex){
            System.out.println("INTERRUPTED EXCEPTION");
            ex.printStackTrace();
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
    public Key receive_SessionKey_With_MY_key(String my_key_loc) { //A method to send things with public key encryption
        Key returnvalue = null;
        try {
            File my_key_file = new File(my_key_loc);
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            DataOutputStream output = new DataOutputStream(this.sender.getOutputStream());
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
            //Cipher to decrypt is now ready.
            this.dataInputStream.readInt(); //Receive the 0
            System.out.println("Receiving file that was encrypted with my public key....");
            int numBytes = this.dataInputStream.readInt();
            System.out.println("Received total byte array size: " + numBytes);
            byte[] block = new byte[128];
            byte[] answers;
            while(this.dataInputStream.available()>0){
                this.dataInputStream.read(block);
            }
            output.writeInt(0);
            this.dataInputStream.read(block);
            System.out.println("Received byte array of length (Before Decryption) - " + block.length);
            for( byte v:block){
                System.out.print(v);
            }
            System.out.println("");
            answers = cipher.doFinal(block);
            //decrypt the byte array...
            System.out.println("Received byte array of length (After Decryption) - " + answers.length);
            System.out.println("Received a round of packets -public key encrypted type");
            System.out.println("File received.. it was encrypted with my public key");
            Key key = new SecretKeySpec(answers,0,answers.length,"AES"); //re-obtain key
            return key;
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
    public String recieveEncryptedWith_AES(String saveLocation, Key Session_key) {
        //So we need to decode with their public key ie their cert.
        // If the packet is for transferring the filename
        String returnvalue=null;
        byte[] block=null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            IvParameterSpec iv = null;
            cipher.init(Cipher.DECRYPT_MODE,Session_key,iv);
            if (this.dataInputStream == null) {
                this.dataInputStream = new DataInputStream(this.sender.getInputStream());//send data to here to talk to opponent party.}
            }
            Long Length_of_File = new Long(10);
            byte[] answer;

            this.bufferoutputstream=null;
            Long totalBytesSent=new Long(0);
            while(totalBytesSent<Length_of_File) {
                int packetType = this.dataInputStream.readInt();
                if (packetType == 0) {

                    System.out.println("Receiving file that was encrypted with AES key.");
                    int numofBytes = this.dataInputStream.readInt();
                    byte[] filename_Buffer = new byte[numofBytes];
                    this.dataInputStream.read(filename_Buffer);
                    returnvalue = saveLocation + new String(filename_Buffer, 0, numofBytes);
                    this.fileoutput = new FileOutputStream(returnvalue);
                    System.out.println(returnvalue);
                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput);
                    // If the packet is for transferring a chunk of the file
                    Length_of_File = this.dataInputStream.readLong();

                } else if (packetType == 1) {
                    int numBytes = this.dataInputStream.readInt();
                    block = new byte[numBytes];
                    TimeUnit.MILLISECONDS.sleep(90);
                    this.dataInputStream.read(block);
                    //System.out.println("int-" + numBytes + " block len - "+ block.length);
                    answer = cipher.doFinal(block);
                    if (answer.length > 0) {
                        this.bufferoutputstream.write(answer, 0, answer.length);
                        //this.fileoutput.write(answer, 0, numBytes);
                    }
                    totalBytesSent+=answer.length;
                    System.out.println("Received a round of packets - session key encrypted type");
                }
            }
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("File received -it was encrypted with private");
            if (this.bufferoutputstream != null) {
                this.bufferoutputstream.close();
            }
            if (this.fileoutput != null) {
                this.fileoutput.close();
            }
            TimeUnit.SECONDS.sleep(5);
        }
        catch(InvalidAlgorithmParameterException ex){
            System.out.println("Invalid Algorithm Parameter Exception");
            ex.printStackTrace();
        }
        catch(InterruptedException ex){
            System.out.println("Interrupted exception in receive AES encrypted file");
            ex.printStackTrace();
        }
        catch(BadPaddingException ex){
            System.out.println("Bad Padding Exception");
            System.out.println(block.length);
            for(byte b: block){
                System.out.print(b);
            }
            System.out.println("");
            ex.printStackTrace();
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            System.out.println(block.length);
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
        catch(SocketException ex){
            System.out.println("completed...");
            return returnvalue;
        }
        catch(IOException ex){
            System.out.println("IOException Occurred.");
            ex.printStackTrace();
        }
        return returnvalue;
    }
}
