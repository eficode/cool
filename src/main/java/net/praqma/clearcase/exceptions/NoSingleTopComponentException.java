package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Stream;


public class NoSingleTopComponentException extends CleartoolException {

	private Stream stream;
	
	public NoSingleTopComponentException( Stream s ) {
		super();
		this.stream = s;
	}
	
	public Stream getStream() {
		return stream;
	}

	
}