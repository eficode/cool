package net.praqma.util;

public class CommandLineException extends RuntimeException
{
	CommandLineException()
	{
		super(); 
	}
	
	CommandLineException( String s )
	{
		super( s ); 
	}

}