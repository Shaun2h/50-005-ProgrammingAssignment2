public class PoohClient {
    public static void main(String[] args){
        ClientWithSecurity client = new ClientWithSecurity("Alice_Cert.crt",4321,"localhost","unencryptedprivatekeyALICE.der");
        client.start();
        client.sendplaincert();
        client.receivecert();
        client.clean_Streams();
        client.verify_Certs("BOB");

    }
}

