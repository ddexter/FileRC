package filerc;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import filerc.model.FileInteractions;
import filerc.view.ClusterFinder;

public class FCAnalyzerVAD implements IViewActionDelegate {
	private ClusterFinder view;

	@Override
	public void run(IAction action) {
		FileInteractions model = FileInteractions.getInstance();
		
		view.getViewer().setInput(model.getClusters());
		view.getViewer().refresh();
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}

	@Override
	public void init(IViewPart view) {
		this.view = (ClusterFinder) view;
	}
}
