package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import model.ReadiedAction;
import model.StatusEffect;
import model.Unit;
import model.Unit.TEAM;
import controller.BattleQueue;
import controller.IBattleListener;
import controller.IPlayerListener;

public class PartyPanel extends JPanel implements IBattleListener, IPlayerListener, MouseListener{
	private static final long serialVersionUID = -8724521387521550507L;
	private static final int HEIGHT = 27;
	private static final double ICON_SCALE = 0.85;
	private static final Color SELECTED_UNIT_COLOR = new Color(100, 100, 0);
	private static final Color SELECTION_ARROW_COLOR = new Color(255, 255, 0);
	
	private List<Unit> units = new ArrayList<Unit>();
	private Unit activePlayer;
	
	public PartyPanel() {
		addMouseListener(this);
		BattleQueue.addBattleListener(this);
		BattleQueue.addPlayerListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform transform = AffineTransform.getScaleInstance(ICON_SCALE, ICON_SCALE);
		int count = 0;
		for (Unit unit : units) {
			int baseHeight = count * HEIGHT;
			if (unit.equals(activePlayer)) {
				g2.setColor(SELECTION_ARROW_COLOR);
				g2.fillPolygon(new int[]{212, 232, 232}, 
						new int[] {baseHeight + 15, baseHeight + 5, baseHeight + 25}, 3);
				g2.setColor(Color.BLACK);
				g2.drawPolygon(new int[]{212, 232, 232}, 
						new int[] {baseHeight + 15, baseHeight + 5, baseHeight + 25}, 3);
				g2.setColor(SELECTED_UNIT_COLOR);
			} else {
				g2.setColor(Color.DARK_GRAY);
			}
			g2.fillRect(0, baseHeight, 210, HEIGHT);
			g2.drawImage(unit.getMini(), transform, null);
			g2.setColor(Color.WHITE);
			g2.drawString(unit.getName(), 55, baseHeight + 15);
			g2.setColor(Color.GREEN);
			g2.fillRect(55, baseHeight + HEIGHT - 5, 152 * unit.getHp() / unit.getMaxHp(), 4);
			g2.setColor(Color.BLACK);
			g2.drawRect(55, baseHeight + HEIGHT - 5, 152, 4);
			int effectCount = 0;
			Iterator<StatusEffect> statusEffects = unit.getStatusEffects();
			while (statusEffects.hasNext()) {
				g2.drawImage(statusEffects.next().getIcon(), 195 - effectCount * 17, baseHeight + 3, null);
				effectCount++;
			}
			g2.setColor(Color.BLACK);
			g2.drawRect(0, baseHeight, 210, HEIGHT);
			
			transform.translate(0, HEIGHT / ICON_SCALE);
			count++;
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		int clickedIndex = e.getY() / HEIGHT;
		Unit clickedUnit = units.get(clickedIndex);
		BattleQueue.setActivePlayer(clickedUnit);
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void onUnitAdded(Unit unit) {
		if (unit.getTeam() == TEAM.PLAYER && !units.contains(unit)) {
			units.add(unit);
		}
	}

	@Override
	public void onUnitRemoved(Unit unit) {
		units.remove(unit);
	}

	@Override
	public void onUnitDefeated(Unit unit) {}

	@Override
	public void onTeamDefeated(TEAM team) {}

	@Override
	public void onChangedActivePlayer(Unit unit) {
		activePlayer = unit;
	}
	
	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {}

	@Override
	public void onPlayerUsedAbility(ReadiedAction action) {}
}
