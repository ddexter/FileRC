package filerc.model;

import java.io.File;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SQLiteWrapper {
	// These values are used to specify the type of count to increment
	public static final String INTERACTION_COUNT = "interaction_count";
	public static final String SCM_COUNT = "scm_count";
	public static final String STATIC_CODE_COUNT = "static_code_count";
	private static final String DEFAULT_DB = "FileTrackerDB";
	private static final String Q = "\""; // Quote character used by SQLite
	
	private Connection connection;
	private String dbName;
	
	public SQLiteWrapper() {
		dbName = DEFAULT_DB;
		
		setupDB();
		
		if(!tableExists("samples"))
			createTable("samples");
	}
	
	public SQLiteWrapper(String dbName) {
		this.dbName = dbName;
		
		setupDB();
		
		if(!tableExists("samples"))
			createTable("samples");
		
	}
	
	private void addRow(Row row) {
		// Check that a duplicate entry does not exist
		if(entryExists(row)) {
			System.err.println("Method SQLiteWrapper.addPair(Row):");
			System.err.println("Row already exists");
			return;
		}
		
		PreparedStatement stmt = null;
		
		String sql = "INSERT INTO samples VALUES(?,?,?,?,?,?)";
		/*
		 * Surround table name with identifier quotes to protect against
		 * SQL injections
		 */
		sql = String.format(sql, Q, row.getProject().replaceAll(Q, Q+Q), Q);
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, row.getFile1());
			stmt.setString(2, row.getFile2());
			stmt.setString(3, row.getProject());
			/*
			 * Row 4: File interactions
			 * Row 5: SCM commits
			 * Row 6: Static code dependencies
			 */
			stmt.setInt(4, row.getInteractionCount());
			stmt.setInt(5, row.getScmCount());
			stmt.setInt(6, row.getStaticCodeCount());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Method SQLiteWrapper.addPair(Row):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("Method SQLiteWrapper.addPair(Row):");
					System.err.println(e.getMessage());
				}
		}
		
	}
	
	public void clearSamples() {
		// Ensure that the table exists
		if(!tableExists("samples")) {
			System.err.println("SQLiteWrapper.clearSamples():");
			System.err.println("table 'samples' does not exist");
			return;
		}
		
		Statement stmt = null;
		String sql = "DELETE FROM samples";
		
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.clearSamples():");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.clearSamples():");
					System.err.println(e.getMessage());
				}
		}
	}
	
	private void closeDB() {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.closeDB()");
			System.err.println(e.getMessage());
		}
	}
	
	private void connectToDB() {
		try {
			String dbPath = "jdbc:sqlite:" + dbName + ".db";
			connection = DriverManager.getConnection(dbPath);
		} catch(SQLException e) {
			System.err.println("SQLiteWrapper.connectToDB():");
			System.err.println(e.getMessage());
		}
		
	}
	
	public void createTable(String table) {
		// Ensure that the table does not already exist for this project
		if(tableExists(table)) {
			System.err.println("SQLiteWrapper.createTable(String):");
			System.err.println("Table " + table + " already exists");
			return;
		}
		
		Statement stmt = null;
		String sql = "CREATE TABLE %s%s%s(" +
		"file1 varchar(200) NOT NULL," +
		"file2 varchar(200) NOT NULL," +
		"project varchar(100) NOT NULL," +
		"interaction_count INTEGER NOT NULL DEFAULT 0," +
		"scm_count INTEGER NOT NULL DEFAULT 0," +
		"static_code_count INTEGER NOT NULL DEFAULT 0," + 
		"PRIMARY KEY (file1, file2, project))";
		/*
		 * Surround table name with identifier quotes to protect against
		 * SQL injections
		 */
		sql = String.format(sql, Q, table.replaceAll(Q, Q+Q), Q);
	
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.createTable(String):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.createTable(String):");
					System.err.println(e.getMessage());
				}
		}
	}
	
	public void deleteRow(Row row) {
		// Ensure that row exists
		if(!entryExists(row)) {
			System.err.println("SQLiteWrapper.deleteRow:");
			System.err.println("Row does not exist");
			return;
		}
		
		PreparedStatement stmt = null;
		String sql = "DELETE FROM samples " +
			"WHERE file1 = ? AND file2 = ? AND project = ?";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, row.getFile1());
			stmt.setString(2, row.getFile2());
			stmt.setString(3, row.getProject());
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.deleteRow(Row):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.deleteRow(Row):");
					System.err.println(e.getMessage());
				}
		}
	}
	
	@SuppressWarnings("unused")
	public void deleteTable(String table) {
		// Ensure that table exists
		if(!tableExists(table)) {
			System.err.println("SQLiteWrapper.deleteTable(String)");
			System.err.println("Table " + table + " does not exist");
		}
		
		Statement stmt = null;
		String sql = "DROP TABLE %s%s%s";
		sql = String.format(sql, Q, table.replaceAll(Q, Q+Q), Q);
		
		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.deleteTable(String):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.deleteTable(String):");
					System.err.println(e.getMessage());
				}
		}
	}
	
	public void deleteDB() {
		closeDB();
		String dbPath = dbName + ".db";
		File f = new File(dbPath);
		
		// Check that the database exists
		if(!f.exists()) {
			System.err.println("SQLiteWrapper.deleteDB():");
			System.err.println("Database " + dbPath + " doesn't exist");
			return;
		}
		
		// Check that database is not write-protected
		if(!f.exists()) {
			System.err.println("SQLiteWrapper.deleteDB():");
			System.err.println("Database " + dbPath + " is read-only");
			return;
		}
		
		f.delete();
	}
	
	public boolean entryExists(Row row) {
		PreparedStatement stmt = null;
		String sql =  "SELECT 1 FROM samples " +
			"WHERE file1 = ? AND file2 = ? AND project = ?";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, row.getFile1());
			stmt.setString(2, row.getFile2());
			stmt.setString(3, row.getProject());
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
				return true;
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.entryExists(Row):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.entryExists(Row):");
					System.err.println(e.getMessage());
				}
		}
		
		return false;
	}

	// Used to retrieve all file relationships
	public ArrayList<Row> getAllSamples() {
		Statement stmt = null;
		ArrayList<Row> ret = new ArrayList<Row>();
		
		String sql = "SELECT * FROM samples";
		
		try {
			stmt = connection.createStatement();
			
			// Get all rows that have pair.file as one of the files
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()) {
				String f1 = rs.getString("file1");
				String f2 = rs.getString("file2");
				String p = rs.getString("project");
				int interactionCount = rs.getInt("interaction_count");
				int scmCount = rs.getInt("scm_count");
				int staticCodeCount = rs.getInt("static_code_count");
				
				ret.add(new Row(interactionCount, scmCount, staticCodeCount,
					f1, f2, p));
			}
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.getTable(String):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.getTable(String):");
					System.err.println(e.getMessage());
				}
		}
		
		return ret;
	}
	
	public ArrayList<String> getProjectNames() {
		Statement stmt = null;
		ArrayList<String> ret = new ArrayList<String>();
		String sql = "SELECT DISTINCT project FROM samples";
		
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				ret.add(rs.getString("project"));
			}
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.getProjectNames():");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.getProjectNames():");
					System.err.println(e.getMessage());
				}
		}
		
		return ret;
	}
	
	public ArrayList<Row> getProjectSamples(String project) {
		PreparedStatement stmt = null;
		ArrayList<Row> ret = new ArrayList<Row>();
		
		String sql = "SELECT * FROM samples WHERE project = ?";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, project);
			
			// Get all rows that have pair.file as one of the files
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String f1 = rs.getString("file1");
				String f2 = rs.getString("file2");
				String p = rs.getString("project");
				int interactionCount = rs.getInt("interaction_count");
				int scmCount = rs.getInt("scm_count");
				int staticCodeCount = rs.getInt("static_code_count");
				
				ret.add(new Row(interactionCount, scmCount, staticCodeCount,
					f1, f2, p));
			}
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.getTable(String):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.getTable(String):");
					System.err.println(e.getMessage());
				}
		}
		
		return ret;
	}
	
	// Used for finding all elements related to a specific file
	public ArrayList<Row> getRelatedFiles(Pair pair) {
		PreparedStatement stmt = null;
		ArrayList<Row> ret = new ArrayList<Row>();
		String sql = "SELECT * FROM samples " +
			"WHERE file1 = ? OR file2 = ? AND project = ?";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, pair.getFile());
			stmt.setString(2, pair.getFile());
			stmt.setString(3, pair.getProject());
			
			// Get all rows that have pair.file as one of the files
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				String f1 = rs.getString("file1");
				String f2 = rs.getString("file2");
				int interactionCount = rs.getInt("interaction_count");
				int scmCount = rs.getInt("scm_count");
				int staticCodeCount = rs.getInt("static_code_count");
				
				ret.add(new Row(interactionCount, scmCount, staticCodeCount,
					f1, f2, pair.getProject()));
			}
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.getRelatedElements(Pair)");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.getRelatedElements(Pair)");
					System.err.println(e.getMessage());
				}
		}
		
		return ret;
	}
	
	/*
	 * Used to get related files for a list of file names.  The returned list
	 * will be distinct.  Even though making multiple queries is a performance
	 * hit, we do so for the sake of protecting against SQL injections here.
	 * In real projects, the number of queries should be small.
	 */
	public ArrayList<Row> getRelatedFiles(ArrayList<Pair> pairs) {
		Set<Row> ret = new HashSet<Row>();
		for(Pair pair : pairs) {
			ret.addAll(getRelatedFiles(pair));
		}
		
		return new ArrayList<Row>(ret);
	}
	
	// Assumes that the entry exists, if not, creates it
	public void incCount(Row row) {
		PreparedStatement stmt = null;
		
		// Create the strings for incrementing each type of count
		String interactionCount = "INTERACTION_COUNT = INTERACTION_COUNT + " +
			Integer.toString(row.getInteractionCount()) + ", ";
		String scmCount = "SCM_COUNT = SCM_COUNT + " +
			Integer.toString(row.getScmCount()) + ", ";
		String staticCodeCount = "STATIC_CODE_COUNT = STATIC_CODE_COUNT + " +
			Integer.toString(row.getStaticCodeCount()) + " ";
		
		String sql = "UPDATE samples " +
		    "SET " + interactionCount + scmCount + staticCodeCount + 
			"WHERE file1 = ? AND file2 = ? AND project = ?";
		
		// Ensure that the entry exists and create a new entry if it does not
		if(!entryExists(row)) {
			addRow(row);
		}
		else {
			try {
				stmt = connection.prepareStatement(sql);
				stmt.setString(1, row.getFile1());
				stmt.setString(2, row.getFile2());
				stmt.setString(3, row.getProject());
				stmt.executeUpdate();
			} catch (SQLException e) {
				System.err.println("SQLiteWrapper.incPairCount(Row):");
				System.err.println(e.getMessage());
			} finally {
				if(stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						System.err.println("SQLiteWrapper.incPairCount(Row):");
						System.err.println(e.getMessage());
					}
				}
			}
		}
	}
	
	/* 
	 * Set the SQLite JDBC driver.  This driver must be imported as a separate
	 * eclipse plug-in.  I am using:
	 * http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC
	 */
	private void setJDBCDriver() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void setupDB() {
		setJDBCDriver();
		connectToDB();
	}
	
	private boolean tableExists(String table) {
		PreparedStatement stmt = null;
		String sql = "SELECT name FROM sqlite_master " +
			"WHERE type = 'table' AND name = ?";
		
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, table);
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next())
				return true;
		} catch (SQLException e) {
			System.err.println("SQLiteWrapper.tableExists(String):");
			System.err.println(e.getMessage());
		} finally {
			if(stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					System.err.println("SQLiteWrapper.tableExists(String):");
					System.err.println(e.getMessage());
				}
		}
		
		return false;
	}
}
