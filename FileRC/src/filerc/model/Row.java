package filerc.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import java.text.Collator;
import java.util.Locale;

// file1, file2 will always be ordered alphabetically
public class Row {
	private int interactionCount;
	private int scmCount;
	private int staticCodeCount;
	private String file1;
	private String file2;
	private String project;

	public Row(String file1, String file2, String project) {
		interactionCount = 0;
		scmCount = 0;
		staticCodeCount = 0;
		this.file1 = file1;
		this.file2 = file2;
		sort();
		this.project = project;
	}
	
	public Row(int interactionCount, int scmCount, int staticCodeCount,
		String file1, String file2, String project) {
		
		this.interactionCount = interactionCount;
		this.scmCount = scmCount;
		this.staticCodeCount = staticCodeCount;
		this.file1 = file1;
		this.file2 = file2;
		sort();
		this.project = project;
	}

	public String getFile1() {
		return file1;
	}
	
	public String getFile2() {
		return file2;
	}
	
	public String getProject() {
		return project;
	}
	
	public int getInteractionCount() {
		return interactionCount;
	}
	
	public int getScmCount() {
		return scmCount;
	}
	
	public int getStaticCodeCount() {
		return staticCodeCount;
	}
	
	public void setInteractionCount(int interactionCount) {
		this.interactionCount = interactionCount;
	}
	
	public void setScmCount(int scmCount) {
		this.scmCount = scmCount;
	}
	
	public void setStaticCodeCount(int staticCodeCount) {
		this.staticCodeCount = staticCodeCount;
	}
	
	public void setFile1(String file1) {
		this.file1 = file1;
		sort();
	}
	
	public void setFile2(String file2) {
		this.file2 = file2;
		sort();
	}
	
	public void setProject(String project) {
		this.project = project;
	}
	
	private void sort() {
		Collator collator = Collator.getInstance(new Locale("en", "US"));
	
		// If out of order, swap the files, file1 should be alphabetically first
		if(collator.compare(file1, file2) > 0) {
			String tmp = new String(file1);
			file1 = file2;
			file2 = tmp;
		}
	}
	
	public int hashCode() {
		// Hash based only on the file and project names
		return new HashCodeBuilder(17, 37).
			append(file1).
			append(file2).
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
	
		Row r2 = (Row) o;
		return new EqualsBuilder().
			append(file1, r2.file1).
			append(file2, r2.file2).
			append(project, r2.project).
			isEquals();
	}
}
