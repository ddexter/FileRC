package filerc.model;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class FTTreeLabelProvider extends LabelProvider {
	public Image getImage(Object element) {
		TreeNode e = (TreeNode) element;
		
		// If the TreeNode has children, then give it a project icon
		if(e.hasChildren())
		{
			return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FOLDER);
		// Otherwise, it's a file, so give it a file icon
		} else {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FILE);
		}
	}
	
	public String getText(Object element) {
		return ((TreeNode) element).getText();
	}

}
