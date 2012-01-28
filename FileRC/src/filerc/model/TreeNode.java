package filerc.model;

import java.util.ArrayList;

public class TreeNode {
	private String text;
	private TreeNode parent;
	private double score;
	
	private ArrayList<TreeNode> children;
	
	public TreeNode(String text) {
		this.text = text;
		this.parent = null;
		
		this.children = new ArrayList<TreeNode>();
	}
	
	public TreeNode(String text, double score) {
		this.text = text;
		this.parent = null;
		this.score = score;
		
		this.children = new ArrayList<TreeNode>();
	}
	
	public TreeNode(String text, double score, ArrayList<TreeNode> children) {
		this.text = text;
		this.score = score;
		
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
	
	public double getScore() {
		return score;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
}
