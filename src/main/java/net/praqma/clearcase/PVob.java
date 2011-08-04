package net.praqma.clearcase;

public class PVob extends Cool {

	private String name;
	private String localPath;
	private String globalPath;

	public PVob(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

}
