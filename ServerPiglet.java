import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerPiglet {
    public static void main(String[] args){
        BufferedReader inputreader = new BufferedReader(new InputStreamReader(System.in));
        String certloc="";
        String portnum="";
        String who;
        String privatekey="";
        String who_is_that="";
        boolean do_or_dont=true;
        try{
            System.out.println("running using default settings? type \"y\" if this is so. case sensitive");
            if(inputreader.readLine().equals("y")){
                certloc = "Bob_Cert.crt";
                portnum = "4321";
                privatekey = "unencryptedprivatekeyBOB.der";
                who_is_that = "ALICE";
                do_or_dont=false;
            }
            if(do_or_dont) {
                System.out.println("Please enter your cert file name (Case sensitive, space sensitive, just sensitive in general) type d for default.");
                certloc = inputreader.readLine();
                System.out.println("Please enter your port number. type d for default.");
                portnum = inputreader.readLine();
                System.out.println("enter private key. type d for default.");
                privatekey = inputreader.readLine();
                System.out.println("enter who the opposing party is. type d for default. Actually there isn't a point in typing anything else. it's ALICE by default. anything you else is taken as BOB");
                who = inputreader.readLine();
                if (certloc.equals("d")) {
                    certloc = "Bob_Cert.crt";
                }
                if (portnum.equals("d")) {
                    portnum = "4321";
                }
                if (privatekey.equals("d")) {
                    privatekey = "unencryptedprivatekeyBOB.der";
                }
                if (who.equals("d")) {
                    who_is_that = "ALICE";
                } else {
                    who_is_that = "BOB";
                }
            }
            System.out.println("Please enter what type of file sending you are looking for:");
            System.out.println("1 = send the file plain, after ensuring certificates are correct");
            System.out.println("2 = send the file encrypted with RSA encryption, after ensuring certificates are correct");
            System.out.println("3 = send the file with AES encryption, after ensuring certificates are correct, and exchanging keys.");
            System.out.println("if you enter anything else, the server won't do anything and will exit immediately after verifying certificates.");
            String type = inputreader.readLine();
            System.out.println("initialising server....");
            ServerWithSecurity server = new ServerWithSecurity(certloc,Integer.parseInt(portnum),privatekey);
            System.out.println("now awaiting connection");
            server.start(); //await connection.

            //connection established...

            long timeStarted = System.nanoTime(); //start the timer.

            server.receivecert();
            server.sendplaincert();

            server.verify_Certs(who_is_that);
            if(type.equals("1")){
                server.receiveFile();
            } //receive the file...
            if(type.equals("2")){
                server.receieve_file_with_SERVER_PrivateKey();
            }//FOR SENDING VIA RSA ENCRYPTION
            if(type.equals("3")){
                server.shareSessionKey();
                server.receieve_file_with_AES();
            }


            long timeTaken = System.nanoTime() - timeStarted;
            System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run - Server side Timing");
        }
        catch(IOException ex){
            System.out.println("entire thing failed.");
            ex.printStackTrace();
        }
        catch(NumberFormatException ex){
            System.out.println("You didn't enter a proper port number...");
            ex.printStackTrace();
        }


    }
}






