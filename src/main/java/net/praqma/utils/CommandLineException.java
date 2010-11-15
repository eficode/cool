package net.praqma.utils;

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