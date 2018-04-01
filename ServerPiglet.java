public class ServerPiglet {
    public static void main(String[] args){
        ServerWithSecurity server = new ServerWithSecurity("Bob_Cert.crt",4321);
        server.start();
        server.receivecert_andVerify("ALICE");
        //server.sendplaincert();

    }
}
