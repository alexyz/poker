package pet.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import javax.swing.*;
import javax.swing.event.*;

public class AboutPanel extends JPanel {
	
	private final JEditorPane edit = new JEditorPane();
	private final JScrollPane scroll = new JScrollPane(edit);
	
	public AboutPanel() {
		super(new BorderLayout());
		edit.setEditable(false);
		edit.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(URI.create(e.getURL().toString()));
					} catch (Exception e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					// this can get stuck in an infinite loop when laying out
					// will hopefully avoid it by loading after the editor has been sized
					edit.setPage(getClass().getResource("/index.html"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		add(scroll, BorderLayout.CENTER);
	}
	
}
