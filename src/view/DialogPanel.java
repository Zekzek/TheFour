package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private final Action dismissDialogAction = new AbstractAction(){
		private static final long serialVersionUID = -7716284876977116939L;
		@Override
		public void actionPerformed(ActionEvent e) {
			dismissDialog();
		}
	};
	private final Action showNextDialogAction = new AbstractAction(){
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
	private BattleQueue battleQueue;
	private Set<IMenuListener> menuListeners;
	
	public DialogPanel(BattleQueue battleQueue) {
		this.battleQueue = battleQueue;
		menuListeners = new HashSet<IMenuListener>();
		
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
	
	//TODO: stop using static calls
	/**
	 * Add the speech to the dialog queue and display the next entry
	 * @param speech
	 */
	public void showDialog(Dialog[] speech, Runnable actionOnConclusion) {
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
	public void showNextDialog() {
		battleQueue.setPause(true);
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
	
	private void dismissDialog() {
		setVisible(false);
		if (goToTitleOnConclusion) {
			goToTitleOnConclusion = false;
			for (IMenuListener listener : menuListeners) {
				listener.onSceneComplete();
			}
		} else {
			GameFrame.enableMenu();
			battleQueue.setPause(false);
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

	public void addMenuListener(IMenuListener listener) {
		menuListeners.add(listener);
	}
}
