package com.fullwall.Citizens.Interfaces;

import com.fullwall.Citizens.Properties.PropertyManager;

public class NPCType {
	private final NPCFactory factory;
	private final String type;

	public NPCType(String type, Saveable saveable, NPCFactory factory) {
		this.factory = factory;
		this.type = type;
		PropertyManager.add(type, saveable);
	}

	public NPCFactory factory() {
		return factory;
	}

	public String getType() {
		return this.type;
	}
}
