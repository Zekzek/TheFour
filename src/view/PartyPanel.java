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
import java.util.Set;

import javax.swing.JPanel;

import model.GameObject;
import model.ReadiedAction;
import model.StatusEffect;
import model.GameObject.TEAM;
import model.Unit;
import controller.ActionPlayer;
import controller.IBattleListener;
import controller.IPlayerListener;

public class PartyPanel extends JPanel implements IBattleListener, IPlayerListener, MouseListener{
	private static final long serialVersionUID = -8724521387521550507L;
	private static final int HEIGHT = 27;
	private static final double ICON_SCALE = 0.85;
	private static final Color SELECTED_UNIT_COLOR = new Color(100, 100, 0);
	private static final Color SELECTION_ARROW_COLOR = new Color(255, 255, 0);
	private static final Color MOST_READY_ARROW_COLOR = new Color(100, 100, 100);
	private static final int[] SELECTION_ARROW_X_POSITION = new int[]{217, 232, 232};
	private static final int[] MOST_READY_ARROW_X_POSITION = new int[]{212, 227, 227};
	
	private List<Unit> playerUnits = new ArrayList<Unit>();
	private Unit activePlayer;
	private Unit mostReadyPlayer;
	private ActionPlayer battleQueue;
	
	public PartyPanel(ActionPlayer battleQueue) {
		this.battleQueue = battleQueue;
		addMouseListener(this);
		battleQueue.addBattleListener(this);
		battleQueue.addPlayerListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform transform = AffineTransform.getScaleInstance(ICON_SCALE, ICON_SCALE);
		int count = 0;
		for (Unit unit : playerUnits) {
			int baseHeight = count * HEIGHT;
			if (unit.equals(mostReadyPlayer)) {
				g2.setColor(MOST_READY_ARROW_COLOR);
				int[] arrowYPosition = new int[] {baseHeight + 15, baseHeight + 5, baseHeight + 25};
				g2.fillPolygon(MOST_READY_ARROW_X_POSITION, arrowYPosition, 3);
				g2.setColor(Color.BLACK);
				g2.drawPolygon(MOST_READY_ARROW_X_POSITION, arrowYPosition, 3);
			}
			if (unit.equals(activePlayer)) {
				g2.setColor(SELECTION_ARROW_COLOR);
				int[] arrowYPosition = new int[] {baseHeight + 15, baseHeight + 5, baseHeight + 25};
				g2.fillPolygon(SELECTION_ARROW_X_POSITION, arrowYPosition, 3);
				g2.setColor(Color.BLACK);
				g2.drawPolygon(SELECTION_ARROW_X_POSITION, arrowYPosition, 3);
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
		Unit clickedUnit = playerUnits.get(clickedIndex);
		battleQueue.setActivePlayer(clickedUnit);
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
		if (unit.getTeam() == TEAM.PLAYER && !playerUnits.contains(unit)) {
			playerUnits.add(unit);
		}
	}

	@Override
	public void onUnitRemoved(Unit unit) {
		playerUnits.remove(unit);
	}

	@Override
	public void onUnitDefeated(Unit unit) {}

	@Override
	public void onTeamDefeated(TEAM team) {}

	@Override
	public void onUnitChangedTeam(Unit unit) {
		if (unit.getTeam() == TEAM.PLAYER && !playerUnits.contains(unit)) {
			playerUnits.add(unit);
		} else if (unit.getTeam() != TEAM.PLAYER && playerUnits.contains(unit)) {
			playerUnits.remove(unit);
		}
	}
	
	@Override
	public void onChangedActivePlayer(Unit unit) {
		activePlayer = unit;
	}
	
	@Override
	public void onChangedMostReadyPlayer(Unit unit) {
		mostReadyPlayer = unit;
	}
	
	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {}

	@Override
	public void onPlayerUsedAbility(ReadiedAction action) {}

	@Override
	public void onChangedPlayerTeam(Set<GameObject> playerObjects) {
		for (GameObject playerObject : playerObjects) {
			if (playerObject instanceof Unit && !playerUnits.contains(playerObject)) {
				playerUnits.add((Unit) playerObject);
			}
		}
		Iterator<Unit> playerUnitIterator = playerUnits.iterator();
		while (playerUnitIterator.hasNext()) {
			Unit playerUnit = playerUnitIterator.next();
			if (!playerObjects.contains(playerUnit)) {
				playerUnitIterator.remove();
			}
		}
	}
}
