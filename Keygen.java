import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static jdk.nashorn.internal.runtime.Context.printStackTrace;

public class Keygen {
    public SecretKey createAES(){
        try{
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            SecureRandom random  = new SecureRandom();
            kg.init(random);
            SecretKey key = kg.generateKey(); //generate the AES key.
            return key;
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println("No Such Algorithm in KeyGen");
            printStackTrace(ex);
        }
        return null;
    }
}
