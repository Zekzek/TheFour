package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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
import controller.plot.Plot;

public class UnitQueuePanel extends JPanel implements MouseListener, IPlayerListener {

	private static final long serialVersionUID = -6109804051676468346L;
	private static final int HEIGHT = 27;
	private static final double ICON_SCALE = 0.85;
	private static final Font ABILITY_FONT = new Font("Monospaced", Font.PLAIN, 14);
	private static final SpriteSheet iconSheet 
		= SpriteSheet.getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/icons.png"));

	private static List<ReadiedAction> unitActions = new ArrayList<ReadiedAction>();
	private ActionQueue battleQueue;
	
	public UnitQueuePanel(ActionQueue battleQueue) {
		this.battleQueue = battleQueue;
		addMouseListener(this);
		battleQueue.addPlayerListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform savedTransform = g2.getTransform();
		
		AffineTransform transform = AffineTransform.getScaleInstance(ICON_SCALE, ICON_SCALE);
		FontMetrics metrics = g2.getFontMetrics(ABILITY_FONT);

		String text;
		Image playPauseImage;
		if (battleQueue.isPaused()) {
			text = "Paused";
			playPauseImage = iconSheet.getSprite(SpriteSheet.ICON_ID.PLAY);
		} else {
			text = "Playing";
			playPauseImage = iconSheet.getSprite(SpriteSheet.ICON_ID.PAUSE);
		}
	    int x = (getWidth() - metrics.stringWidth(text)) / 2;
	    g2.drawString(text, x, 18);
	    g2.drawImage(playPauseImage, 1, 1, null);
		
		for (ReadiedAction action : unitActions) {
			g2.translate(0, HEIGHT);
			
			//Source and Target images
			g2.drawImage(action.getSource().getMini(), transform, null);
			ITargetable target = action.getTarget();
			if (target instanceof Unit) {
				BufferedImage targetImage = ((Unit)target).getMini();
				transform.translate((getWidth() - targetImage.getWidth()*ICON_SCALE) / ICON_SCALE, 0);
				g2.drawImage(targetImage, transform, null);
				transform.translate(-(getWidth() - targetImage.getWidth()*ICON_SCALE) / ICON_SCALE, 0);
			}
			
			//Draw close button
			g2.drawImage(iconSheet.getSprite(SpriteSheet.ICON_ID.CLOSE), 1, 1, null);
				
			//Ability name
			g2.setColor(Color.BLACK);
			text = action.getAbility().getName();
		    x = (getWidth() - metrics.stringWidth(text)) / 2;
		    g2.setFont(ABILITY_FONT);
		    g2.drawString(text, x, 18);
			
		    //Border
			g2.setColor(Color.BLACK);
			g2.drawRect(0, 0, getWidth(), HEIGHT);
		}
		g2.setTransform(savedTransform);
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		if (x > SpriteSheet.ICON_WIDTH)
			return;
		
		int y = e.getY();
		int clickedIndex = y / HEIGHT;
		if (y - clickedIndex*HEIGHT > SpriteSheet.ICON_HEIGHT)
			return;
		
		if (clickedIndex == 0) {
			if (battleQueue.isPaused())
				battleQueue.setPause(false);
			else
				battleQueue.setPause(true);
		}
		else {
			int actionIndex = clickedIndex - 1;
			if (actionIndex < unitActions.size()) {
				ReadiedAction action = unitActions.get(actionIndex);
				battleQueue.dequeueAction(action);
			}
		}
	}

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
