package filetrackerplugin.model;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
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
	}
	
	public void addCounts(Pair openedFile) {
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
				
				/*
				 * Increment the entry count if it exists, otherwise
				 * initialize it with a count of 1
				 */
				if(db.entryExists(row)) {
					db.incCount(row);
				}
				else {
					row.setCount(1);
					db.addRow(row);
				}
			}
		}
	}
	
	public void clearSamples() {
		db.clearSamples();
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
		ArrayList<TreeNode> samplesTree = new ArrayList<TreeNode>();
		ArrayList<Pair> openFiles = listOpenFiles();
		Set<String> projects = new HashSet<String>();
		
		// First get the list of open projects
		for(Pair pair : openFiles) {
			projects.add(pair.getProject());
		}
		
		// Get all related entries for each open file and project
		for(String project : projects) {
			Set<Pair> openProjFiles = new HashSet<Pair>();
			Set<Pair> projFiles = new HashSet<Pair>();
			HashMap<Row, Integer> simMtx = new HashMap<Row, Integer>();
			
			// Sort files by project
			for(Pair pair : openFiles) {
				if(pair.getProject().equals(project)) {
					openProjFiles.add(pair);
					projFiles.add(pair);
				}
			}
		
			ArrayList<Row> projQueries = 
				db.getRelatedFiles(new ArrayList<Pair>(openProjFiles));
		
			// Add all file names to the projFiles list
			for(Row r : projQueries) {
				projFiles.add(new Pair(r.getFile1(), project));
				projFiles.add(new Pair(r.getFile2(), project));
				simMtx.put(r, r.getCount());
			}
			// Convert these sets as arraylists to access get method
			ArrayList<Pair> oPF = new ArrayList<Pair>(openProjFiles);
			ArrayList<Pair> pF = new ArrayList<Pair>(projFiles);
			
			// Calculate center of mass of open files
			double[] cm = new double[oPF.size()];
			int oPFSize = oPF.size();
			for(int i = 0; i < oPFSize; ++i) {
				double count = 0;
				/*
				 *  Each dimension needs an average of the distances with other
				 *  open files.  Each open file is given a dimension, so the
				 *  center of mass vector will be in R^m for m open files
				 */
				for(int j = 0; j < oPFSize; ++j) {
					Row index = new Row(oPF.get(i).getFile(),
						oPF.get(j).getFile(), project);
					
					if(simMtx.containsKey(index))
						count += (double) simMtx.get(index);
				}
				
				cm[i] = count / ((double) oPFSize);
			}
		
			// Calculate euclidean distance from center of mass of open files
			int pFSize = pF.size();
			ArrayList<FileCloseness> fCs = new ArrayList<FileCloseness>();
			for(int i = 0; i < pFSize; ++i) {
				// closeness stores distance from the center of mass
				double closeness = 0;
				Pair p = pF.get(i);
				for(int j = 0; j < oPFSize; ++j) {
					// If the file is already open, don't suggest it
					if(oPF.contains(p)) {
						break;
					}
					
					Row index = new Row(oPF.get(j).getFile(),
						p.getFile(), project);
					
					if(simMtx.containsKey(index)) {
						double d = (cm[j] - ((double) simMtx.get(index)));
						closeness += (d * d);
					}
				}
				
				// Find Euclidean distance once all dimension distances found
				closeness = Math.sqrt(closeness);
				
				if(!oPF.contains(p)) {
					fCs.add(new FileCloseness(p.getFile(), closeness));
				}
			}
			
			// Display the recommendations in sorted order with highest first
			Collections.sort(fCs);
			ArrayList<TreeNode> children = new ArrayList<TreeNode>();
			for(int i = 0; i < fCs.size(); ++i) {
				children.add(new TreeNode(fCs.get(i).getFile()));
			}
			samplesTree.add(new TreeNode(project, children));
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
							sims[j] = projQueries.get(index).getCount();
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
	
	public ArrayList<Pair> listOpenFiles() {
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
}
