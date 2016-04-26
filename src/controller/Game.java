package controller;

import model.World;
import view.GameFrame;

public class Game {
	private World world;
	private ActionQueue battleQueue;
	
	public Game() {
		world = new World();
		battleQueue = new ActionQueue(world);
		new GameFrame(world, battleQueue);
	}
}
