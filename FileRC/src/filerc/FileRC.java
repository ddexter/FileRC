package filerc;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FileRC extends AbstractUIPlugin implements
	IWorkbenchListener {

	// The plug-in ID
	public static final String PLUGIN_ID = "FileTrackerPlugin"; //$NON-NLS-1$

	// The shared instance
	private static FileRC plugin;
	
	private FileListener fileListener;
	
	/**
	 * The constructor
	 */
	public FileRC() {
	}

	@Override
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		return true;
	}

	@Override
	public void postShutdown(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		getWorkbench().addWorkbenchListener(this);
		
		fileListener = new FileListener();
		fileListener.enableListener();
	}
	
	public void stop(BundleContext context) throws Exception {
		getWorkbench().removeWorkbenchListener(this);
		
		fileListener.disableListener();
		
		plugin = null;
		super.stop(context);
	}
	
	public FileRC getPlugin() {
		return plugin;
	}
}
