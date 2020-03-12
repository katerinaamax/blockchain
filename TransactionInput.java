public class TransactionInput {
	public String transactionOutputId; 
	public TransactionOutput UTXO; //unspent transaction outputs 
	
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
