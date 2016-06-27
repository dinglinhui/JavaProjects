package pcclient;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * class TransTableModel
 * @version 1.0
 * @author dinglinhui
 */
class TransTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private String[] headings;
	private Vector vect = new Vector();// 实例化向量

	public TransTableModel(String[] head) {
		headings = head;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	@Override
	public int getRowCount() {
		/*
		 * if (data == null) return 0; else return data.length;
		 */
		return vect.size();
	}

	@Override
	public int getColumnCount() {
		return headings.length;
	}

	@Override
	public Object getValueAt(int row, int column) {
		// return data[row][column];
		if (!vect.isEmpty())
			return ((Vector) vect.elementAt(row)).elementAt(column);
		else
			return null;
	}

	@Override
	public String getColumnName(int column) {
		return headings[column];
	}

	public void addElement(Vector<String> list) {
		vect.addElement(list);
	}

	public void clearModel() {
		vect.clear();
	}
}
