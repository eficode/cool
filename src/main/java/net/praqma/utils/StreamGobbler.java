package net.praqma.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamGobbler extends Thread
{
    InputStream is;
    public StringBuffer sres;
    public List<String> lres;
    
    
    StreamGobbler( InputStream is )
    {
        this.is = is;
        lres = new ArrayList<String>();
        sres = new StringBuffer();
    }
    
    public void run( )
    {
		try
		{
			InputStreamReader isr = new InputStreamReader( is );
			BufferedReader br = new BufferedReader( isr );
			String line = null;
			System.out.println( "BEFORE" );
			//while( ( line = br.readLine() ) != null )
			while( true )
			{
				System.out.println( "Next line" );
				br.ready();
				line = br.readLine();
				System.out.println( "LINE=" + line );
				if( line == null ) break;
				System.out.println( "LINE1" );
				sres.append( line );
				lres.add( line );
			}
			System.out.println( "AFTER" );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
}
