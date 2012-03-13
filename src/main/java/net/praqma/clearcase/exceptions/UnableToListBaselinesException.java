package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;

public class UnableToListBaselinesException extends CleartoolException {

	private Stream s;
	private Component c;
	private PromotionLevel plevel;
	
	public UnableToListBaselinesException( Stream s, Component c, PromotionLevel plevel, Exception e ) {
		super( e );
		this.s = s;
		this.c = c;
		this.plevel = plevel;
	}

	public Stream getStream() {
		return s;
	}

	public Component getComponent() {
		return c;
	}

	public PromotionLevel getPromotionLevel() {
		return plevel;
	}

	
}