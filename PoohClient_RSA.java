import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PoohClient_RSA {
    public static void main(String[] args){
        String certloc="";
        String portnum="";
        String who;
        String privatekey="";
        String targetip="10.12.141.174";
        //"localhost"
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
            if(hold.equals("y") || hold.equals("Y")){
                certloc = "Alice_Cert.crt";
                portnum = "4321";
                privatekey = "unencryptedprivatekeyALICE.der";
                who_is_that = "BOB";
                do_or_dont=false;
                System.out.println("Default settings selected.");
            }

            if (do_or_dont) {

                System.out.println("Please enter your cert file name (Case sensitive, space sensitive, just sensitive in general) type d for default.");
                certloc = inputreader.readLine();
                System.out.println("Please enter your port number. type d for default.");
                portnum = inputreader.readLine();
                System.out.println("enter private key. type d for default. Keep in mind this system only accepts ALICE and BOB's Cert. this is more of where the private key is located.");
                privatekey = inputreader.readLine();
                System.out.println("enter who the opposing party is");
                who = inputreader.readLine();
                System.out.println("enter target IP address");
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
            System.out.println("initialising client....(RSA only)");

            ClientWithSecurity client = new ClientWithSecurity(certloc, Integer.parseInt(portnum), targetip, privatekey);
            System.out.println("Attempting to find server.");
            client.start();
            long timeStarted = System.nanoTime();
            client.sendplaincert();
            client.receivecert();
            client.verify_Certs(who_is_that,disablecheck);
            client.sendWith_ServerPublicKeyEncrypted(filename); //FOR SENDING VIA RSA ENCRYPTION


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







