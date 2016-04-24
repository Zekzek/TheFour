package controller;

import model.GameObject;

public interface IGameObjectListener {
	public void onObjectDeath(GameObject object);
}
