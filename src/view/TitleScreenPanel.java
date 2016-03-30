package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

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
import controller.plot.Plot_Tutorial_Beserker_Deceit;
import controller.plot.Plot_Tutorial_Defend_Sorc_Item;

public class TitleScreenPanel extends JPanel{
	private static final long serialVersionUID = -8163142113386384945L;

	private static TitleScreenPanel me;
	private static Map<String, Class<? extends Plot>> plotOptions;
	private static String selectedPlotString;
	
	public TitleScreenPanel() {
		plotOptions = new HashMap<String, Class<? extends Plot>>();
		plotOptions.put("Beginnings: Magic Case", Plot_Tutorial_Defend_Sorc_Item.class);
		plotOptions.put("Beginnings: Arena", Plot_Tutorial_Arena.class);
		plotOptions.put("Beginnings: Berserkers Request", Plot_Tutorial_Beserker_Deceit.class);
		plotOptions.put("Test: Speed Test", Plot_SpeedTest.class);
		
		setLayout(new BorderLayout());
		add(new JLabel("The Four: Forgotten Age ~ Version 0.02", SwingConstants.CENTER), BorderLayout.NORTH);
		
		JPanel centerWrapperPanel = new JPanel();
		centerWrapperPanel.add(new JLabel("Scene:"));
		centerWrapperPanel.add(getPlotComboBox());
		centerWrapperPanel.add(new JLabel("     Climate:"));
		centerWrapperPanel.add(getClimateComboBox());
		add(centerWrapperPanel, BorderLayout.CENTER);
		
		JPanel southWrapperPanel = new JPanel();
		southWrapperPanel.add(getStartPlotButton());
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
	
	private JComboBox<String> getPlotComboBox() {
		String[] plotNames = plotOptions.keySet().toArray(new String[]{});
		selectedPlotString = plotNames[0];
		JComboBox<String> plotSelector = new JComboBox<String>(plotNames);
		plotSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)e.getSource();
				selectedPlotString = (String) cb.getSelectedItem();
			}
		});
		return plotSelector;
	}
	
	private JButton getStartPlotButton() {
		Action startPlot = new AbstractAction("Start Scene"){
			private static final long serialVersionUID = -8836958976794130145L;
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Class<? extends Plot> plotClass = plotOptions.get(selectedPlotString);
					Plot selectedPlot = (Plot) plotClass.newInstance();
					selectedPlot.start();
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
