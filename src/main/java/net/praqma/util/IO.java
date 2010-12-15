package net.praqma.util;

import java.io.File;

public class IO
{
	public static boolean DeleteDirectory( File directory )
	{
		if ( directory.isDirectory() )
		{
			String[] elements = directory.list();
			for( int i = 0 ;  i < elements.length ; i++ ) 
			{
				boolean success = DeleteDirectory( new File( directory, elements[i] ) ); 
				if ( !success ) return false;
			} 
		} 
		
		return directory.delete();
	}
}
