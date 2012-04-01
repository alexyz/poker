package pet.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.swing.*;

import pet.hp.*;
import pet.hp.impl.PSParser;
import pet.hp.util.*;

/**
 * TODO
 * [path text field] follow
 * [progress bar (hands, players)]
 * [add file (no follow)]
 * [rejected files]
 * dnd
 * 
 */
public class HistoryPanel extends JPanel implements FollowListener {
	
	public static FollowThread followThread;
	
	private final JLabel pathLabel = new JLabel();
	private final JButton browseButton = new JButton("Browse");
	private final JButton addButton = new JButton("Add");
	private final JToggleButton runButton = new JToggleButton("Run");
	private final Date now = new Date();
	
	public HistoryPanel() {
		setDropTarget(new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent e) {
				e.acceptDrop(TransferHandler.LINK);
				Transferable t = e.getTransferable();
				try {
					List<?> files = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
					System.out.println("dropped " + files);
					// TODO add to follow thread
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}));
		
		String home = System.getProperty("user.home");
		pathLabel.setText(home + "/Library/Application Support/PokerStars/HandHistory/tawvx");
		
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(pathLabel.getText());
				fc.setMultiSelectionEnabled(false);
				int opt = fc.showOpenDialog(HistoryPanel.this);
				if (opt == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					if (f != null) {
						pathLabel.setText(f.toString());
					}
				}
			}
		});
		
		addButton.setEnabled(false);
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				followThread.addFile(pathLabel.getText());
			}
		});
		
		runButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				start(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(pathLabel);
		add(browseButton);
		add(addButton);
		add(runButton);
	}
	
	private void start(boolean go) {
		if (followThread != null) {
			followThread.stop = !go;
			
		} else {
			PSParser parser = new PSParser();
			FollowThread ft = new FollowThread(parser);
			ft.addFile(pathLabel.getText());
			ft.addListener(this);
			ft.addListener(PokerFrame.getInstance().getHistory());
			ft.start();
			followThread = ft;
			addButton.setEnabled(true);
		}
	}

	@Override
	public void nextHand(Hand h) {
		if (h.date.after(now)) {
			System.out.println(h);
			//HandInfo.printhand2(h);
		}
	}
	
}
