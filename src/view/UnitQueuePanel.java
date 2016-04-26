package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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
import model.ITargetable;
import model.ReadiedAction;
import model.Unit;
import controller.ActionQueue;
import controller.IPlayerListener;

public class UnitQueuePanel extends JPanel implements MouseListener, IPlayerListener {

	private static final long serialVersionUID = -6109804051676468346L;
	private static final int HEIGHT = 27;
	private static final double ICON_SCALE = 0.85;
	private static final Font ABILITY_FONT = new Font("Monospaced", Font.PLAIN, 14);

	private static List<ReadiedAction> unitActions = new ArrayList<ReadiedAction>();
	
	public UnitQueuePanel(ActionQueue battleQueue) {
		addMouseListener(this);
		battleQueue.addPlayerListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform transform = AffineTransform.getScaleInstance(ICON_SCALE, ICON_SCALE);
		int count = 0;
		for (ReadiedAction action : unitActions) {
			int baseHeight = count * HEIGHT;
			g2.drawImage(action.getSource().getMini(), transform, null);
			transform.translate(150 / ICON_SCALE, 0);
			ITargetable target = action.getTarget();
			if (target instanceof Unit) {
				g2.drawImage(((Unit)target).getMini(), transform, null);
			}
			
			g2.setColor(Color.BLACK);
			String text = action.getAbility().getName();
		    FontMetrics metrics = g2.getFontMetrics(ABILITY_FONT);
		    int x = (100 - metrics.stringWidth(text)) / 2;
		    g2.setFont(ABILITY_FONT);
		    g2.drawString(text, 55 + x, baseHeight + 18);
			
			
			g2.setColor(Color.BLACK);
			g2.drawRect(0, baseHeight, 210, HEIGHT);
			
			transform.translate(-150 / ICON_SCALE, HEIGHT / ICON_SCALE);
			count++;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void onChangedActivePlayer(Unit unit) {}
	
	@Override
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions) {
		unitActions.clear();
		while(actions.hasNext()) {
			unitActions.add(actions.next());
		}
	}

	@Override
	public void onPlayerUsedAbility(ReadiedAction action) {}

	@Override
	public void onChangedPlayerTeam(Set<GameObject> playerObjects) {}
}
