package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
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
import view.GraphicsPanel.AMBIENT_LIGHT;
import controller.ActionQueue;
import controller.plot.Plot;
import controller.plot.Plot_Beginnings;

public class TitleScreenPanel extends JPanel{
	private static final long serialVersionUID = -8163142113386384945L;

	private static TitleScreenPanel me;
	private static Map<String, Class<? extends Plot>> plotOptions;
	private static String selectedPlotString;
	private static Class[] plotParameters = new Class[]{ActionQueue.class, World.class};
	private ActionQueue battleQueue;
	private World world;
	
	public TitleScreenPanel(ActionQueue battleQueue, World world) {
		this.battleQueue = battleQueue;
		this.world = world;
		plotOptions = new HashMap<String, Class<? extends Plot>>();
		plotOptions.put("Beginnings", Plot_Beginnings.class);
//		plotOptions.put("Beginnings: Magic Case", Plot_Tutorial_Defend_Sorc_Item.class);
//		plotOptions.put("Beginnings: Arena", Plot_Tutorial_Arena.class);
//		plotOptions.put("Beginnings: Berserkers Request", Plot_Tutorial_Beserker_Deceit.class);
//		plotOptions.put("Test: Speed Test", Plot_SpeedTest.class);
		
		setLayout(new BorderLayout());
		add(new JLabel("The Four: Forgotten Age ~ Version 0.03", SwingConstants.CENTER), BorderLayout.NORTH);
		
		JPanel centerWrapperPanel = new JPanel();
		centerWrapperPanel.add(new JLabel("Scene:"));
		centerWrapperPanel.add(getPlotComboBox());
//		centerWrapperPanel.add(new JLabel("     Light Level:"));
//		centerWrapperPanel.add(getAmbientLightComboBox());
		
		add(centerWrapperPanel, BorderLayout.CENTER);
		
		JPanel southWrapperPanel = new JPanel();
		southWrapperPanel.add(getStartPlotButton());
		add(southWrapperPanel, BorderLayout.SOUTH);
		
		me = this;
	}
	
	private JComboBox<AMBIENT_LIGHT> getAmbientLightComboBox() {
		JComboBox<AMBIENT_LIGHT> lightSelector = new JComboBox<AMBIENT_LIGHT>(AMBIENT_LIGHT.values());
		lightSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<AMBIENT_LIGHT> cb = (JComboBox<AMBIENT_LIGHT>)e.getSource();
				AMBIENT_LIGHT light = (AMBIENT_LIGHT) cb.getSelectedItem();
				GraphicsPanel.setAmbientLight(light);
			}
		});
		return lightSelector;
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
				} catch (InstantiationException | IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				}
			}
		};
		JButton button = new JButton(startPlot);
		return button;
	}
}
