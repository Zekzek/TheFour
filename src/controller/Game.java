package controller;

import model.World;
import view.GameFrame;

public class Game {
	private World world;
	private BattleQueue battleQueue;
	
	public Game() {
		world = new World();
		battleQueue = new BattleQueue(world);
		new GameFrame(world, battleQueue);
	}
}
