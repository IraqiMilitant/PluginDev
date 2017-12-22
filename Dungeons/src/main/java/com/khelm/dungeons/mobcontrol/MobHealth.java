package com.khelm.dungeons.mobcontrol;

public enum MobHealth {
	ZOMBIE (20),
	BLAZE (20),
	CAVE_SPIDER (12),
	CREEPER (20),
	GHAST (10),
	MAGMA_CUBE (16),
	SLIME (16),
	SILVERFISH (8),
	SKELETON (20),
	SPIDER (16),
	WITCH (26),
	WITHER_SKELETON (20),
	PIG_ZOMBIE (20);
	public int health;

	private MobHealth(int health){
		this.health=health;
	}
}
