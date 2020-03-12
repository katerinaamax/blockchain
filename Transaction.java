import java.security.*;
import java.util.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Transaction {
	public String transactionId;    //hash of the Transaction
	public PublicKey sender;        //sender's public key 
	public PublicKey reciepient;   //reciepient's public key
	public float value;
	public byte[] signature;
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	public static int sequence = 0; //count of how many transaction we have
	
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	public String transactionToData() {
		return sender.toString() + "->" + reciepient.toString() + ": " + value;
	}
	
	private String calculateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		String dataToHash = sender.toString() + reciepient.toString()  + sequence;
		
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
	
	//Applies ECDSA Signature and returns the result ( as bytes ).
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}
	
	//Verifies a String signature 
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	//Signs all the data we dont wish to be tampered with.
	public void generateSignature(PrivateKey privateKey) {
		String data = getStringFromKey(sender) + getStringFromKey(reciepient) + Float.toString(value)	;
		signature = applyECDSASig(privateKey,data);		
	}
	
	//Verifies the data we signed hasnt been tampered with
	public boolean verifiySignature() {
		String data = getStringFromKey(sender) + getStringFromKey(reciepient) + Float.toString(value)	;
		
		return verifyECDSASig(sender, data, signature);
	}
	
	//Returns true if new transaction could be created.	
	public boolean processTransaction() {
		/*
		if(verifiySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}*/
				
		//gather transaction inputs (Make sure they are unspent):
		for(TransactionInput i : inputs) {
			i.UTXO = Blockchain.UTXOs.get(i.transactionOutputId);
		}

		//generate transaction outputs:
		float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
		transactionId = calculateHash();
		outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender		
				
		//add outputs to Unspent list
		for(TransactionOutput o : outputs) {
			Blockchain.UTXOs.put(o.id , o);
		}
		
		//remove transaction inputs from UTXO lists as spent:
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			Blockchain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	//returns sum of inputs(UTXOs) values
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			total += i.UTXO.value;
		}
		return total;
	}

	//returns sum of outputs:
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
}
