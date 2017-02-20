package controller;

import model.World;
import view.GameFrame;

public class Game {
	private World world;
	private ActionRunner battleQueue;
	
	public Game() {
		world = new World();
		battleQueue = new ActionRunner(world);
		new GameFrame(world, battleQueue);
	}
}
