package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.Deliver;

public class DeliverException extends CleartoolException {
	
	public enum Type {
		REQUIRES_REBASE,
		MERGE_ERROR,
		INTERPROJECT_DELIVER_DENIED,
		DELIVER_IN_PROGRESS,
		UNKNOWN
	}
	
	private Type type;
	private Deliver deliver;
	
	public DeliverException( Deliver deliver, Type type, Exception e ) {
		super( e );
		this.deliver = deliver;
	}

	public Type getType() {
		return type;
	}

	public Deliver getDeliver() {
		return deliver;
	}
}