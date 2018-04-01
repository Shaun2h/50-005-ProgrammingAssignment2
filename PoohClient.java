public class PoohClient {
    public static void main(String[] args){
        ClientWithSecurity client = new ClientWithSecurity("Alice_Cert.crt",4321,"localhost");
        client.start();
        client.sendplaincert();
        client.receivecert_andVerify("BOB");

    }
}

