package view;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import controller.BattleQueue;
import model.World;

public class GameFrame extends JFrame {
	
	private static final long serialVersionUID = 118154690873028536L;
	
	private static GameFrame me;
//	private World world;
	private GraphicsPanel graphicsPanel;
	private AbilityPanel abilityPanel;
	private MenuPanel menuPanel;
	private DialogPanel dialogPanel;
	private TitleScreenPanel titleScreenPanel;
	
	public GameFrame() {
//		this.world = world;
		setPreferredSize(new Dimension(800, 600));
		setBounds(0, 0, 800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JLayeredPane layeredPane = new GameLayeredPane();
		
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
		me.revalidate();
		me.repaint();
	}
	
	public static void updateMenu() {
		me.menuPanel.refreshMenu();
		me.menuPanel.revalidate();
		me.menuPanel.repaint();
	}
	
	public static boolean isMenuVisible() {
		return me.menuPanel.isVisible();
	}
	
	public static void updateDialog() {
		me.dialogPanel.revalidate();
		me.dialogPanel.repaint();
	}
	
	public static void disableMenu() {
		me.menuPanel.setEnabled(false);
		me.menuPanel.setVisible(false);
	}
	
	public static void enableMenu() {
		me.menuPanel.setEnabled(true);
		me.menuPanel.setVisible(true);
	}
	
	public static void returnToTitleScreen() {
		BattleQueue.endCombat();
		BattleQueue.clearBattleListeners();
		World.reset();
		me.titleScreenPanel.setVisible(true);
	}
}
