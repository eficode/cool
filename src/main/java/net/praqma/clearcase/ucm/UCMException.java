package net.praqma.clearcase.ucm;

//public class UCMException extends RuntimeException
public class UCMException extends Exception
{
	public UCMType type = UCMType.DEFAULT;
	
	public enum UCMType
	{
		DEFAULT,
		LOAD_FAILED,
		TAG_CREATION_FAILED,
		UNKNOWN_TAG,
		VIEW_ERROR
	}
	
	public UCMException()
	{
		super(); 
	}
	
	public UCMException( String s )
	{
		super( s ); 
	}
	
	public UCMException( String s, UCMType type )
	{
		super( s );
		
		this.type = type;
	}

}