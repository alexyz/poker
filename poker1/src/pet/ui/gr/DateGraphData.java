package pet.ui.gr;

import java.text.DateFormat;
import java.util.*;

/**
 * graph data where the x axis is date
 */
public class DateGraphData extends GraphData {
	
	@Override
	public String getXName(int x) {
		return getDayName(x);
	}

	public static String getDayName(int x) {
		int y = (x / 366) + 2000;
		int d = (x % 366) + 1;
		Calendar cal = new GregorianCalendar(y, 0, 1);
		cal.add(Calendar.DAY_OF_YEAR, d);
		String s = DateFormat.getDateInstance(DateFormat.SHORT).format(cal.getTime());
		return s;
	}
	
	public static int getDayNumber(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int y = cal.get(Calendar.YEAR) - 2000;
		int d = cal.get(Calendar.DAY_OF_YEAR) - 1;
		int id = y * 366 + d;
		return id;
	}
	
}
