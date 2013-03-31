package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;

import pet.PET;
import pet.hp.impl.FTParser;
import pet.hp.impl.HistoryUtil;
import pet.hp.impl.PSParser;
import pet.hp.info.FollowThread;
import pet.ui.hp.HistoryPanel;

public class FilesPanel extends JPanel {

	private static void mem() {
		Runtime r = Runtime.getRuntime();
		r.gc();
		double mib = Math.pow(2,20);
		int h = PET.getHistory().getHands();
		System.out.println(String.format("memory max: %.3f total: %.3f free: %.3f used: %.3f (MiB) hands: %d",
				r.maxMemory() / mib,
				r.totalMemory() / mib,
				r.freeMemory() / mib,
				(r.totalMemory() - r.freeMemory()) / mib,
				h));
	}
	
	private final JButton funcButton = new JButton("Memory");
	private final JButton clearButton = new JButton("Clear");
	private final ConsolePanel consolePanel = new ConsolePanel();
	private final JPanel buttonPanel = new JPanel();
	private final JTabbedPane historyPane = new JTabbedPane();
	private final HistoryPanel starsPanel = new HistoryPanel();
	private final HistoryPanel tiltPanel = new HistoryPanel();
	
	public FilesPanel() {
		super(new BorderLayout());
		
		PSParser psParser = new PSParser(PET.getHistory());
		starsPanel.setPath(HistoryUtil.getStarsPath(psParser));
		FollowThread psThread = new FollowThread(psParser);
		psThread.start();
		starsPanel.setThread(psThread);
		
		FTParser ftParser = new FTParser();
		tiltPanel.setPath(HistoryUtil.getTiltPath(ftParser));
		FollowThread ftThread = new FollowThread(ftParser);
		ftThread.start();
		tiltPanel.setThread(ftThread);
		
		historyPane.addTab("PokerStars", starsPanel);
		historyPane.addTab("Full Tilt", tiltPanel);
		
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				consolePanel.clear();
			}
		});
		
		funcButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mem();
			}
		});
		buttonPanel.add(clearButton);
		buttonPanel.add(funcButton);
		
		add(historyPane, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		add(consolePanel, BorderLayout.CENTER);
	}
	
}
