package net.praqma.clearcase.ucm.entities;

public class UCMEntityDoesNotExistException extends Exception
{
	UCMEntityDoesNotExistException()
	{
		super(); 
	}
	
	UCMEntityDoesNotExistException( String s )
	{
		super( s ); 
	}

}