package filetrackerplugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import filetrackerplugin.model.FileInteractions;
import filetrackerplugin.view.FileRecommender;

public class FRAnalyzerVAD implements IViewActionDelegate {
	private FileRecommender view;

	@Override
	public void run(IAction action) {
		FileInteractions model = FileInteractions.getInstance();
		
		view.getViewer().setInput(model.getSamplesTree());
		view.getViewer().refresh();
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IViewPart view) {
		this.view = (FileRecommender) view;
		
	}

}
