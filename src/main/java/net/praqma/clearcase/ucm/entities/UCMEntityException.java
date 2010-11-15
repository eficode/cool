package net.praqma.clearcase.ucm.entities;

public class UCMEntityException extends RuntimeException
{
	UCMEntityException()
	{
		super(); 
	}
	
	UCMEntityException( String s )
	{
		super( s ); 
	}

}