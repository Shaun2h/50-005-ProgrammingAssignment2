public class PoohClient {
    public static void main(String[] args){
        ClientWithSecurity client = new ClientWithSecurity("Alice_Cert.crt",4321,"localhost","unencryptedprivatekeyALICE.der");
        client.start();
        client.sendplaincert();
        client.receivecert();
        client.clean_Streams();
        client.verify_Certs("BOB");
        //client.sendWith_ServerPublicKeyEncrypted("rr.txt"); //FOR SENDING VIA RSA ENCRYPTION
        client.receiveSessionKey();
        client.clean_Streams();
        client.send_file_with_AES();
        client.clean_Streams();
    }
}



//long timeStarted = System.nanoTime();
//long timeTaken = System.nanoTime() - timeStarted;
//System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");

