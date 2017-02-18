package controller;

import model.World;
import view.GameFrame;

public class Game {
	private World world;
	private ActionPlayer battleQueue;
	
	public Game() {
		world = new World();
		battleQueue = new ActionPlayer(world);
		new GameFrame(world, battleQueue);
	}
}
