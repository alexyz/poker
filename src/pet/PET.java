package pet;

import java.util.Locale;

import javax.swing.*;

import pet.hp.History;
import pet.ui.PokerFrame;

/**
 * Main class for gui
 */
public class PET {
	
	private static PokerFrame instance;
	
	/** all the parsed data */
	private static final History history = new History();
	
	/**
	 * get instance of gui
	 */
	public static PokerFrame getPokerFrame() {
		return instance;
	}
	
	public static History getHistory() {
		return history;
	}
	
	public static void main(String[] args) {
		// os x assumes US locale if system language is english...
		// user needs to add british english to list of languages in system preferences/language and text
		System.out.println("locale " + Locale.getDefault());
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						handleException("Error", e);
					}
				});
				// need to create and pack in awt thread otherwise it can deadlock
				// due to the java console panel
				instance = new PokerFrame();
				System.out.println("Poker Equity Tool - https://github.com/alexyz");
				instance.setVisible(true);
			}
		});
		
	}
	
	/**
	 * display dialog
	 */
	public static void handleException(final String title, final Throwable e) {
		e.printStackTrace(System.out);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(instance, 
						e.toString(), // + ": " + e.getMessage(), 
						title, 
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}
	
	
}
