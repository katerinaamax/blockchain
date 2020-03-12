import java.util.*;
import java.security.*;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Blockchain {
	private static List<Block> chain = new ArrayList<Block>();
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	
	public static int difficulty = 4;
	
	public static Wallet walletA;
	public static Wallet walletB;
	
	public static Transaction genesisTran;

	
	public Blockchain() {
		chain = new ArrayList<Block>();
	}
	
	public static void addBlock(Block block) {
		Block newBlock = block;
		if(chain.size() > 0) 
			newBlock.setPrevHash(chain.get(chain.size() - 1).getHash());
		else
			newBlock.setPrevHash(null);
		
		newBlock.calculateBlockHash();
		
		if(!block.isValid(difficulty)) 
			block.mineBlock(difficulty);
		
		chain.add(newBlock);
		System.out.println("Added to blockchain");
	}
	
	public static void displayChain() {
		for (int i=0; i < chain.size(); i++) {
			System.out.println("Block: " + i);
			System.out.println("Nonce: " + chain.get(i).getNonce());
			System.out.println("Timestamp: " + chain.get(i).getTimeStamp());
			System.out.println("Data: " + chain.get(i).getData());
			System.out.println("PreviousHash: " + chain.get(i).getPrevHash());
			System.out.println("Hash: " + chain.get(i).getHash());
			System.out.println();
		}
	}
	
	public Block getLastBlock() {
		return chain.get(chain.size() - 1);
	}
	
	public boolean isValid() {
		for (int i = chain.size() - 1; i > 0; i--) {
			
			if (!(chain.get(i).getHash().equals(chain.get(i).calculateBlockHash()))) {
				System.out.println("Blockchain is not valid2." + i);
				return false;
			}
			
			if(!chain.get(i).getPrevHash().equals(chain.get(i-1).calculateBlockHash())) {
				System.out.println("Blockchain is not valid3." + i);
				return false;
			}
		}
		
		System.out.println("Blockchain is valid.");
		return true;
	}
	
	public static void main(String[] args) {
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		
		//Create wallets:
		walletA = new Wallet();
		walletB = new Wallet();		
		Wallet coinbase = new Wallet();
		
		//create genesis transaction, which sends 100 MaxCoin to walletA: 
		genesisTran = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTran.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
		genesisTran.transactionId = "0"; //manually set the transaction id
		genesisTran.outputs.add(new TransactionOutput(genesisTran.reciepient, genesisTran.value, genesisTran.transactionId)); //manually add the Transactions Output
		UTXOs.put(genesisTran.outputs.get(0).id, genesisTran.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTran);
		addBlock(genesis);
		
		Block newTran = new Block("");
		newTran.addTransaction(walletA.sendFunds(walletB.publicKey, 20f));
		addBlock(newTran);
		
		Block block1 = new Block("");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		
		Block block2 = new Block("");
		block2.addTransaction(walletB.sendFunds(walletA.publicKey, 15f));
		addBlock(block2);
		
		displayChain();
		
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
	}
}
