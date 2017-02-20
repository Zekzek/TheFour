package view;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import controller.ActionRunner;
import controller.Trigger;
import controller.Watcher;
import controller.plot.Plot;
import model.Dialog;
import model.World;

public class GameFrame extends JFrame implements IMenuListener{
	
	private static final long serialVersionUID = 118154690873028536L;
	private static final Dimension INITIAL_DIMENSIONS = new Dimension(1024, 600);
	
	private static GameFrame me;
	private JLayeredPane layeredPane;
	private GraphicsPanel graphicsPanel;
	private PartyPanel partyPanel;
	private AbilityDetailPanel abilityPanel;
	private AbilitySelectionPanel menuPanel;
	private UnitQueuePanel unitQueuePanel;
	private DialogPanel dialogPanel;
	private TitleScreenPanel titleScreenPanel;
	private World world;
	private ActionRunner battleQueue;
	
	public GameFrame(World world, ActionRunner battleQueue) {
		me = this;
		this.world = world;
		this.battleQueue = battleQueue;
		Plot.setGameFrame(this);
		Trigger.setGameFrame(this);
		setPreferredSize(INITIAL_DIMENSIONS);
		setBounds(0, 0, INITIAL_DIMENSIONS.width, INITIAL_DIMENSIONS.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        layeredPane = new GameLayeredPane();
		
        graphicsPanel = new GraphicsPanel(world, battleQueue);
		layeredPane.add(graphicsPanel, JLayeredPane.DEFAULT_LAYER);
		
		partyPanel = new PartyPanel(battleQueue);
		layeredPane.add(partyPanel, JLayeredPane.PALETTE_LAYER);
		
		abilityPanel = new AbilityDetailPanel();
		layeredPane.add(abilityPanel, JLayeredPane.PALETTE_LAYER);
		
		menuPanel = new AbilitySelectionPanel(abilityPanel, world, battleQueue);
		layeredPane.add(menuPanel, JLayeredPane.PALETTE_LAYER);
		
		unitQueuePanel = new UnitQueuePanel(battleQueue);
		layeredPane.add(unitQueuePanel, JLayeredPane.PALETTE_LAYER);
		
		dialogPanel = new DialogPanel(battleQueue);
		layeredPane.add(dialogPanel, JLayeredPane.MODAL_LAYER);
		
		titleScreenPanel = new TitleScreenPanel(battleQueue, world);
		layeredPane.add(titleScreenPanel, JLayeredPane.POPUP_LAYER);
		
		getContentPane().add(layeredPane);
		
		pack();
		setVisible(true);
		
		graphicsPanel.startPainting();
		SpriteSheet.preloadSpriteSheets();
		Watcher.registerMenuListener(this);
	}
	
	public static void updateAll() {
		if (me != null) {
			me.revalidate();
			me.repaint();
		}
	}
	
	public static boolean isMenuVisible() {
		return me == null ? false : me.menuPanel.isVisible();
	}
	
	public static void updateDialog() {
		if (me != null) {
			me.dialogPanel.revalidate();
			me.dialogPanel.repaint();
		}
	}
	
	public static void disableMenu() {
		if (me != null) {
			me.menuPanel.setEnabled(false);
			me.menuPanel.setVisible(false);
		}
	}
	
	public static void enableMenu() {
		if (me != null) {
			me.menuPanel.setEnabled(true);
			me.menuPanel.setVisible(true);
		}
	}
	
	public void returnToTitleScreen() {
		battleQueue.reset();
		layeredPane.remove(menuPanel);
		menuPanel = new AbilitySelectionPanel(abilityPanel, world, battleQueue);
		layeredPane.add(menuPanel, JLayeredPane.PALETTE_LAYER);
		titleScreenPanel.setVisible(true);
	}

	public static void repaintAll() {
		if (me != null) {
			me.repaint();
		}
	}

	public void changeScene(SceneTransition transition) {
		graphicsPanel.changeScene(transition);
	}

	public void showDialog(Dialog[] speech, Runnable actionOnConclusion) {
		dialogPanel.showDialog(speech, actionOnConclusion);
	}

	@Override
	public void onSceneComplete() {
		returnToTitleScreen();
	}
}
