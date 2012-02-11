package filerc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.mcl.MCL;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.distance.CosineSimilarity;
import net.sf.javaml.filter.normalize.NormalizeMidrange;

// Singleton pattern for accessing the entire model
public class FileInteractions {
	private static FileInteractions instance;
	private static final String DEFAULT_DB = "FileTrackerDB";
	private final IWorkbench workbench;
	private String dbName;
	private Pair recentFile;
	private SQLiteWrapper db;
	
	private FileInteractions() {
		dbName = DEFAULT_DB;
		this.workbench = PlatformUI.getWorkbench();
		
		db = new SQLiteWrapper(DEFAULT_DB);
	}
	
	public FileInteractions(IWorkbench workbench, String dbName) {
		this.dbName = dbName;
		//view = new View();
		this.workbench = workbench;
		
		db = new SQLiteWrapper(this.dbName);
		
		recentFile = new Pair("", "");
	}
	
	public void addInteractionCounts(Pair openedFile) {
		ArrayList<Pair> pairs = listOpenFiles();
		ArrayList<Row> rows = new ArrayList<Row>();
		
		// Update database file pair count for all files in the same project
		for(Pair pair : pairs) {
			/*
			 * Only add if they belong to the same project.  Duplicate files
			 * just counts how many times each file is opened (with itself).
			 */
			if(openedFile.getProject().equals(pair.getProject())) {
				Row row = new Row(openedFile.getFile(), pair.getFile(),
					openedFile.getProject());
				rows.add(row);
				Row selfRow = new Row(pair.getFile(), pair.getFile(),
					pair.getProject());
				
				/*
				 * Increment the entry count if it exists, otherwise
				 * initialize it with a count of 1
				 */
				if(db.entryExists(row)) {
					db.incCount(row, SQLiteWrapper.INTERACTION_COUNT);
				}
				else {
					row.setInteractionCount(1);
					row.setScmCount(0);
					row.setStaticCodeCount(0);
					db.addRow(row);
				}
				
				// Increment total file interaction counter for each open file
				if(!pair.equals(openedFile))
					db.incCount(selfRow, SQLiteWrapper.INTERACTION_COUNT);
			}
		}
	}
	
	public void clearSamples() {
		db.clearSamples();
	}

	public void updateRecentFile(Pair f) {
		this.recentFile = f;
	}
	
	// Access this singleton class
	public static synchronized FileInteractions getInstance() {
		if(instance == null)
			instance = new FileInteractions();
		return instance;
	}
	
	// Retrieve all file pair samples and counts from the input project
	public ArrayList<Row> getAllSamples() {
		return db.getAllSamples();
	}
	
	public ArrayList<TreeNode> getSamplesTree() {
		// Exit silently if there is no recent project
		if(recentFile.getFile().equals(""))
			return new ArrayList<TreeNode>();
		
		String project = recentFile.getProject();
		ArrayList<TreeNode> samplesTree = new ArrayList<TreeNode>();
		Set<Pair> projFiles = new HashSet<Pair>();
		HashMap<Row, Integer> simMtx = new HashMap<Row, Integer>();
		
		// Get list of open files related to the last focused tab
		ArrayList<Pair> openFiles = this.listOpenProjectFiles(project);
		
		// Get all related entries for each open file and project
		ArrayList<Row> projQueries = 
			db.getRelatedFiles(new ArrayList<Pair>(openFiles));
		
		// Add all file names to the projFiles list
		for(Row r : projQueries) {
			projFiles.add(new Pair(r.getFile1(), project));
			projFiles.add(new Pair(r.getFile2(), project));
			simMtx.put(r, r.getInteractionCount());
		}
		
		// Convert these sets to arraylists to access get method
		ArrayList<Pair> pF = new ArrayList<Pair>(projFiles);
		
		ArrayList<Double> coefficients = new ArrayList<Double>();
		
		// Get the denominator simMtx[f][f]
		Row index = new Row(recentFile.getFile(), recentFile.getFile(),
			project);
		double totalCounts = (double) simMtx.get(index);
		
		/*
		 *  Calculate the weights for the weighted average with respect to the
		 *  most recent file
		 */
		for(Pair f : openFiles) {
			index = new Row(this.recentFile.getFile(), f.getFile(),
				project);
			
			// sim(recent, open) = counts(recent, open) / counts(recent, recent)
			if(simMtx.containsKey(index))
    			coefficients.add((double) simMtx.get(index) / totalCounts);
			else
				coefficients.add(0.0);
		}
		
		/*
		 *  Calculate the scores for all files in the project
		 *  score(f_i) = sum(coefficients[j] * sim(openFile_j, f_i), j)
		 */
		ArrayList<FileCloseness> scores = new ArrayList<FileCloseness>();
		for(Pair f : pF) {
			// Skip scoring for files which are already open
			if(openFiles.contains(f))
				continue;
			
			double score =  0.0;
			for(int i = 0; i < openFiles.size(); ++i) {
				Row i0 = new Row(openFiles.get(i).getFile(),
					f.getFile(), project);
				Row i1 = new Row(openFiles.get(i).getFile(),
					openFiles.get(i).getFile(), project);
				
				if(simMtx.containsKey(i0))
					score += (double) simMtx.get(i0) / (double) simMtx.get(i1) *
					    coefficients.get(i);
			}
			scores.add(new FileCloseness(f.getFile(), score));
		}
		
		// Display the recommendations in sorted order with highest first
		Collections.sort(scores);
		Collections.reverse(scores);
		for(int i = 0; i < scores.size(); ++i) {
			samplesTree.add(new TreeNode(scores.get(i).getFile(),
				scores.get(i).getCloseness()));
		}
		
		return samplesTree;
	}
	
