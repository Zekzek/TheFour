package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.Ability;
import model.GridPosition;
import model.GroundTarget;
import model.ITargetable;
import model.ReadiedAction;
import model.Unit;
import model.World;
import controller.BattleQueue;
import controller.IPlayerListener;

public class AbilitySelectionPanel extends JPanel implements IGridClickedListener, IPlayerListener {
	private static final long serialVersionUID = -5640915321281094627L;
	
	private JList<Ability> abilityList;
	private AbilityDetailPanel abilityDetailPanel;

	private ArrayList<ITargetable> validTargets;
	private Unit activeUnit;
	private Ability activeAbility;
	private World world;
	private BattleQueue battleQueue;
	
	public AbilitySelectionPanel(AbilityDetailPanel abilityDetailPanel, World world, BattleQueue battleQueue) {
		this.abilityDetailPanel = abilityDetailPanel;
		this.world = world;
		this.battleQueue = battleQueue;
		BattleQueue.addPlayerListener(this);
		setLayout(new GridLayout(1,1));
		abilityList = makeMenuList();
		abilityList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (abilityList.getSelectedValue() != null) {
						activeAbility = abilityList.getSelectedValue();
						abilityDetailPanel.updateDisplay(activeAbility);
						abilityDetailPanel.setVisible(true);
						updateTargets();
					}
				}
			}
		});
		add(new JScrollPane(abilityList));
		
		validTargets = new ArrayList<ITargetable>();
		setVisible(false);
		GraphicsPanel.addGridClickedListener(this);
	}
	
	private <T> JList<T> makeMenuList() {
		DefaultListModel<T> listModel = new DefaultListModel<T>();
		JList<T> list = new JList<T>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(5);
		list.setBorder(new BevelBorder(BevelBorder.RAISED));
		list.setBackground(Color.LIGHT_GRAY);
		return list;
	}
	
	private void queueSelectedAbilityAtTarget(ITargetable target) {
		battleQueue.queueAction(activeAbility, activeUnit, target);
		abilityDetailPanel.setVisible(false);
		abilityList.clearSelection();
		battleQueue.finishPlanningAction(activeUnit);
		clearTargets();
	}
	
	private void updateAbilityMenuList(JList<Ability> list, Iterator<Ability> menuItems) {
		DefaultListModel<Ability> listModel = (DefaultListModel<Ability>) list.getModel();
		listModel.clear();
		while(menuItems.hasNext()) {
			listModel.addElement(menuItems.next());
		}
		list.setPreferredSize(new Dimension(getSize().width/2, getSize().height));
	}
	
	private void clearTargets() {
		for (ITargetable oldTarget : validTargets) {
			oldTarget.setInTargetList(false);
		}
		validTargets.clear();
	}
	
	private void updateTargets() {
		clearTargets();
		validTargets = world.getTargets(activeUnit, activeAbility);
		for (ITargetable target : validTargets) {
			target.setInTargetList(true);
			if (target instanceof GroundTarget) {
				GraphicsPanel.addGroundTarget((GroundTarget)target);
			}
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		abilityList.setEnabled(enabled);
	}

	@Override
	public void reportGridClicked(GridPosition pos) {
		if (activeAbility != null && activeUnit != null) {
			ArrayList<ITargetable> targets = world.getTargets(activeUnit, activeAbility);
			for (ITargetable target : targets) {
				if (target.getPos().equals(pos)) {
					queueSelectedAbilityAtTarget(target);
					break;
				}
			}
		}
	}

	@Override
	public void onChangedActivePlayer(Unit unit) {
		activeUnit = unit;
		if (activeUnit == null || activeUnit.getTeam() != Unit.TEAM.PLAYER) {
			setVisible(false);
		} else {
			setVisible(true);
			updateAbilityMenuList(abilityList, activeUnit.getKnownActions());
		}
		revalidate();
		repaint();
	}
	
	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {}

	@Override
	public void onPlayerUsedAbility(ReadiedAction action) {}
}