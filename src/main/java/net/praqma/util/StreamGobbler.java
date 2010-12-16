package net.praqma.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
			
//			StringWriter sw = new StringWriter();
//			BufferedWriter bw = new BufferedWriter( sw );
//
//			int ch;
//			while( ( ch = br.read() ) > -1 )
//			{
//				sw.write( ch );
//			}
			
			while( ( line = br.readLine() ) != null )
			{
				sres.append( line + linesep );
				lres.add( line );
			}
			
			/* Delete the last line break */
			sres.deleteCharAt( sres.length()-1 );
			
			
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