	public ArrayList<TreeNode> getClusters() {
		ArrayList<TreeNode> ret = new ArrayList<TreeNode>();
		ArrayList<String> projects = db.getProjectNames();
		
		
		// Get all related entries for each open file and project
		for(String project : projects) {
			TreeNode projectTN = new TreeNode(project);
			Dataset data = new DefaultDataset();
			Set<Pair> projFiles = new HashSet<Pair>();
			
			ArrayList<Row> projQueries = 
				db.getProjectSamples(project);
		
			// Add all file names to the projFiles list
			for(Row r : projQueries) {
				projFiles.add(new Pair(r.getFile1(), project));
				projFiles.add(new Pair(r.getFile2(), project));
			}
			
			ArrayList<Pair> pF = new ArrayList<Pair>(projFiles);
			
			// Add pairwise distances to data set for every file
			int pFSize = pF.size();
			for(int i = 0; i < pFSize; ++i) {
				double[] sims = new double[pF.size()];
				for(int j = 0; j < pFSize; ++j) {
					if(i == j) {
						sims[j] = 0;
					} else {
						Row r = new Row(pF.get(i).getFile(),
							pF.get(j).getFile(), project);
						
						int index = projQueries.indexOf(r);
						if(index > 0 && index < projQueries.size()) {
							sims[j] =
								projQueries.get(index).getInteractionCount();
						} else {
							sims[j] = 0;
						}
					}
				}
				data.add(new DenseInstance(sims, pF.get(i).getFile()));
			}
			
			// Normalize the data
			NormalizeMidrange nmr = new NormalizeMidrange(0.5, 1);
			nmr.build(data);
			nmr.filter(data);
			
			// Run the MCL clustering algorithm
			Clusterer mcl =
				new MCL(new CosineSimilarity());
				//new MCL(new NormalizedEuclideanSimilarity(data));
			Dataset[] clusters = mcl.cluster(data);
			
			// New tree node for each cluster
			for(int i = 0; i < clusters.length; ++i) {
				Dataset cluster = clusters[i];
				TreeNode clusterTN = new TreeNode(Integer.toString(i));
				
				ArrayList<TreeNode> children = new ArrayList<TreeNode>();
				for(Object file : cluster.classes())
					children.add(new TreeNode(file.toString()));
				
				clusterTN.addChildren(children);
				projectTN.addChild(clusterTN);
			}
			
			ret.add(projectTN);
		}
		
		return ret;
	}
	
	public ArrayList<Row> getProjectSamples(String project) {
		return db.getProjectSamples(project);
	}
	
	private ArrayList<Pair> listOpenFiles() {
		// files and projects form a tuple that describe the file name and the
		// project that it belongs to respectively.
		ArrayList<Pair> fileProjectPairs = new ArrayList<Pair>();
		
		// Get the list of open files
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page: window.getPages()) {
				for(IEditorReference editorRef : page.getEditorReferences()) {
					IEditorPart editor = editorRef.getEditor(false);
					if(editor != null) {
						
						IFileEditorInput input =
							(IFileEditorInput) editor.getEditorInput();
						// Add the file name
						String f = input.getFile().getFullPath().toString();
						// Add the project name
						IProject project = input.getFile().getProject();
						String p = project.getName();
						
						fileProjectPairs.add(new Pair(f, p));
					}
				}
			}
		}
		
		Collections.sort(fileProjectPairs);
		
		return fileProjectPairs;
	}
	
	private ArrayList<Pair> listOpenProjectFiles(String project) {
		ArrayList<Pair> filePairs = new ArrayList<Pair>();
		ArrayList<Pair> allOpenFiles = this.listOpenFiles();
		
		for(Pair pair : allOpenFiles) {
			if(project.equals(pair.getProject()))
				filePairs.add(pair);
		}
		
		return filePairs;
	}
}
