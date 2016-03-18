package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.Ability;
import model.Unit;
import model.World;
import controller.BattleQueue;

public class MenuPanel extends JPanel {
	private static final long serialVersionUID = -5640915321281094627L;
	
	private JLabel nameLabel;
	private JList<Ability> abilityList;
	private JList<Unit> targetList;
	
	private Unit activeUnit;
	private Ability activeAbility;
	
	public MenuPanel(AbilityPanel abilityPanel) {
		this.setLayout(new BorderLayout());
		nameLabel = new JLabel("", SwingConstants.CENTER);
		nameLabel.setOpaque(true);
		nameLabel.setBackground(Color.BLACK);
		nameLabel.setForeground(Color.WHITE);
		add(nameLabel, BorderLayout.NORTH);
	
		abilityList = makeMenuList();
		abilityList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (abilityList.getSelectedValue() != null) {
						activeAbility = abilityList.getSelectedValue();
						abilityPanel.updateDisplay(activeAbility);
						abilityPanel.setVisible(true);
						targetList.setVisible(true);
						updateMenuList(targetList, World.getTargets(activeUnit, activeAbility, GraphicsPanel.getScreenRectangle()).iterator());
					}
				}
			}
		});
		add(abilityList, BorderLayout.WEST);
		
		targetList = makeMenuList();
		targetList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (targetList.getSelectedValue() != null) {
						Unit selectedTarget = targetList.getSelectedValue();
						BattleQueue.queueAction(activeAbility, activeUnit, selectedTarget);
						BattleQueue.resumeBattle();
						targetList.setVisible(false);
						abilityPanel.setVisible(false);
						abilityList.clearSelection();
						refreshMenu();
					}
				}
			}
		});
		setVisible(false);
		add(targetList, BorderLayout.EAST);
	}
	
	public void refreshMenu() {
		activeUnit = BattleQueue.getMostReadyCombatant();
		if (activeUnit == null) {
			setVisible(false);
		} else if (activeUnit.getTeam() == Unit.TEAM.PLAYER) {
			setVisible(true);
			activeUnit.convertNameLabel(nameLabel);
			updateMenuList(abilityList, activeUnit.getKnownActions());
		} else {
			setVisible(false);
			activeUnit.aiQueueAction();
			GameFrame.updateMenu();
		}
	}
	
	private <T> JList<T> makeMenuList() {
		DefaultListModel<T> listModel = new DefaultListModel<T>();
		JList<T> list = new JList<T>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(4);
		list.setBorder(new BevelBorder(BevelBorder.RAISED));
		list.setBackground(Color.LIGHT_GRAY);
		list.setPreferredSize(new Dimension(getSize().width/2, getSize().height));
		return list;
	}
	
	private <T> void updateMenuList(JList<T> list, Iterator<T> menuItems) {
		DefaultListModel<T> listModel = (DefaultListModel<T>) list.getModel();
		listModel.clear();
		while(menuItems.hasNext()) {
			listModel.addElement(menuItems.next());
		}
		list.setPreferredSize(new Dimension(getSize().width/2, getSize().height));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		abilityList.setEnabled(enabled);
		targetList.setEnabled(enabled);
	}
}