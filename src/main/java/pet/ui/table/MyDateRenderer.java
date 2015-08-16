package pet.ui.table;

import java.text.DateFormat;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

public class MyDateRenderer extends DefaultTableCellRenderer {
	@Override
	protected void setValue(Object value) {
		if (value instanceof Date) {
			value = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(value);
		}
		super.setValue(value);
	}
}