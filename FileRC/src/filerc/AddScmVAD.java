package filerc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ca.ucalgary.cpsc.lsmr.common.scm.connector.SVNConnector;
import ca.ucalgary.cpsc.lsmr.common.scm.model.BaseRepository;
import ca.ucalgary.cpsc.lsmr.common.scm.model.BaseRevision;
import ca.ucalgary.cpsc.lsmr.common.scm.model.BaseRevisionSet;
import ca.ucalgary.cpsc.lsmr.common.scm.model.SCMFacade;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.JayFXException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

import filerc.model.FileInteractions;
import filerc.model.Pair;
import filerc.model.SQLiteWrapper;
import filerc.view.DBMonitor;

public class AddScmVAD implements IViewActionDelegate {
	private DBMonitor view;
	
	@Override
	public void run(IAction action) {
		JayFX jfx = new JayFX();
		IProgressMonitor lMonitor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()[0].getView( true ).getViewSite().getActionBars().getStatusLineManager().getProgressMonitor();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("Test");
		try {
			jfx.initialize(project, lMonitor, true);
		} catch(JayFXException e) {
			System.out.println(e.getMessage());
		}
		jfx.dumpConverter();
		System.out.println("****************");
		Set<IElement> es = jfx.getAllElements();
		for(IElement e : es) {
			System.out.println(e.getId());
			System.out.println(e.getCategory());
			System.out.println("-------");
			for(IElement e2 : jfx.getRange(e, Relation.CALLS)) {
				System.out.println(e2.getId());
			}
			System.out.println("**");
			for(IElement e2 : jfx.getRange(e, Relation.ACCESSES)) {
				System.out.println(e2.getId());
			}
			System.out.println("** Inverse **");
			for(IElement e2 : jfx.getRange(e, (Relation.CALLS).getInverseRelation())) {
				System.out.println(e2.getId());
			}
			System.out.println("**");
			for(IElement e2 : jfx.getRange(e, (Relation.ACCESSES).getInverseRelation())) {
				System.out.println(e2.getId());
			}
			System.out.println("-------");
			System.out.println("");
		}
		
		System.out.println("here");
		String scmName = "azureus";
		String scmURL = "https://subversion.assembla.com/svn/LaspView";
		String scmUsername = "anonymous";
		String scmPassword = "anonymous";
		
		FileInteractions model = FileInteractions.getInstance();
		
        BaseRepository repository = new BaseRepository(scmName, scmURL,
        	scmUsername, scmPassword, SVNConnector.KIND);
        //repository.setRoots("/trunk/", "/branches/", "/tags/");
        repository.setRoots("/", null, null);
        
        SVNConnector svnc = new SVNConnector(repository);
        
        long startRevision = 0;
        long endRevision = -1;
 
        SCMFacade scmf = svnc.getChanges(startRevision, endRevision, null);
        Vector<BaseRevisionSet> revSets = scmf.getRevisionSets();
        for(BaseRevisionSet revSet : revSets) {
        	List<BaseRevision> revs = revSet.getRevisions();
        	ArrayList<Pair> toAdd = new ArrayList<Pair>();
        	for(BaseRevision rev: revs) {
        		toAdd.add(new Pair(rev.getResource().getPath(), "LaspView"));
        	}
        	
        	model.incPairwiseCounts(toAdd, SQLiteWrapper.SCM_COUNT);
        }
 
		// Clear the view
		view.getViewer().setInput(model.getAllSamples());
		view.getViewer().refresh();
	}
	
	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {}
	
	@Override
	public void init(IViewPart view) {
		this.view = (DBMonitor) view;
	}
}
