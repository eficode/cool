package net.praqma.clearcase.ucm;

public class UCMException extends RuntimeException
{
	public UCMType type = UCMType.DEFAULT;
	
	public enum UCMType
	{
		DEFAULT,
		UNKNOWN_TAG
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