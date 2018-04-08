public class ServerPiglet {
    public static void main(String[] args){
        ServerWithSecurity server = new ServerWithSecurity("Bob_Cert.crt",4321,"unencryptedprivatekeyBOB.der");
        server.start();
        long timeStarted = System.nanoTime();
        server.receivecert();
        server.sendplaincert();
        server.clean_Streams();
        server.verify_Certs("ALICE");
        //server.receieve_file_with_SERVER_PrivateKey(); //FOR SENDING VIA RSA ENCRYPTION
        server.receiveFile(); //receive the file...


        /*
        server.shareSessionKey();
        server.clean_Streams();
        server.receieve_file_with_AES();
        server.clean_Streams();
        */
        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run - Server side Timing");
    }
}






