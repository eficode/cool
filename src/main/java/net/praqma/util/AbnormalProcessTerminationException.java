package net.praqma.util;

public class AbnormalProcessTerminationException extends RuntimeException
{
	AbnormalProcessTerminationException()
	{
		super(); 
	}
	
	AbnormalProcessTerminationException( String s )
	{
		super( s ); 
	}

}