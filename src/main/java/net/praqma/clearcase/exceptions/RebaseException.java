package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.Rebase;

public class RebaseException extends CleartoolException {

	private Rebase rebase;
	
	public RebaseException( Rebase rebase, Exception e ) {
		super( e );
		this.rebase = rebase;
	}

	public Rebase getRebase() {
		return rebase;
	}

	
}