package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import view.SpriteSheet.CLIMATE;
import controller.MapBuilder;
import controller.plot.Plot;
import controller.plot.Plot_SpeedTest;
import controller.plot.Plot_Tutorial_Arena;
import controller.plot.Plot_Tutorial_Defend_Sorc_Item;

public class TitleScreenPanel extends JPanel{
	private static final long serialVersionUID = -8163142113386384945L;

	private static TitleScreenPanel me;
	
	public TitleScreenPanel() {
		setLayout(new BorderLayout());
		add(new JLabel("The Four: Forgotten Age ~ Version 0.01", SwingConstants.CENTER), BorderLayout.NORTH);
		
		JPanel centerWrapperPanel = new JPanel();
		centerWrapperPanel.add(getPlotButton("Beginnings: Magic Case", Plot_Tutorial_Defend_Sorc_Item.class));
		centerWrapperPanel.add(getPlotButton("Beginnings: Arena", Plot_Tutorial_Arena.class));
		centerWrapperPanel.add(getClimateComboBox());
		add(centerWrapperPanel, BorderLayout.CENTER);
		
		JPanel southWrapperPanel = new JPanel();
		southWrapperPanel.add(getPlotButton("Test: Speed Test", Plot_SpeedTest.class));
		add(southWrapperPanel, BorderLayout.SOUTH);
		
		me = this;
	}
	
	private JComboBox<CLIMATE> getClimateComboBox() {
		JComboBox<CLIMATE> climateSelector = new JComboBox<CLIMATE>(CLIMATE.values());
		climateSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<CLIMATE> cb = (JComboBox<CLIMATE>)e.getSource();
		        CLIMATE climate = (CLIMATE) cb.getSelectedItem();
		        MapBuilder.setClimate(climate);
			}
		});
		
		return climateSelector;
	}
	
	private JButton getPlotButton(String name, Class<? extends Plot> theClass) {
		Action startPlot = new AbstractAction(name){
			private static final long serialVersionUID = -8836958976794130145L;
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Plot plot = (Plot) theClass.newInstance();
					plot.start();
					me.setVisible(false);
				} catch (InstantiationException | IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
		};
		JButton button = new JButton(startPlot);
		return button;
	}
}
