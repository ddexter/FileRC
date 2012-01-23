package filerc;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;

import filerc.model.FileInteractions;
import filerc.model.Pair;
import filerc.view.DBMonitor;

@SuppressWarnings("restriction")
public class FileListener {
	private final IWorkbench workbench;
	private DBMonitor dbm;
	
	// Handle parts opening and closing
	private final IPartListener2 partListener = new IPartListener2() {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {}
		
		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if(partRef.getId().equals("filerc.view.DBM")) {
				dbm = null;
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			// Opened part is a DB monitor instance
			if(partRef.getId().equals("filerc.view.DBM")) {
				FileInteractions model = FileInteractions.getInstance();
				dbm = (DBMonitor) partRef.getPart(true);
				
				if(dbm != null) {
					dbm.getViewer().setInput(model.getAllSamples());
					dbm.getViewer().refresh();
				}
			}
			
			/*
			 * Opened part is a file, update pairwise file counts in DB and
			 * refresh both the DB monitor and file recommender windows if open
			 */
			if(partRef instanceof EditorReference) {
				FileInteractions model = FileInteractions.getInstance();
				
				IEditorPart editor =
					((EditorReference) partRef).getEditor(false);
				if(editor != null) {
					IFileEditorInput input =
						(IFileEditorInput) editor.getEditorInput();
					Pair newFile = new Pair(
						input.getFile().getFullPath().toString(),
						input.getFile().getProject().getName());
					
					model.addCounts(newFile);
					
					// Store the file as the most recently visited
					model.updateRecentFile(newFile);
					
					// Update the views
					if(dbm != null) {
						dbm.getViewer().setInput(model.getAllSamples());
						dbm.getViewer().refresh();
					}
				}
			}
		}

		@Override
		public void partHidden(IWorkbenchPartReference arg0) {}

		@Override
		public void partInputChanged(IWorkbenchPartReference arg0) {}

		@Override
		public void partVisible(IWorkbenchPartReference arg0) {}

		@Override
		public void partDeactivated(IWorkbenchPartReference arg0) {}
	};

	private final IWindowListener windowListener = new IWindowListener() {
		@Override
		public void windowOpened(IWorkbenchWindow window) {
			window.getPartService().addPartListener(partListener);
		}

		@Override
		public void windowClosed(IWorkbenchWindow window) {
			window.getPartService().removePartListener(partListener);
		}

		@Override
		public void windowActivated(IWorkbenchWindow arg0) {}

		@Override
		public void windowDeactivated(IWorkbenchWindow arg0) {}
	};
	
	public FileListener() {
		workbench = PlatformUI.getWorkbench();
		
		dbm = null;
		
		// Initialize the model with the current workbench and db = ftPlugin
	}
	
	protected void enableListener() {
		// Window listener to add a new part listener to every new window
		workbench.addWindowListener(windowListener);
	
		// Add the listener to every part
		for(IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
			w.getPartService().addPartListener(partListener);
		}
	}
	
	protected void disableListener() {
		workbench.removeWindowListener(windowListener);
		
		for(IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
			w.getPartService().removePartListener(partListener);
		}
	}
}
