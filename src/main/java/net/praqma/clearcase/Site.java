package net.praqma.clearcase;

import java.util.List;

import net.praqma.clearcase.exceptions.UCMException;

public class Site extends Cool {

	private String name;

	public Site( String name ) {
		this.name = name;
	}

	public List<Vob> getVobs( Region region ) throws UCMException {
		return context.getVobs( region );
	}
}
