package view;

import java.awt.Component;

import javax.swing.JLayeredPane;

public class GameLayeredPane extends JLayeredPane {

	private static final long serialVersionUID = -7110358661003446126L;
	private static final int MENU_HEIGHT = 120;
	private static final int PARTY_PANEL_WIDTH = 235;

	public GameLayeredPane() {
	}
	
	@Override
    public void doLayout() {
        synchronized(getTreeLock()) {
            int w = getWidth();
            int h = getHeight();
            
            for(Component c : getComponents()) {
            	if (c instanceof GraphicsPanel) {
            		c.setBounds(0, 0, w, h - MENU_HEIGHT);
            	} else if (c instanceof PartyPanel) {
            		c.setBounds(0, h - MENU_HEIGHT, PARTY_PANEL_WIDTH, MENU_HEIGHT);
            	} else if (c instanceof MenuPanel) {
            		c.setBounds(PARTY_PANEL_WIDTH, h - MENU_HEIGHT, w/4, MENU_HEIGHT);
            	} else if (c instanceof AbilityPanel) {
            		c.setBounds(w/2, h - MENU_HEIGHT, w/4, MENU_HEIGHT);
            	} else if (c instanceof DialogPanel) {
            		c.setBounds(w/4, h/20, w/2, h/3);
            	} else if (c instanceof TitleScreenPanel) {
            		c.setBounds(0, 0, w, h);
            	}
            }
        }
    }
}
