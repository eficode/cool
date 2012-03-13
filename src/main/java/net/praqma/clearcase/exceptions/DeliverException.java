package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;

public class DeliverException extends CleartoolException {
	
	public enum Type {
		REQUIRES_REBASE,
		MERGE_ERROR,
		INTERPROJECT_DELIVER_DENIED,
		DELIVER_IN_PROGRESS,
		UNKNOWN
	}
	
	private Type type;
	private Baseline baseline;
	private Stream source;
	private Stream target;
	
	public DeliverException( Baseline b, Stream s, Stream t, Type type, Exception e ) {
		super( e );
		this.baseline = b;
		this.source = s;
		this.target = t;
	}

	public Type getType() {
		return type;
	}

	public Baseline getBaseline() {
		return baseline;
	}

	public Stream getSource() {
		return source;
	}

	public Stream getTarget() {
		return target;
	}

	
}