package index;

import java.util.Arrays;

public class LeafNode implements Node {

	private static final int OCCUPANCY = 100;
	
	int[] index_entries = new int[OCCUPANCY];
	int[] values = new int[OCCUPANCY];
	public InternalNode partentNode = null;
	private int lastItemIndex = 0;

	@Override
	public Node getChildNode(int key) {
		return this;
	}
	
	public int getValue(int key) {
		int i = Arrays.binarySearch(index_entries, key);
		if(key == index_entries[i]) {
			return values[i];
		}else {
			return -1;
		}
	}
	
	public boolean isOverFlowed() {
		return lastItemIndex >= OCCUPANCY;
	}
	
	public void insert(int key, int value) {
		if(this.isOverFlowed()) {
			this.split();
		}else {
			this.lastItemIndex += 1;
			this.values[this.lastItemIndex] = value;
		}
	}
	
	public void split() {
		LeafNode newNode = new LeafNode();
		for(int i=0; i < OCCUPANCY/2; i++) {
			newNode.values[i] = this.values[i+OCCUPANCY/2];
			this.values[i] = 0;
		}
		newNode.lastItemIndex = OCCUPANCY/2 - 1;
		this.lastItemIndex = OCCUPANCY/2 - 1;
		
		int promoting_key = values[OCCUPANCY/2];
		this.partentNode.insert(promoting_key, newNode);
	}

}
