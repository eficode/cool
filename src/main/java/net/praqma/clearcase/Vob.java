package net.praqma.clearcase;

public class Vob extends Cool {

	private String name;
	private String localPath;
	private String globalPath;

	public Vob(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

}
