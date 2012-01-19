package filetrackerplugin.model;

import java.util.ArrayList;

public class TreeNode {
	private String text;
	private TreeNode parent;
	
	private ArrayList<TreeNode> children;
	
	public TreeNode(String text) {
		this.text = text;
		this.parent = null;
		
		this.children = new ArrayList<TreeNode>();
	}
	
	public TreeNode(String text, ArrayList<TreeNode> children) {
		this.text = text;
		
		if(children != null)
			this.children = children;
		else
			this.children = new ArrayList<TreeNode>();
		
		this.parent = null;
		
		for(int i = 0; i < children.size(); ++i) {
			children.get(i).parent = this;
		}
	}
	
	public void addChild(TreeNode tn) {
		children.add(tn);
	}
	
	public void addChildren(ArrayList<TreeNode> tn) {
		children.addAll(tn);
	}
	
	public ArrayList<TreeNode> getChildren() {
		return children;
	}
	
	public TreeNode getParent() {
		return parent;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
}
