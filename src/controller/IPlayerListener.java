package controller;

import java.util.Iterator;
import java.util.Set;

import model.GameObject;
import model.ReadiedAction;
import model.Unit;

public interface IPlayerListener {
	public void onChangedActivePlayer(Unit unit);
	public void onChangedMostReadyPlayer(Unit unit);
	public void onActivePlayerAbilityQueueChanged(Iterator<ReadiedAction> actions);
	public void onPlayerUsedAbility(ReadiedAction action);
	public void onChangedPlayerTeam(Set<GameObject> playerObjects);
}
