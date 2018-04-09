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
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    public receiveFiles(Socket fromwho){
        this.sender = fromwho;
        try{
            this.dataInputStream = new DataInputStream(this.sender.getInputStream());
            this.dataOutputStream = new DataOutputStream(this.sender.getOutputStream()); //allows you to fire back that you are done.
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    public String recievePlainFile(String saveLocation) {
        // If the packet is for transferring the filename
        String returnvalue=null;
        try {
            Long Length_of_File = new Long(10);
            Long totalBytesSent=new Long(0);
            while(totalBytesSent<Length_of_File) {
                int packetType = this.dataInputStream.readInt();
                //System.out.println("Packet type:" + packetType); //debug message
                if (packetType == 0) {

                    System.out.println("Receiving unencrypted file...");
                    int numBytes = this.dataInputStream.readInt();
                    //System.out.println("Received number of bytes for file name"); debug message


                    TimeUnit.MILLISECONDS.sleep(10);
                    //You HAVE to sleep. because it takes time on the other computer to write stuff over. If your delay is insufficient, coupled with a slow network,
                    //your packet being sent will be cut off. i.e. you'll read "valid part-000000000000000000000000000" where it literally says 0 because that bit wasn't written/sent over yet.

                    byte[] filename = new byte[numBytes];
                    this.dataInputStream.read(filename);

                    returnvalue = saveLocation + new String(filename, 0, numBytes); //now get the return value, which is where the file is located/it's name!!)
                    this.fileoutput = new FileOutputStream(saveLocation + new String(filename, 0, numBytes)); //open a file stream to that place

                    //System.out.println(returnvalue); //debug message so that you can see what the actual file name is...

                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput); //open stream


                    Length_of_File = this.dataInputStream.readLong(); //obtain total file length

                } else if (packetType == 1) {
                    int numBytes = this.dataInputStream.readInt(); //get total length of array sent over.

                    TimeUnit.MILLISECONDS.sleep(5);
                    //You HAVE to sleep. because it takes time on the other computer to write stuff over. If your delay is insufficient, coupled with a slow network,
                    //your packet being sent will be cut off. i.e. you'll read "valid part-000000000000000000000000000" where it literally says 0 because that bit wasn't written/sent over yet.

                    byte[] block = new byte[numBytes]; //create a new array using said length
                    int towrite=this.dataInputStream.readInt(); //obtain the actually useful bytes
                    this.dataInputStream.read(block); //read array.


                    totalBytesSent += numBytes;
                    if (numBytes > 0) {
                        this.bufferoutputstream.write(block, 0, towrite);
                        this.bufferoutputstream.flush(); //Write the bytes out.
                    }
                    //System.out.println("Received a round of packets"); //debug message.
                    }

            }
            System.out.println("Reception of unencrypted file is complete.");
            this.dataOutputStream.writeInt(3);
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
        // A method for receiving files.
        String returnvalue=null;
        byte[] block=null;
        try {
            File my_key_file = new File(my_key_loc); //open your private key file.
            Long Length_of_File = new Long(10);
            Long totalBytesSent=new Long(0);


            BufferedInputStream key_File_Buffered_Input_Stream = new BufferedInputStream( new FileInputStream(my_key_file));

            KeyFactory kf = KeyFactory.getInstance("RSA");

            byte[] private_key_bytes = new byte[(int) my_key_file.length() ]; //obtain a byte array that can hold my entire key.
            key_File_Buffered_Input_Stream.read(private_key_bytes); //read the entire file into this array..

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(private_key_bytes); //generate a keyspec.
            PrivateKey my_Private_key = kf.generatePrivate(keySpec); //now make your key.

            //initialise cipher
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,my_Private_key); //make a cipher with your private key...


            byte[] answers; //a byte array to hold your answers after decryption.
            while(totalBytesSent<Length_of_File) { //while i'm not done receiving everything...
                int packetType = this.dataInputStream.readInt();
                //System.out.println(packetType); //allows you to check packet types
                if (packetType == 0) {

                    System.out.println("Receiving file that was encrypted with my public key....");

                    int numBytes = this.dataInputStream.readInt();
                    //note the total array size that will be sent over.
                    byte[] filename = new byte[numBytes];
                    //generate said array
                    TimeUnit.MILLISECONDS.sleep(20);
                    //sleep to allow for network transmissions/lag time while writing.


                    this.dataInputStream.read(filename);
                    //read the name of the file over into your byte array.

                    returnvalue = saveLocation + new String(filename, 0, numBytes);
                    //now you have the return value, which is where this file is located.

                    this.fileoutput = new FileOutputStream(saveLocation + new String(filename, 0, numBytes));
                    //Now you have your file stream
                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput);
                    //buffered file output stream made.

                    Length_of_File = this.dataInputStream.readLong(); //use this to tell how many bytes are meant to be sent over.


                } else if (packetType == 1) {
                    int numBytes = this.dataInputStream.readInt();
                    block = new byte[128];
                    //TimeUnit.MILLISECONDS.sleep(50); //give them time to write over..

                    this.dataInputStream.read(block);
                    //System.out.println("Received byte array of length (Before Decryption) - " + block.length);
                    answers = cipher.doFinal(block);
                    //System.out.println("Received byte array of length (After Decryption) - " + answers.length);

                    if (numBytes > 0) {
                        //System.out.println(new String(answers)); //debug message.
                        this.bufferoutputstream.write(answers, 0, numBytes); // numBytes varies. because it's a pointer to the total number of useful bytes.
                    }

                    totalBytesSent+=block.length;//note the constant amount of 128 is due to each being a block of RSA encoded stuff.
                    //System.out.println(totalBytesSent); //lets you know the current total bytes received
                    /*for (byte b: answers){
                        System.out.print(b); //debug message to allow you to see bytes being sent in
                    }
                    System.out.println("");*/
                    //System.out.println(answers.length); //debug message to see the decrypted amount's length
                    TimeUnit.MICROSECONDS.sleep(2);
                    //give them time to write.


                    //System.out.println("Received a round of packets -public key encrypted type"); //debug message

                }
            }
            System.out.println("File received.. it was encrypted with my public key");
            this.dataOutputStream.writeInt(1); //tell them i completed receiving!
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
            System.out.println(block.length);
            for(byte b: block){
                System.out.print(b);  //so that you can see where it got cut off.
            }
            System.out.println("");
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
        //a method specifically for getting AES keys that were sent over after encoding via my private key.

        Key returnvalue = null; //return value here is a KEY!

        byte[] block= null;
        try {
            File my_key_file = new File(my_key_loc);

            DataOutputStream output = new DataOutputStream(this.sender.getOutputStream());


            Long Length_of_File = new Long(10);
            Long totalBytesSent=new Long(0);


            BufferedInputStream key_File_Buffered_Input_Stream = new BufferedInputStream( new FileInputStream(my_key_file)); //open my private key

            KeyFactory kf = KeyFactory.getInstance("RSA");
            byte[] private_key_bytes = new byte[(int) my_key_file.length() ]; //obtain a byte array that can hold my entire key.
            key_File_Buffered_Input_Stream.read(private_key_bytes); //read the entire file into this array..
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(private_key_bytes); //generate a keyspec.
            PrivateKey my_Private_key = kf.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); //initialise cipher
            cipher.init(Cipher.DECRYPT_MODE,my_Private_key); //make a cipher with your private key...
            //Cipher to decrypt is now ready.


            this.dataInputStream.readInt(); //Receive the 0, informing me that they are beginning to send stuff over.


            System.out.println("Receiving file that was encrypted with my public key....");
            int numBytes = this.dataInputStream.readInt();


            //System.out.println("Received total byte array size: " + numBytes);
            block = new byte[128];
            byte[] answers; // array to hold answers.

            while(this.dataInputStream.available()>0){ //while there are still bytes to read...
                //basically CLEAN THE STREAM.
                this.dataInputStream.read(block);
            }


            output.writeInt(0); //signal ready to receive.
            TimeUnit.MILLISECONDS.sleep(2); //give them the time to write over and send.

            this.dataInputStream.read(block); //read the block of data.

            /*System.out.println("Received byte array of length (Before Decryption) - " + block.length);
            for( byte v:block){
                System.out.print(v); //uncomment to see the before decryption bit.
            }
            System.out.println("");
            */


            answers = cipher.doFinal(block);
            //decrypt the byte array...


            //System.out.println("Received byte array of length (After Decryption) - " + answers.length); //debug message
            //System.out.println("Received a round of packets -public key encrypted type"); //debug message

            System.out.println("File received.. it was encrypted with my public key");
            Key key = new SecretKeySpec(answers,0,answers.length,"AES"); //re-obtain key that was sent over.
            return key; //will be null on failure on any step above. see what's after all the catch blocks
        }
        catch(InterruptedException ex){
            System.out.println("Interrupted");
            ex.printStackTrace();
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            ex.printStackTrace();
        }
        catch(BadPaddingException ex){
            System.out.println("BadPadding Exception");
            System.out.println(block.length);
            for(byte b: block){
                System.out.print(b);  //so that you can see where it got cut off.
            }
            System.out.println(" ");
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
        return returnvalue;//will be null on failure on any step above.
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
            Long Length_of_File = new Long(10);
            byte[] answer;

            this.bufferoutputstream=null;
            Long totalBytesSent=new Long(0);
            while(totalBytesSent<Length_of_File) {
                int packetType = this.dataInputStream.readInt();
                //System.out.println(packetType);
                if (packetType == 0) {

                    System.out.println("Receiving file that was encrypted with AES key.");

                    int numofBytes = this.dataInputStream.readInt();
                    TimeUnit.MICROSECONDS.sleep(2); //just give them time to write over. I've been cut off because of my bad house internet before.

                    byte[] filename_Buffer = new byte[numofBytes];
                    this.dataInputStream.read(filename_Buffer);
                    //for(byte b: filename_Buffer){System.out.print(b);}System.out.println(""); //debug. print your bytes that you received.
                    returnvalue = saveLocation + new String(filename_Buffer); //calculate your file name
                    this.fileoutput = new FileOutputStream(returnvalue); //create file output stream
                    System.out.println("File will be saved in: "+returnvalue); //debug message

                    this.bufferoutputstream = new BufferedOutputStream(this.fileoutput);
                    // If the packet is for transferring a chunk of the file
                    //TimeUnit.MICROSECONDS.sleep(2);
                    Length_of_File = this.dataInputStream.readLong(); //we need to know the total bytes we should be receiving.
                    //System.out.println(Length_of_File); //If you need to see the file length. Allows you to debug your math, if you calculated the final total size to be sent over wrongly.
                    //System.out.println(totalBytesSent);
                } else if (packetType == 1) {
                    int numBytes = this.dataInputStream.readInt();
                    block = new byte[128];


                    TimeUnit.MICROSECONDS.sleep(2);
                    //You HAVE to sleep. because it takes time on the other computer to write stuff over. If your delay is insufficient, coupled with a slow network,
                    //your packet being sent will be cut off. i.e. you'll read "valid part-000000000000000000000000000" where it literally says 0 because that bit wasn't written/sent over yet.

                    this.dataInputStream.read(block);
                    //System.out.println("int-" + numBytes + " block len - "+ block.length);

                    answer = cipher.doFinal(block);
                    if (answer.length > 0) {
                        this.bufferoutputstream.write(answer, 0, numBytes);
                        //System.out.println("wrote"); //debug. so you know you wrote.
                    }

                    totalBytesSent+=block.length;
                    //System.out.println(totalBytesSent); //lets you know how many bytes you received thus far.
                    //System.out.println("Received a round of packets - session key encrypted type");
                }
            }
            TimeUnit.MILLISECONDS.sleep(100);
            System.out.println("File received -it was encrypted with AES");
            this.dataOutputStream.writeInt(1); //signal reception complete

            if (this.bufferoutputstream != null) {
                this.bufferoutputstream.close();
            }

            if (this.fileoutput != null) {
                this.fileoutput.close();
            }

            TimeUnit.SECONDS.sleep(5); //do nothing. file ended. just closing up shop really.
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
                System.out.print(b);  //so that you can see where it got cut off.
            }
            System.out.println("");
            ex.printStackTrace();
        }
        catch(IllegalBlockSizeException ex){
            System.out.println("IllegalBlockSize Exception");
            System.out.println(block.length); //so uh illegal block size. means you sent something over wrongly in some way or another.
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
        return returnvalue;//will be null upon failure.
    }
}
