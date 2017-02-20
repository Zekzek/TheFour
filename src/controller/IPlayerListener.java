package controller;

import java.util.List;
import java.util.Set;

import model.GameObject;
import model.ReadiedAction;
import model.Unit;

public interface IPlayerListener {
	public void onChangedActivePlayer(Unit unit);
	public void onChangedMostReadyPlayer(Unit unit);
	public void onActivePlayerAbilityQueueChanged(List<ReadiedAction> actions);
	public void onPlayerUsedAbility(ReadiedAction action);
	public void onChangedPlayerTeam(Set<GameObject> playerObjects);
}
