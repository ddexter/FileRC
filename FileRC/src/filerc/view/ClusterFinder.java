package filetrackerplugin.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import filetrackerplugin.model.FTTreeContentProvider;
import filetrackerplugin.model.FTTreeLabelProvider;
import filetrackerplugin.model.TreeNode;

public class ClusterFinder extends ViewPart {
	private TreeViewer viewer;
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		viewer.setContentProvider(new FTTreeContentProvider());
		viewer.setLabelProvider(new FTTreeLabelProvider());
	
		// Double-click listener opens file when a file is clicked
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s =
					(IStructuredSelection) event.getSelection();
				
				TreeNode tn = (TreeNode) s.getFirstElement();
				/*
				 *  Assure that the tree node is a file by checking that it
				 *  does not have any children
				 */
				if(tn != null && !tn.hasChildren()) {
					IPath path = new Path(tn.getText());
					IFile file = ResourcesPlugin.getWorkspace().getRoot().
						getFile(path);
					IWorkbenchPage page = PlatformUI.getWorkbench().
						getActiveWorkbenchWindow().getActivePage();
					try {
						page.openEditor(new FileEditorInput(file), 
							"org.eclipse.ui.DefaultTextEditor");
					} catch (PartInitException e) {
						System.err.println(
							"Error in FileRecommender.createPartControl():");
						System.err.println(e.getMessage());
					}
				}
			}
		});
		
		getSite().setSelectionProvider(viewer);
	}

	public TreeViewer getViewer() {
		return viewer;
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
