package index;

public class BTreeMem {

	private InternalNode rootNode = new InternalNode();
	
	public void insert(int key, int value) {
		LeafNode leafNode = (LeafNode) rootNode.getLeafNode(key);
		leafNode.insert(key, value);
		// rootNodeのポインタの更新をどうやるか
	}
	
	public int select(int key) { 
		LeafNode leafNode = (LeafNode) rootNode.getLeafNode(key);
		return leafNode.getValue(key);
	}
	
	public int search(int key) {
		Node leafNode = rootNode.getLeafNode(key);
		return leafNode.getValue(key);
	}
	
	

}
