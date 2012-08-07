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
		try {
			edit.setPage(getClass().getResource("/index.html"));
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		add(scroll, BorderLayout.CENTER);
	}
	
}
