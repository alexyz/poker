package pet;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

public class Log {
	
	public static final List<PrintStream> outs = new Vector<PrintStream>();
	static {
		Locale.setDefault(Locale.UK);
		outs.add(System.out);
	}
	
	private final Class<?> cl;
	
	public Log(Class<?> cl) {
		this.cl = cl;
	}
	
	public void println(String s) {
		String tn = Thread.currentThread().getName();
		String cn = cl.getName();
		String d = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());
		String msg = d + " (" + tn + ") [" + cn + "] " + s;
		for (PrintStream ps : outs) {
			ps.println(msg);
		}
	}
}
