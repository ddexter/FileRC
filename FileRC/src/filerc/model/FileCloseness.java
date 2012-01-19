package filerc.model;

public class FileCloseness implements Comparable<FileCloseness> {
	private double closeness;
	private String file;

	public FileCloseness(String file, double closeness) {
		this.file = file;
		this.closeness = closeness;
	}
	
	public String getFile() {
		return file;
	}
	
	public double getCloseness() {
		return closeness;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setCloseness(Double closeness) {
		this.closeness = closeness;
	}
	
	public int compareTo(FileCloseness fc2) {
		return Double.compare(closeness, fc2.getCloseness());
	}
}
