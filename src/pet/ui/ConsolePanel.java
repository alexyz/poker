package pet.ui;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;

public class ConsolePanel extends JPanel {
	
	private static final OutputStream out = System.out;
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTextArea textArea = new JTextArea();
	
	
	public ConsolePanel() {
		super(new BorderLayout());
		
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		
		scrollPane.setViewportView(textArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Java Console"));
		add(scrollPane, BorderLayout.CENTER);
		
		// TODO start this sooner?
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
				if (textArea.getDocument().getLength() > 100000) {
					try {
						textArea.getDocument().remove(0, 10000);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		});
	}

	public void clear() {
		textArea.setText("");
	}
}
