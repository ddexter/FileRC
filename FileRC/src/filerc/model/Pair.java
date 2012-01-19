package filerc.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.text.Collator;
import java.util.Locale;

public class Pair implements Comparable<Pair> {
	private String file;
	private String project;

	public Pair(String file, String project) {
		this.file = file;
		this.project = project;
	}
	
	public int compareTo(Pair p2) {
		Collator collator = Collator.getInstance(new Locale("en", "US"));
		int alphOrder = collator.compare(this.file, p2.file);
		
		// Break tie with project name
		if(alphOrder == 0) {
			alphOrder = collator.compare(this.project, p2.project);
		}
		
		return alphOrder;
	}
	
	public int hashCode() {
		// Hash based only on the file and project names
		return new HashCodeBuilder(17, 37).
			append(file).
			append(project).
			toHashCode();
	}
	
	public boolean equals(Object o) {
		if(o == this)
			return true;
		if(o == null)
			return false;
		if(o.getClass() != getClass())
			return false;
		
		Pair p2 = (Pair) o;
		return new EqualsBuilder().
			append(file, p2.file).
			append(project, p2.project).
			isEquals();
	}
	
	public String getFile() {
		return file;
	}
	
	public String getProject() {
		return project;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setProject(String project) {
		this.project = project;
	}
}
