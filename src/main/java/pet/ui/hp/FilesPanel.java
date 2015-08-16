package pet.ui.hp;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;

import pet.PET;
import pet.hp.impl.FTParser;
import pet.hp.impl.HistoryUtil;
import pet.hp.impl.PSParser;
import pet.hp.impl.StringCache;
import pet.hp.info.FollowThread;
import pet.ui.ConsolePanel;

public class FilesPanel extends JPanel {

	private static void mem() {
		Runtime r = Runtime.getRuntime();
		r.gc();
		double mib = Math.pow(2,20);
		System.out.println(String.format("memory max: %.3f total: %.3f free: %.3f used: %.3f (MiB)",
				r.maxMemory() / mib,
				r.totalMemory() / mib,
				r.freeMemory() / mib,
				(r.totalMemory() - r.freeMemory()) / mib));
		System.out.println("hands: " + PET.getHistory().getHands());
		System.out.println("Tournaments: " + PET.getHistory().getTourns());
		System.out.println("String cache: " + StringCache.size());
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
		
		PSParser psParser = new PSParser();
		psParser.setHistory(PET.getHistory());
		starsPanel.setPath(HistoryUtil.getStarsPath(psParser));
		FollowThread psThread = new FollowThread(psParser);
		psThread.start();
		starsPanel.setThread(psThread);
		
		FTParser ftParser = new FTParser();
		ftParser.setHistory(PET.getHistory());
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
