package pet.ui.hp;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.info.*;
import pet.ui.*;

/**
 * panel for parsing files
 */
public class HistoryPanel extends JPanel implements FollowListener {
	
	private final JTextField pathField = new JTextField();
	private final JButton pathButton = new JButton("Change Path");
	private final JToggleButton followButton = new JToggleButton("Follow");
	private final JProgressBar progressBar = new JProgressBar();
	private final JButton addButton = new JButton("Add File");
	private final ConsolePanel consolePanel = new ConsolePanel();
	private final JCheckBox hudBox = new JCheckBox("Create HUDs");
	private final JButton funcButton = new JButton("Memory");
	private final JButton clearButton = new JButton("Clear");
	private final JPanel buttonPanel = new JPanel();
	private final JSpinner ageSpinner = new JSpinner();
	
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
				} catch (Exception e1) {
					PokerFrame.handleException("DND", e1);
				}
			}
		}));
		
		pathField.setEditable(false);
		
		String path = getPath();
		pathField.setText(path);
		
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
				ft.setAge(((SpinnerNumberModel)ageSpinner.getModel()).getNumber().intValue());
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
		
		// doesn't work because hud manager has not yet been created...
		//hudBox.setSelected(PokerFrame.getInstance().getHudManager().isCreate());
		hudBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				PokerFrame.getInstance().getHudManager().setCreate(hudBox.isSelected());
			}
		});
		
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				consolePanel.clear();
			}
		});
		
		funcButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//PokerFrame.getInstance().f();
				mem();
			}
		});
		
		ageSpinner.setModel(new SpinnerNumberModel(7, 0, 999, 1));
		ageSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				FollowThread ft = PokerFrame.getInstance().getFollow();
				ft.setAge(((SpinnerNumberModel)ageSpinner.getModel()).getNumber().intValue());
			}
		});
		
		buttonPanel.add(clearButton);
		buttonPanel.add(funcButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel pathPanel = new JPanel();
		pathPanel.add(pathField);
		pathPanel.add(pathButton);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton);
		buttonPanel.add(new JLabel("Age (days)"));
		buttonPanel.add(ageSpinner);
		buttonPanel.add(followButton);
		buttonPanel.add(hudBox);
		
		JPanel topPanel = new JPanel(new GridLayout(3, 1));
		topPanel.setBorder(BorderFactory.createTitledBorder("History"));
		topPanel.add(pathPanel);
		topPanel.add(buttonPanel);
		topPanel.add(progressBar);
		
		add(topPanel, BorderLayout.NORTH);
		add(consolePanel, BorderLayout.CENTER);
	}
	
	/** get the pokerstars hand history directory */
	private static String getPath() {
		// C:\Users\Alex\AppData\Local\PokerStars\HandHistory\
		// /Users/alex/Library/Application Support/PokerStars/HandHistory/tawvx
		String home = System.getProperty("user.home");
		String os = System.getProperty("os.name");
		String path = null;
		if (os.equals("Mac OS X")) {
			path = home + "/Library/Application Support/PokerStars/HandHistory";
		} else if (os.contains("Windows")) {
			// could be something like PokerStars.FR instead
			path = home + "\\AppData\\Local\\PokerStars\\HandHistory";
		}
		if (path != null) {
			File f = new File(path);
			if (f.exists() && f.isDirectory()) {
				// get the first directory
				for (File f2 : f.listFiles()) {
					if (f2.isDirectory()) {
						path = f2.getPath();
						break;
					}
				}
			} else {
				System.out.println("could not find dir " + f);
				path = null;
			}
		}
		if (path == null) {
			path = home;
		}
		return path;
	}
	
	private static void mem() {
		Runtime r = Runtime.getRuntime();
		r.gc();
		double mib = Math.pow(2,20);
		int h = PokerFrame.getInstance().getHistory().getHands();
		System.out.println(String.format("memory max: %.3f total: %.3f free: %.3f used: %.3f (MiB) hands: %d",
				r.maxMemory() / mib,
				r.totalMemory() / mib,
				r.freeMemory() / mib,
				(r.totalMemory() - r.freeMemory()) / mib,
				h));
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
