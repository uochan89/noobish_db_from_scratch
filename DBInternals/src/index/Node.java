package index;

public interface Node {

	Node getChildNode(int key);

	int getValue(int key);
	
	public boolean isOverFlowed();

	void split();

}
