package net.praqma.utils;

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