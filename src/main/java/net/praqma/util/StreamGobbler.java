package net.praqma.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamGobbler extends Thread
{
	protected static Debug logger = Debug.GetLogger();
	private static String linesep = System.getProperty( "line.separator" );
	
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
			
			while( ( line = br.readLine() ) != null )
			{
				lres.add( line );
			}
			
			/* Building buffer */
			for( int i = 0 ; i < lres.size() - 1 ; ++i )
			{
				sres.append( lres.get( i ) + linesep );
			}
			sres.append( lres.get( lres.size()-1 ) );
			
			
			synchronized( this )
			{
				notifyAll();
			}
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
}
