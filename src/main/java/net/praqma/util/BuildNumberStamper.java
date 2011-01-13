package net.praqma.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;


public class BuildNumberStamper
{
	private File src = null;
	private File dst = null;
	
	private final Pattern rx_major_pattern    = Pattern.compile( "(=\\s*)\".*\"(\\s*;\\s*[\\/#]{2,2}\\s*buildnumber\\.major.*$)" );
	private final Pattern rx_minor_pattern    = Pattern.compile( "(=\\s*)\".*\"(\\s*;\\s*[\\/#]{2,2}\\s*buildnumber\\.minor.*$)" );
	private final Pattern rx_patch_pattern    = Pattern.compile( "(=\\s*)\".*\"(\\s*;\\s*[\\/#]{2,2}\\s*buildnumber\\.patch.*$)" );
	private final Pattern rx_sequence_pattern = Pattern.compile( "(=\\s*)\".*\"(\\s*;\\s*[\\/#]{2,2}\\s*buildnumber\\.sequence.*$)" );
	
	private static final String linesep = System.getProperty( "line.separator" );
	
	public BuildNumberStamper( File src ) throws IOException
	{
		this.src = src;
		this.dst = File.createTempFile( "praqma_", ".tmp" );
	}
	
	public void stampIntoCode( String major, String minor, String patch, String sequence ) throws IOException
	{
		BufferedReader reader = new BufferedReader( new FileReader( src ) );
		FileWriter writer = new FileWriter( this.dst );
		
		String s = "";
		
		while( ( s = reader.readLine() ) != null )
		{
			/* Stamp major */
			if( major != null )
			{
				s = rx_major_pattern.matcher( s ).replaceAll( "$1\"" + major + "\"$2" );
			}
			
			/* Stamp minor */
			if( minor != null )
			{
				s = rx_minor_pattern.matcher( s ).replaceAll( "$1\"" + minor + "\"$2" );
			}
			
			/* Stamp patch */
			if( patch != null )
			{
				s = rx_patch_pattern.matcher( s ).replaceAll( "$1\"" + patch + "\"$2" );
			}
			
			/* Stamp sequence */
			if( sequence != null )
			{
				s = rx_sequence_pattern.matcher( s ).replaceAll( "$1\"" + sequence + "\"$2" );
			}
			
			/* Write back */
			writer.write( s + linesep );
		}
		
		writer.close();
		reader.close();
		
		
		copyFile( this.dst, this.src );
	}
	
	public static void copyFile( File sourceFile, File destFile ) throws IOException
	{
		if ( !destFile.exists() )
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		
		try
		{
			source = new FileInputStream( sourceFile ).getChannel();
			destination = new FileOutputStream( destFile ).getChannel();
			destination.transferFrom( source, 0, source.size() );
		}
		finally
		{
			if ( source != null )
			{
				source.close();
			}
			if ( destination != null )
			{
				destination.close();
			}
		}
	}

}
