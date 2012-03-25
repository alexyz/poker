package pet.ui;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class ConsolePanel extends JPanel {
	
	private static final OutputStream out = System.out;
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTextArea textArea = new JTextArea();
	private final JPanel buttonPanel = new JPanel();
	private final JButton clearButton = new JButton("Clear");
	
	public ConsolePanel() {
		super(new BorderLayout());
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Java Console"));
		add(scrollPane, BorderLayout.CENTER);
		buttonPanel.add(clearButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		
		OutputStream os = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				out.write(b);
				wr(String.valueOf((char) b));
			}
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				out.write(b, off, len);
				wr(new String(b, off, len));
			}
		};
		System.setOut(new PrintStream(os));
		System.setErr(new PrintStream(os));
	}
	
	private void wr(final String s) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textArea.append(s);
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		});
	}
}
