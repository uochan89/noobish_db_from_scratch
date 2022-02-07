package index;


public class InternalNode implements Node {

	private static final int OCCUPANCY = 100;
	
	int[] index_entries = new int[OCCUPANCY];
	Node[] pointers = new Node[OCCUPANCY+1];
	int lastItemIndex = 0;
	InternalNode parentNode = null;
	
	public boolean isOverFlowed() {
		return lastItemIndex >= OCCUPANCY+1;
	}
	
	public void insert(int key, Node pointer) {
		if(this.isOverFlowed()) {
			this.split();
		}
		lastItemIndex += 1;
		index_entries[lastItemIndex] = key;
		pointers[lastItemIndex+1] = pointer;
	}
	
	public void split() {
		InternalNode newNode = new InternalNode();
		for(int i=0; i < OCCUPANCY/2; i++) {
			newNode.pointers[i] = this.pointers[i+OCCUPANCY/2];
			this.pointers[i] = null;
		}
		newNode.lastItemIndex = OCCUPANCY/2 - 1;
		this.lastItemIndex = OCCUPANCY/2 - 1;
		
		int promoting_key = index_entries[OCCUPANCY/2];
		if(this.parentNode == null) {
			// if is root node what should i do
		}else {
			this.parentNode.insert(promoting_key, (Node) newNode);
		}
		
	}
	
	// TODO
	// 普通のsplitとは構造が異なるので、別メソッドを作るしかない
	// そうなると、rootNodeクラスをつくるべき。
	public InternalNode splittingwhenroot() {
		InternalNode newNode = new InternalNode();
		for(int i=0; i < OCCUPANCY/2; i++) {
			newNode.pointers[i] = this.pointers[i+OCCUPANCY/2];
			this.pointers[i] = null;
		}
		newNode.lastItemIndex = OCCUPANCY/2 - 1;
		this.lastItemIndex = OCCUPANCY/2 - 1;
		
		int promoting_key = index_entries[OCCUPANCY/2];
		InternalNode newRootNode = new InternalNode();
		newRootNode.insert(promoting_key, this);
		//1つずらしでポインタを結ぶ
		newRootNode.insert(promoting_key, newNode);
		return newRootNode;
	}
	
	
	public Node getChildNode(int key) {
		int target = 0;
		for(int i = 0; i < lastItemIndex+1; i++) {
			int v =this.index_entries[i] ;
			if(v <= key && target < v) {
				target = v;
			}
		}
		return pointers[target];
	}
	
	public Node getLeafNode(int key) {
		Node childNode = getChildNode(key);
		if (childNode == null) {
			return this;
		}else{
			return childNode.getChildNode(key);
		}
	}

	@Override
	public int getValue(int key) {
		// TODO Auto-generated method stub
		return 0;
	}

}
