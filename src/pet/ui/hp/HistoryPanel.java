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

import pet.PET;
import pet.hp.info.*;

/**
 * panel for parsing files
 */
public class HistoryPanel extends JPanel implements FollowListener {
	
	private final JTextField pathField = new JTextField();
	private final JButton pathButton = new JButton("Change Path");
	private final JToggleButton followButton = new JToggleButton("Follow");
	private final JProgressBar progressBar = new JProgressBar();
	private final JButton addButton = new JButton("Add File");
	private final JCheckBox hudBox = new JCheckBox("Create HUDs");
	private final JSpinner ageSpinner = new JSpinner();
	
	private FollowThread thread;
	
	public HistoryPanel() {
		super(new BorderLayout());
		setDropTarget(new DropTarget(this, new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent e) {
				e.acceptDrop(TransferHandler.LINK);
				Transferable t = e.getTransferable();
				try {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					System.out.println("dropped " + files);
					if (files != null && files.size() > 0) {
						for (File f : files) {
							thread.addFile(f);
						}
					}
				} catch (Exception e1) {
					PET.handleException("DND", e1);
				}
			}
		}));
		
		pathField.setEditable(false);
		
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
						thread.setPath(f);
					}
				}
			}
		});
		
		followButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean follow = e.getStateChange() == ItemEvent.SELECTED;
				thread.setPath(new File(pathField.getText()));
				thread.setAge(((SpinnerNumberModel)ageSpinner.getModel()).getNumber().intValue());
				thread.setFollow(follow);
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
							thread.addFile(f);
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
				PET.getPokerFrame().getHudManager().setCreate(hudBox.isSelected());
			}
		});
		
		ageSpinner.setModel(new SpinnerNumberModel(7, 0, 999, 1));
		ageSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				thread.setAge(((SpinnerNumberModel)ageSpinner.getModel()).getNumber().intValue());
			}
		});
		
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
	}
	
	public void setPath(File path) {
		pathField.setText(path.getPath());
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

	public FollowThread getThread () {
		return thread;
	}

	public void setThread (FollowThread thread) {
		this.thread = thread;
	}
	
}
