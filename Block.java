import java.util.*;
import java.lang.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Block {
	public String hash;
	public String prevHash;
	public String data;
	public long timeStamp;
	public int nonce;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	
	//constructor
	public Block( String data) {
		this.data = data;
		this.prevHash = null;
		this.timeStamp = new Date().getTime();
		this.nonce = 0; 
		this.hash = calculateBlockHash();
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getPrevHash() {
		return prevHash;
	}
	
	public void setPrevHash(String prevHash) {
		this.prevHash = prevHash;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public int getNonce() {
		return nonce;
	}
	
	public void setNonce(int nonce) {
		this.nonce = nonce;
	}
	
	//maps input data to output data of fixed size
	public String calculateBlockHash() {
		String dataToHash = prevHash + timeStamp  + data + nonce;
		
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
	    
	    this.hash = buffer.toString();
	 
	    return getHash();
	}
	
	public String mineBlock(int difficulty) { //difficulty is the number of 0's they must solve for
		String target = new String(new char[difficulty]).replace('\0', '0');
		
		while(!hash.substring(0, difficulty).equals(target)) {
			nonce++;
			hash = calculateBlockHash();
		}
		
		System.out.println("Block mined. New hash: " + hash);
		return hash;
	}
	
	public boolean isValid(int difficulty) {
		if (hash.startsWith(new String(new char[difficulty])))
			return true;
		else
			return false;
	}
	
	//Add transactions to this block
	public boolean addTransaction(Transaction transaction) {
		
		//process transaction and check if valid, unless block is genesis block then ignore.
		if(transaction == null) 
			return false;		
		
		if((prevHash != null)) {
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}
		
		transactions.add(transaction);
		
		data = transaction.transactionToData();
		
		System.out.println("Transaction Successfully added to Block");
		
		return true;
	}
}
