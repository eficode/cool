package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Stream;

public class CancelDeliverException extends ClearCaseException {

	private Stream stream;
	
	public CancelDeliverException( Stream s, Exception e ) {
		super( e );
		this.stream = s;
	}

    public CancelDeliverException( Stream s, String e ) {
        super( e );
        this.stream = s;
    }


    public Stream getStream() {
		return stream;
	}

	
}