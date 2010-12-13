package net.praqma.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamGobbler extends Thread
{
	protected static Debug logger = Debug.GetLogger();
	
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
			
			logger.debug( "Gobbling..." );
			
			while( ( line = br.readLine() ) != null )
			{
				sres.append( line );
				lres.add( line );
			}
			
			logger.debug( "... End of gobbling" );
		}
		catch ( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}
}
