package filerc.model;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class FTTreeContentProvider extends ArrayContentProvider
	implements ITreeContentProvider {
	
	@Override
	public Object[] getChildren(Object element) {
		ArrayList<TreeNode> children = ((TreeNode) element).getChildren();
		return children.toArray(new TreeNode[children.size()]);
	}

	@Override
	public Object getParent(Object element) {
		return ((TreeNode) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((TreeNode) element).hasChildren();
	}
}
