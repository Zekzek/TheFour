package view;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import controller.BattleQueue;
import model.Unit;
import model.World;

public class GameFrame extends JFrame {
	
	private static final long serialVersionUID = 118154690873028536L;
	private static final Dimension INITIAL_DIMENSIONS = new Dimension(1024, 600);
	
	private static GameFrame me;
	private GraphicsPanel graphicsPanel;
	private AbilityPanel abilityPanel;
	private MenuPanel menuPanel;
	private DialogPanel dialogPanel;
	private TitleScreenPanel titleScreenPanel;
	private JLayeredPane layeredPane;
	
	public GameFrame() {
		setPreferredSize(INITIAL_DIMENSIONS);
		setBounds(0, 0, INITIAL_DIMENSIONS.width, INITIAL_DIMENSIONS.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        layeredPane = new GameLayeredPane();
		
        graphicsPanel = new GraphicsPanel();
		layeredPane.add(graphicsPanel, JLayeredPane.DEFAULT_LAYER);
		
		abilityPanel = new AbilityPanel();
		layeredPane.add(abilityPanel, JLayeredPane.PALETTE_LAYER);
		
		menuPanel = new MenuPanel(abilityPanel);
		layeredPane.add(menuPanel, JLayeredPane.PALETTE_LAYER);
		
		dialogPanel = new DialogPanel();
		layeredPane.add(dialogPanel, JLayeredPane.MODAL_LAYER);
		
		titleScreenPanel = new TitleScreenPanel();
		layeredPane.add(titleScreenPanel, JLayeredPane.POPUP_LAYER);
		
		getContentPane().add(layeredPane);
		
		setVisible(true);
		me = this;
		
		graphicsPanel.startPainting();
	}
	
	public static void updateAll() {
		if (me != null) {
			me.revalidate();
			me.repaint();
		}
	}
	
	public static void makeMenuFor(Unit unit) {
		if (me != null) {
			me.menuPanel.makeMenuFor(unit);
			me.menuPanel.revalidate();
			me.menuPanel.repaint();
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
	
	public static void returnToTitleScreen() {
		BattleQueue.endCombat();
		BattleQueue.clearBattleListeners();
		World.reset();
		if (me != null) {
			me.layeredPane.remove(me.menuPanel);
			me.menuPanel = new MenuPanel(me.abilityPanel);
			me.layeredPane.add(me.menuPanel, JLayeredPane.PALETTE_LAYER);
			me.titleScreenPanel.setVisible(true);
		}
	}

	public static void repaintAll() {
		if (me != null) {
			me.repaint();
		}
	}
}
