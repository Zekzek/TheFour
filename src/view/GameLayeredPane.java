package view;

import java.awt.Component;

import javax.swing.JLayeredPane;

public class GameLayeredPane extends JLayeredPane {

	private static final long serialVersionUID = -7110358661003446126L;

	public GameLayeredPane() {
	}
	
	@Override
    public void doLayout() {
        synchronized(getTreeLock()) {
            int w = getWidth();
            int h = getHeight();
            
            for(Component c : getComponents()) {
            	if (c instanceof GraphicsPanel) {
            		c.setBounds(0, 0, w, h);
            	} else if (c instanceof AbilityPanel) {
            		c.setBounds(w/3, h*7/8, w/3, h/8);
            	} else if (c instanceof MenuPanel) {
            		c.setBounds(0, h*5/6, w/3, h/6);
            	} else if (c instanceof DialogPanel) {
            		c.setBounds(w/4, h/20, w/2, h/3);
            	} else if (c instanceof TitleScreenPanel) {
            		c.setBounds(0, 0, w, h);
            	}
            }
        }
    }
}
