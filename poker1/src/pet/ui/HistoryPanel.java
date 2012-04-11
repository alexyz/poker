package pet.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.swing.*;

import pet.hp.*;
import pet.hp.info.*;

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
	
	private final JTextField pathField = new JTextField();
	private final JButton pathButton = new JButton("Change Path");
	private final JToggleButton followButton = new JToggleButton("Follow");
	private final JProgressBar progressBar = new JProgressBar();
	private final JButton addButton = new JButton("Add File");
	private final ConsolePanel consolePanel = new ConsolePanel();
	
	private final Date now = new Date();
	
	public HistoryPanel() {
		super(new BorderLayout());
		setDropTarget(new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent e) {
				e.acceptDrop(TransferHandler.LINK);
				Transferable t = e.getTransferable();
				try {
					List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					System.out.println("dropped " + files);
					if (files != null && files.size() > 0) {
						FollowThread ft = PokerFrame.getInstance().getFollow();
						for (File f : files) {
							ft.addFile(f);
						}
					}
					// TODO add to follow thread
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}));
		
		pathField.setEditable(false);
		pathField.setBorder(BorderFactory.createTitledBorder("Path"));
		pathField.setColumns(50);
		String home = System.getProperty("user.home");
		pathField.setText(home + "/Library/Application Support/PokerStars/HandHistory/tawvx");
		
		pathButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(pathField.getText());
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setMultiSelectionEnabled(false);
				int opt = fc.showOpenDialog(HistoryPanel.this);
				if (opt == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					if (f != null) {
						pathField.setText(f.toString());
						PokerFrame.getInstance().getFollow().setPath(f);
					}
				}
			}
		});
		
		followButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean follow = e.getStateChange() == ItemEvent.SELECTED;
				FollowThread ft = PokerFrame.getInstance().getFollow();
				ft.setPath(new File(pathField.getText()));
				ft.setFollow(follow);
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(pathField.getText());
				fc.setMultiSelectionEnabled(true);
				int opt = fc.showOpenDialog(HistoryPanel.this);
				if (opt == JFileChooser.APPROVE_OPTION) {
					File[] fs = fc.getSelectedFiles();
					if (fs != null && fs.length > 0) {
						for (File f : fs) {
							PokerFrame.getInstance().getFollow().addFile(f);
						}
					}
				}
			}
		});
		
		JPanel pathPanel = new JPanel();
		pathPanel.add(pathField);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(pathButton);
		buttonPanel.add(addButton);
		buttonPanel.add(followButton);
		
		JPanel topPanel = new JPanel(new GridLayout(3, 1));
		topPanel.setBorder(BorderFactory.createTitledBorder("History"));
		topPanel.add(pathPanel);
		topPanel.add(buttonPanel);
		topPanel.add(progressBar);
		
		add(topPanel, BorderLayout.NORTH);
		add(consolePanel, BorderLayout.CENTER);
	}
	
	@Override
	public void nextHand(Hand h) {
		if (h.date.after(now)) {
			System.out.println(h);
			//HandInfo.printhand2(h);
		}
	}

	@Override
	public void doneFile(final int done, final int total) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setMaximum(total - 1);
				progressBar.setValue(done);
			}
		});
	}
	
}
