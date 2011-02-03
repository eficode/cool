package net.praqma.clearcase.ucm;

import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
public class UCMException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5325867242379727760L;
	public UCMType type = UCMType.DEFAULT;
	private Logger logger = Logger.getLogger();
	
	public enum UCMType
	{
		DEFAULT,
		ENTITY_ERROR,
		ENTITY_NAME_ERROR,
		LOAD_FAILED,
		TAG_CREATION_FAILED,
		UNKNOWN_HLINK_TYPE,
		VIEW_ERROR
	}
	
	public UCMException()
	{
		super();
		
		logger.warning( "Unnamed UCMException thrown" );
	}
	
	public UCMException( String s )
	{
		super( s );
		
		logger.warning( s );
	}
	
	public UCMException( String s, UCMType type )
	{
		super( s );
		
		logger.warning( s );
		
		this.type = type;
	}

}