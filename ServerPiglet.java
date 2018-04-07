public class ServerPiglet {
    public static void main(String[] args){
        ServerWithSecurity server = new ServerWithSecurity("Bob_Cert.crt",4321,"unencryptedprivatekeyBOB.der");
        server.start();
        server.receivecert();
        server.sendplaincert();
        server.clean_Streams();
        server.verify_Certs("ALICE");
        server.receieve_file_with_SERVER_PrivateKey(); //FOR SENDING VIA RSA ENCRYPTION
        //server.shareSessionKey();
        //server.clean_Streams();
        //server.receieve_file_with_AES();
        //server.clean_Streams();
    }
}
