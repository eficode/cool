package net.praqma.clearcase.exceptions;

public class EntityNotLoadedException extends RuntimeException {

	private String name;
	
	public EntityNotLoadedException( String name, String m ) {
		super( m );
		
		this.name = name;
	}
	
	public EntityNotLoadedException( String name, String m, Exception e ) {
		super( m, e );
		
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
