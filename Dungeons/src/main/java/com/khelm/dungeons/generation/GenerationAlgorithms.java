package com.khelm.dungeons.generation;

/**
 * Enum of the available generation algorithms
 * 
 * @author IraqiMilitant
 *
 */
public enum GenerationAlgorithms {
	BASIC,
	SPACIOUS,
	CAVE;
	
	GenerationAlgorithms(){
		
	}
	
	/**
	 * gets a instance of a generation algorithm based on the enum value
	 * 
	 * @return
	 */
	public GenerationAlgorithm getAlgorithm(){
		switch (this){
		case BASIC:
			return new BasicGenerationAlgorithm();
		case CAVE:
			return new CaveGenerationAlgorithm();
		case SPACIOUS:
			return new SpaciousCaveGenerationAlgorithm();
		}
		return null;
	}

}
