package com.cardshifter.modapi.phase;

import com.cardshifter.modapi.base.Entity;

public class Phase {

	private final Entity owner;
	private final String name;

	public Phase(Entity owner, String name) {
		this.owner = owner;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Entity getOwner() {
		return owner;
	}
	
	@Override
	public String toString() {
		return "Phase " + name + " for " + owner;
	}
	
}
