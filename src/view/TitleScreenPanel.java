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

import model.World;
import controller.ActionPlayer;
import controller.plot.Plot;
import controller.plot.Plot_Beginnings;

public class TitleScreenPanel extends JPanel{
	private static final long serialVersionUID = -8163142113386384945L;

	private static TitleScreenPanel me;
	private static Map<String, Class<? extends Plot>> plotOptions;
	private static String selectedPlotString;
	private static Class<?>[] plotParameters = new Class[]{ActionPlayer.class, World.class};
	private ActionPlayer battleQueue;
	private World world;
	
	public TitleScreenPanel(ActionPlayer battleQueue, World world) {
		this.battleQueue = battleQueue;
		this.world = world;
		plotOptions = new HashMap<String, Class<? extends Plot>>();
		plotOptions.put("Beginnings", Plot_Beginnings.class);
		
		setLayout(new BorderLayout());
		add(new JLabel("The Four: Forgotten Age ~ Version 0.04", SwingConstants.CENTER), BorderLayout.NORTH);
		
		JPanel centerWrapperPanel = new JPanel();
		centerWrapperPanel.add(new JLabel("Scene:"));
		centerWrapperPanel.add(getPlotComboBox());
		
		add(centerWrapperPanel, BorderLayout.CENTER);
		
		JPanel southWrapperPanel = new JPanel();
		southWrapperPanel.add(getStartPlotButton());
		add(southWrapperPanel, BorderLayout.SOUTH);
		
		me = this;
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
					Plot selectedPlot = (Plot) plotClass.getDeclaredConstructor(plotParameters)
							.newInstance(battleQueue, world);
					selectedPlot.start();
					me.setVisible(false);
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
			}
		};
		JButton button = new JButton(startPlot);
		return button;
	}
}
