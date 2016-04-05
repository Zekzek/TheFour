package controller;

public class Starter {
	public static void main(String[] args) {
		new Game();
	}
}

//		Issues
//	Exception in thread "Thread-5" java.lang.ArrayIndexOutOfBoundsException: -1965
//	at controller.MapBuilder.getTerrainType(MapBuilder.java:58)
//	at model.World.isTraversable(World.java:175)
//	at model.World.getTraversableNeighbors(World.java:159)
//	at controller.BattleQueue.getPathToUseAction(BattleQueue.java:383)
//	at controller.BattleQueue.getFirstStepToUseAction(BattleQueue.java:356)
//lifebar gets blocked
//	player lifebars always visible in menus?
//	show target lifebars in menu
//	color/flash hp bar as life gets low
//murderous intent looks like a sad face
//units go behind menus = bad

//		TODO: Feature improvements
//	Terrain object spritesheet
//	object spritesheet
//	object map (ensure edges are closed)
//	Labels for ability & target?
//	Win animation, subset of cast animation
//	Ability Categories
//	Decorate dialog box
//	Decorate ability selection
// 	climate-type overlay map?
// 	expanded world template
// 	Keyboard interaction
// 	Glowing active player
// 	damage number on screen
// 	Triggers on enter a region
// 	structure template
// 	Death screen?
// 	Win screen?
// 	Elevation map

//		LATER
//	Combos
//	Momentum
//	Party dynamics fight
//	Stat interaction display
//	Equipment
//	Basr stats
//	Learn new abilities
//	master abilities (through use)
//	resources (mp, tp)
//	flanking
//	stealth
//	random encounters
//	music and sounds

//		LANDSCAPE/TERRAIN
// Plains, Jungle, Tundra, Forest, Desert, Rocky, Volcanic, Castle, Underground
// Road, Path, thick ground cover, light ground cover, shallow water, deep water
// Big plant, little plant, tree, bush, wall, door, fence, chest(open?), spring, 
// fountain, well, lawn ornament, bucket, bed, bookshelf, chair, fire, stump, 
// weapon rack, combat dummy, meat, carcass, oven, table, 

//		ATTRIBUTION
//http://gaurav.munjal.us/Universal-LPC-Spritesheet-Character-Generator/
//https://icons8.com