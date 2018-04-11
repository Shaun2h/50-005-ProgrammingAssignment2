import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerPiglet_AES {
    public static void main(String[] args){
        BufferedReader inputreader = new BufferedReader(new InputStreamReader(System.in));
        String certloc="";
        String portnum="";
        String who;
        String privatekey="";
        String who_is_that="";
        boolean do_or_dont=true;
        boolean disablecheck=false;
        try{
            System.out.println("are you running this server for someone other then alice and bob?");
            String hold = inputreader.readLine();
            if(hold.equals("y") || hold.equals("Y")){
                disablecheck=true;
            }
            System.out.println("running using default settings? type \"y\" if this is so. case sensitive");
            hold = inputreader.readLine();
            if(hold.equals("y") || hold.equals("Y")){
                certloc = "Bob_Cert.crt";
                portnum = "4321";
                privatekey = "unencryptedprivatekeyBOB.der";
                who_is_that = "ALICE";
                do_or_dont=false;
                System.out.println("Default settings have been applied for AES server.");
            }
            if(do_or_dont) {
                System.out.println("Please enter your cert file name (Case sensitive, space sensitive, just sensitive in general) type d for default.");
                certloc = inputreader.readLine();
                System.out.println("Please enter your port number. type d for default.");
                portnum = inputreader.readLine();
                System.out.println("enter private key. type d for default. ");
                privatekey = inputreader.readLine();
                System.out.println("enter who the opposing party is. type d for default.");
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
            System.out.println("initialising server....");
            ServerWithSecurity server = new ServerWithSecurity(certloc,Integer.parseInt(portnum),privatekey);
            System.out.println("now awaiting connection (AES)");
            server.start(); //await connection.

            //connection established...

            long timeStarted = System.nanoTime(); //start the timer.

            server.receivecert();
            server.sendplaincert();

            server.verify_Certs(who_is_that,disablecheck);
            server.shareSessionKey();
            server.receieve_file_with_AES();



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






