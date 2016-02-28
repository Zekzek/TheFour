package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import model.Dialog;
import controller.BattleQueue;

public class DialogPanel extends JPanel{
	private static final long serialVersionUID = 1582923222552495250L;

	private static final Action dismissDialogAction = new AbstractAction(){
		private static final long serialVersionUID = -7716284876977116939L;
		@Override
		public void actionPerformed(ActionEvent e) {
			dismissDialog();
		}
	};
	private static final Action showNextDialogAction = new AbstractAction(){
		private static final long serialVersionUID = -1957062937323496697L;
		@Override
		public void actionPerformed(ActionEvent e) {
			showNextDialog();
		}
	};
	
	private static DialogPanel me;
	private static JLabel nameLabel;
	private static JTextArea contentArea;
	private static JButton nextButton;	
	private static List<Dialog> conversation = new ArrayList<Dialog>();
	private static boolean goToTitleOnConclusion = false;
	private static Runnable actionOnConclusion;
	
	public DialogPanel() {
		this.setLayout(new BorderLayout());
		nameLabel = new JLabel("", SwingConstants.CENTER);
		nameLabel.setOpaque(true);
		nameLabel.setBackground(Color.BLACK);
		nameLabel.setForeground(Color.WHITE);
		add(nameLabel, BorderLayout.NORTH);
		contentArea = new JTextArea();
		contentArea.setWrapStyleWord(true);
		contentArea.setLineWrap(true);
		contentArea.setEditable(false);
		add(contentArea, BorderLayout.CENTER);
		nextButton = new JButton();
		add(nextButton, BorderLayout.SOUTH);
		setVisible(false);
		me = this;
	}
	
	/**
	 * Add the speech to the dialog queue and display the next entry
	 * @param speech
	 */
	public static void showDialog(Dialog[] speech, Runnable actionOnConclusion) {
		DialogPanel.actionOnConclusion = actionOnConclusion;
		GameFrame.disableMenu();
		addDialog(speech);
		showNextDialog();
	}
	
	/**
	 * Add the speech to the dialog queue
	 * @param speech
	 */
	public static void addDialog(Dialog[] speech) {
		for (Dialog dialog : speech) {
			conversation.add(dialog);
		}
	}
	
	/**
	 * display the next entry
	 */
	public static void showNextDialog() {
		BattleQueue.pauseBattle();
		me.setVisible(true);
		
		Dialog dialog = conversation.remove(0);
		
		dialog.getSpeaker().convertNameLabel(nameLabel);
		contentArea.setText(dialog.getSpeech());
		if (conversation.isEmpty()) {
			nextButton.setAction(dismissDialogAction);
			nextButton.setText("End Conversation");
		} else {
			nextButton.setAction(showNextDialogAction);
			nextButton.setText("More");
		}
	}
	
	private static void dismissDialog() {
		me.setVisible(false);
		if (goToTitleOnConclusion) {
			goToTitleOnConclusion = false;
			GameFrame.returnToTitleScreen();
		} else {
			GameFrame.enableMenu();
			BattleQueue.resumeBattle();
			if (actionOnConclusion != null) {
				actionOnConclusion.run();
			}
		}
	}
	
	public static void setGoToTitleOnConclusion(boolean bool) {
		goToTitleOnConclusion = bool;
	}
	
	public static void setActionOnConclusion(Runnable action) {
		actionOnConclusion = action;
	}
}
