import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PoohClient {
    public static void main(String[] args){
        String certloc="";
        String portnum="";
        String who;
        String privatekey="";
        String targetip="10.12.141.174";
        // "localhost";
        //"10.12.141.174"
        String who_is_that="";
        boolean do_or_dont=true;
        boolean disablecheck=false;
        try {

            BufferedReader inputreader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("are you running this server for someone other then alice and bob?");
            String hold;
            hold = inputreader.readLine();
            if(hold.equals("y") || hold.equals("Y")){
                disablecheck=true;
            }
            System.out.println("running using default settings? type \"y\" if this is so. case sensitive");
            hold = inputreader.readLine();
            if(hold.equals("y") || hold.equals("Y")){
                certloc = "Alice_Cert.crt";
                portnum = "4321";
                privatekey = "unencryptedprivatekeyALICE.der";
                who_is_that = "BOB";
                do_or_dont=false;
            }

            if (do_or_dont) {

                System.out.println("Please enter your cert file name (Case sensitive, space sensitive, just sensitive in general) type d for default.");
                certloc = inputreader.readLine();
                System.out.println("Please enter your port number. type d for default.");
                portnum = inputreader.readLine();
                System.out.println("enter private key. type d for default. Keep in mind this system only accepts ALICE and BOB's Cert. this is more of where the private key is located.");
                privatekey = inputreader.readLine();
                System.out.println("enter who the opposing party is. type d for default. Actually there isn't a point in typing anything else. it's ALICE by default. anything you else is taken as BOB");
                who = inputreader.readLine();
                System.out.println("enter who the opposing party is. type d for default. Actually there isn't a point in typing anything else. it's ALICE by default. anything you else is taken as BOB");
                targetip = inputreader.readLine();
                if (certloc.equals("d")) {
                    certloc = "Alice_Cert.crt";
                }
                if (portnum.equals("d")) {
                    portnum = "4321";
                }
                if (privatekey.equals("d")) {
                    privatekey = "unencryptedprivatekeyALICE.der";
                }

                if (who.equals("d")) {
                    who_is_that = "BOB";
                } else {
                    who_is_that = "ALICE";
                }
            }

            System.out.println("Please enter the file you want sent over. Ensure it is in the same directory");
            String filename = inputreader.readLine();

            System.out.println("Please enter what type of file sending you are looking for:");
            System.out.println("1 = send the file plain, after ensuring certificates are correct");
            System.out.println("2 = send the file encrypted with RSA encryption, after ensuring certificates are correct");
            System.out.println("3 = send the file with AES encryption, after ensuring certificates are correct, and exchanging keys.");
            System.out.println("if you enter anything else, the server won't do anything and will exit immediately after verifying certificates.");
            String type = inputreader.readLine();
            System.out.println("initialising client....");

            ClientWithSecurity client = new ClientWithSecurity(certloc, Integer.parseInt(portnum), targetip, privatekey);
            System.out.println("Attempting to do find server.");
            client.start();
            long timeStarted = System.nanoTime();
            client.sendplaincert();
            client.receivecert();
            client.verify_Certs(who_is_that,disablecheck);

            if(type.equals("1")){
                client.sendFile(filename);
            }
            if(type.equals("2")){
                client.sendWith_ServerPublicKeyEncrypted(filename); //FOR SENDING VIA RSA ENCRYPTION
            }
            if(type.equals("3")){
                client.receiveSessionKey();
                client.send_file_with_AES(filename);
            }


            long timeTaken = System.nanoTime() - timeStarted;
            System.out.println("Program took: " + timeTaken / 1000000.0 + "ms to run - Client side Timing");
        }
        catch(IOException ex){
            System.out.println("entire system didn't work.");
        }
        catch(NumberFormatException ex){
            System.out.println("You didn't enter a proper port number...");
            ex.printStackTrace();
        }
    }
}







