package filerc.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

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
			Path path = new Path(e.getText());
		    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			IContentType contentType = IDE.getContentType(file);
			ImageDescriptor imageDescriptor =
			    PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file.getName(), contentType);
			
			return (Image) imageDescriptor.createImage();
		}
	}
	
	public String getText(Object element) {
		return ((TreeNode) element).getText();
	}

}
