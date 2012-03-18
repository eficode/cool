package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Stream;


public class NoSingleTopComponentException extends ClearCaseException {

	private Stream stream;
	
	public NoSingleTopComponentException( Stream s ) {
		super();
		this.stream = s;
	}
	
	public Stream getStream() {
		return stream;
	}

	
}