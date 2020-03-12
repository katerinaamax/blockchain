import java.security.*;
import java.util.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;


public class TransactionOutput {
	public String id;
	public PublicKey reciepient; //also known as the new owner of these coins.
	public float value; //the amount of coins they own
	public String parentTransactionId; //the id of the transaction this output was created in
	
	//Constructor
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = calculateHash();
	}
	
	private String calculateHash() {
		String dataToHash = getStringFromKey(reciepient) + Float.toString(value) + parentTransactionId;
		
		MessageDigest digest = null;
		byte[] bytes = null;
		
		try{
			digest = MessageDigest.getInstance("SHA-256");
			bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
		} catch(NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		
		StringBuffer buffer = new StringBuffer();
	    for (byte b : bytes) {
	        buffer.append(String.format("%02x", b));
	    }
	    
		return buffer.toString();
	}
	
	//Check if coin belongs to you
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
}
