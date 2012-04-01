package pet.ui;

import java.text.DateFormat;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

class DateRenderer extends DefaultTableCellRenderer {
	@Override
	protected void setValue(Object value) {
		if (value instanceof Date) {
			value = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(value);
		}
		super.setValue(value);
	}
}