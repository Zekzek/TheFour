package model;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import view.SpriteSheet;
import view.SpriteSheet.CLIMATE;
import controller.plot.Plot;

public class Structure extends GameObject{

	private static final SpriteSheet STRUCTURE_SHEET = 
			SpriteSheet.getSpriteSheet(Plot.class.getResource("/resource/img/spriteSheet/objects.png"));
	public static enum ID {
		TREE(0,true), WALL(9,true), TREASURE(18,false), TARGET_DUMMY(19, false);
		private final int index; //used by spriteSheet to select an icon
		private final boolean climateSpecific;
		
		ID(int index, boolean climateSpecific) {
			this.index = index;
			this.climateSpecific = climateSpecific;
		}
		public int getIndex(SpriteSheet.CLIMATE climate) {
			return index + (climateSpecific?climate.ordinal():0);
		}
	};
	
	private final ID id;
	private CLIMATE climate;
	
	private Structure(ID id, String name, int hp) {
		super(name, hp);
		this.id = id;
		this.team = TEAM.NONCOMBATANT;
	}

	private Structure(Structure structure) {
		super(structure);
		this.climate = structure.climate;
		this.id = structure.id;
	}
	
	public static Structure get(ID id, CLIMATE climate) {
		return StructureFactory.getStructure(id, climate);
	}

	@Override
	public BufferedImage getSprite() {
		return STRUCTURE_SHEET.getSprite(id, climate);
	}
	
	public void setClimate(CLIMATE climate) {
		this.climate = climate;
	}
	
	private static class StructureFactory {
		private static Map<ID, Structure> structures = new HashMap<ID, Structure>();
		private static boolean structuresInitialized = false;

		private static void initStructures() {
			structuresInitialized = true;
			structures.put(ID.TREE, new Structure(ID.TREE, "Tree", 200));
			structures.put(ID.WALL, new Structure(ID.WALL, "Wall", 200));
			structures.put(ID.TREASURE, new Structure(ID.TREASURE, "Treasure", 200));
			structures.put(ID.TARGET_DUMMY, new Structure(ID.TARGET_DUMMY, "Target Dummy", 2000));
		}

		public static Structure getStructure(ID id, CLIMATE climate) {
			if (!structuresInitialized) {
				initStructures();
			}
			Structure structure = structures.get(id);
			if (structure == null) {
				throw new UnsupportedOperationException("Not implemented yet - Structure::" + id.name());
			}
			structure = new Structure(structure);
			structure.setClimate(climate);
			return structure;
		}
	}
}
