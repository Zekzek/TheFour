package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.Ability;
import model.ITargetable;
import model.Unit;
import model.World;
import controller.BattleQueue;

public class MenuPanel extends JPanel {
	private static final long serialVersionUID = -5640915321281094627L;
	
	private final JLabel nameLabel = new JLabel();
	private JList<Ability> abilityList;
	private JList<ITargetable> targetList;
	
	private Unit activeUnit;
	private Ability activeAbility;
	
	public MenuPanel(AbilityPanel abilityPanel) {
		this.setLayout(new BorderLayout());
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
						updateTargetMenuList(targetList, World.getTargets(activeUnit, activeAbility, GraphicsPanel.getScreenRectangle()).iterator());
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
						ITargetable selectedTarget = targetList.getSelectedValue();
						BattleQueue.queueAction(activeAbility, activeUnit, selectedTarget);
						targetList.setVisible(false);
						abilityPanel.setVisible(false);
						abilityList.clearSelection();
						BattleQueue.finishPlanningAction(activeUnit);
						updateTargetMenuList(targetList, new ArrayList<ITargetable>().iterator());
					}
				}
			}
		});
		setVisible(false);
		add(targetList, BorderLayout.EAST);
	}
	
	public void makeMenuFor(Unit unit) {
		activeUnit = unit;
		if (activeUnit == null || activeUnit.getTeam() != Unit.TEAM.PLAYER) {
			setVisible(false);
		} else {
			setVisible(true);
			activeUnit.convertNameLabel(nameLabel);
			updateAbilityMenuList(abilityList, activeUnit.getKnownActions());
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
	
	private void updateTargetMenuList(JList<ITargetable> list, Iterator<ITargetable> menuItems) {
		DefaultListModel<ITargetable> listModel = (DefaultListModel<ITargetable>) list.getModel();
		Enumeration<ITargetable> oldTargets = listModel.elements();
		while (oldTargets.hasMoreElements()) {
			ITargetable oldTarget = oldTargets.nextElement();
			oldTarget.setInTargetList(false);
			oldTarget.setSelectedTarget(false);
		}
		listModel.clear();
		while(menuItems.hasNext()) {
			ITargetable target = menuItems.next();
			listModel.addElement(target);
			target.setInTargetList(true);
//			target.setSelectedTarget(true);
		}
		list.setPreferredSize(new Dimension(getSize().width/2, getSize().height));
	}
	
	private void updateAbilityMenuList(JList<Ability> list, Iterator<Ability> menuItems) {
		DefaultListModel<Ability> listModel = (DefaultListModel<Ability>) list.getModel();
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