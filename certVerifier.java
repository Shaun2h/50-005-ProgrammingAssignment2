import java.lang.*;
import java.security.cert.*;
import java.security.*;
import java.util.*;
import java.io.*;

/*
METHOD TO VERIFY A CERT IS FROM THE RIGHT PERSON.
does not implement hash checks...
GIVE Your LOCATION and WHO you expect on the other end, ALL in caps.
*/
class certVerifier{
  private String CA_cert_loc = "CA.crt";
  private String given_cert_loc;
  public boolean verify(String location_of_cert, String who){
    given_cert_loc = location_of_cert;
    try{
      InputStream f = new FileInputStream(this.CA_cert_loc); //Certififying authority's file
      InputStream a = new FileInputStream(this.given_cert_loc); //Cert to be compared with's File.
      CertificateFactory cf = CertificateFactory.getInstance("X.509");//for generating certificate item
      X509Certificate CSE_cert = (X509Certificate) cf.generateCertificate(f); //CSE certificate authority certificate
      X509Certificate unknown_cert = (X509Certificate) cf.generateCertificate(a); //Unknown's Cert.
      CSE_cert.checkValidity(); //check validity of CSE cert. Will throw an exception.
      PublicKey CSE_key = CSE_cert.getPublicKey(); //extract CSE public key
      unknown_cert.verify(CSE_key);//verify unknown cert was signed with CSE public key.
      String[] unknown_info = unknown_cert.getSubjectDN().getName().split(", ");
      //There are only 2 people in this ecosystem. So if it does not match with ALICE or bob, it is not valid.
      /*
      for(String s: unknown_info ){
        System.out.println(s); //if you want to see the internals of the cert, uncomment this.
      }
      */
      ArrayList<String> source = null;
      gen_info info_source = new gen_info();
      if(who.equals("ALICE")){
        source = info_source.genalice();
      }
      if(who.equals("BOB")){
        source = info_source.genbob();
      }
      try{
        for(int i=0; i<unknown_info.length; i++){
          if(!unknown_info[i].equals(source.get(i))){
            System.out.println("CERT INFO VERIFICATION FAILURE");
            //System.out.println(unknown_info[i]); //debug info. where was the info different?
            //System.out.println(source.get(i)); //debug info. where was the info different?
            return false;
          }
        }
     }
     catch(Exception ex){
       System.out.println("FAILED!");
       return false;
     }


      //else, it is valid. return true.
      return true; //if it has passed all checks.
    }
    catch(Exception ex){
      System.out.println(ex);
      System.out.println("Exception in verifying certificate.");
      return false;
    }
  }
}


class gen_info{
  public static ArrayList<String> genalice(){
    ArrayList<String> a= new ArrayList<>();
    a.add("EMAILADDRESS=Alice@alice.alice");
    a.add("CN=Alice");
    a.add("OU=Alice");
    a.add("O=Alice");
    a.add("L=singapore");
    a.add("ST=singapore");
    a.add("C=sg");
    return a;
  }
  public static ArrayList<String> genbob(){
    ArrayList<String> b= new ArrayList<>();
    b.add("EMAILADDRESS=Bob@bob.bob");
    b.add("CN=Bob");
    b.add("OU=Bob");
    b.add("O=Bob");
    b.add("L=singapore");
    b.add("ST=singapore");
    b.add("C=sg");
    return b;
  }
}
