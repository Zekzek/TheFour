package view;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import model.Ability;

public class AbilityPanel extends JPanel {
	private static final long serialVersionUID = -5640915321281094627L;
	
	private JLabel damageLabel;
	private JLabel delayLabel;
	private JTextArea specialLabel;
	
	public AbilityPanel() {
		this.setLayout(new BorderLayout());
		JPanel upperPanel = new JPanel(new BorderLayout());
		damageLabel = new JLabel("Damage: ", SwingConstants.CENTER);
		upperPanel.add(damageLabel, BorderLayout.WEST);
		delayLabel = new JLabel("Time: ", SwingConstants.CENTER);
		upperPanel.add(delayLabel, BorderLayout.EAST);
		add(upperPanel, BorderLayout.NORTH);
		specialLabel = new JTextArea("Special: ");
		specialLabel.setWrapStyleWord(true);
		specialLabel.setLineWrap(true);
		specialLabel.setEditable(false);
		specialLabel.setOpaque(false);
		add(specialLabel, BorderLayout.CENTER);
		setVisible(false);
	}
	
	public void updateDisplay(Ability ability) {
		if (ability.getDamage() > 0) {
			damageLabel.setText("Damage: " + ability.getDamage());
		} else {
			damageLabel.setText("");
		}
		delayLabel.setText("Time: " + ability.getDelay());
		specialLabel.setText("Special: " + ability.getSpecial());
	}
}