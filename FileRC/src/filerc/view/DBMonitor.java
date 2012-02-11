package filerc.view;

import org.eclipse.swt.SWT;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import filerc.model.Row;

public class DBMonitor extends ViewPart {
	
	private TableViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |
			SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		// Create the columns
		createColumns(parent, viewer);
		
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		
		viewer.setContentProvider(new ArrayContentProvider());
		getSite().setSelectionProvider(viewer);
	}

	public TableViewer getViewer() {
		return viewer;
	}
	
	private void createColumns(final Composite parent,
		final TableViewer viewer) {
			// Column 1: File1
			TableViewerColumn col = createTableViewerColumn("File1", 300, 0);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Row row = (Row) element;
					return row.getFile1();
				}
			});
			
			// Column 2: File2
			col = createTableViewerColumn("File2", 300, 0);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Row row = (Row) element;
					return row.getFile2();
				}
			});
			
			// Column 3: Project
			col = createTableViewerColumn("Project", 100, 0);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Row row = (Row) element;
					return row.getProject();
				}
			});
			
			// Column 4: Interaction count
			col = createTableViewerColumn("Interaction Count", 100, 0);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Row row = (Row) element;
					return Integer.toString(row.getInteractionCount());
				}
			});
			
			// Column 5: SCM commit count
			col = createTableViewerColumn("SCM Count", 100, 0);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Row row = (Row) element;
					return Integer.toString(row.getScmCount());
				}
			});
			
			// Column 6: Static code commit count
			col = createTableViewerColumn("Static Code Count", 100, 0);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Row row = (Row) element;
					return Integer.toString(row.getStaticCodeCount());
				}
			});
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound,
		int colNum) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
			SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		
		return viewerColumn;
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
