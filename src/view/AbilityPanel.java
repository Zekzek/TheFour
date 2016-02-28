package view;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.Ability;

public class AbilityPanel extends JPanel {
	private static final long serialVersionUID = -5640915321281094627L;
	
	private JLabel damageLabel;
	private JLabel delayLabel;
	private JLabel specialLabel;
	
	public AbilityPanel() {
		this.setLayout(new FlowLayout());
		damageLabel = new JLabel("Damage: ", SwingConstants.CENTER);
		add(damageLabel);
		delayLabel = new JLabel("Duration: ", SwingConstants.CENTER);
		add(delayLabel);
		specialLabel = new JLabel("Special: ", SwingConstants.CENTER);
		add(specialLabel);
		setVisible(false);
	}
	
	public void updateDisplay(Ability ability) {
		damageLabel.setText("Damage: " + ability.getDamage());
		delayLabel.setText("Duration: " + ability.getDelay());
		specialLabel.setText("Special: " + ability.getSpecial());
	}
}