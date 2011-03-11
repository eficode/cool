package test;

import java.io.File;

//import net.praqma.clearcase.ucm.view.DefaultFileFilter;

public class javatest
{

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		File file = new File( "." );
		
		File[] files = file.listFiles();
		
		for( File f : files )
		{
			System.out.println( "--->" + f.getName() );
		}
		
		System.out.println( "" );
		
		/*
		files = file.listFiles( new DefaultFileFilter() );
		
		for( File f : files )
		{
			System.out.println( "--->" + f.getName() );
		}
		*/

	}

}
