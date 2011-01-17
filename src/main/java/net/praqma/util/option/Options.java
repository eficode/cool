package net.praqma.util.option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.praqma.util.Tuple;

public class Options
{
	public enum OptionType
	{
		SINGLE_CHAR,
		STRING
	}
	

	
	//public Map<String, Option> options = new HashMap<String, Option>();
	public List<Option> options = new ArrayList<Option>();
	public String program = "";
	
	public Options()
	{
	}
	
	public void setOption( Option option )
	{
		options.add( option );
	}
	
	public void parse( String[] args )
	{
		if( args.length < 1 )
		{
			System.err.println( "The program could not be recognized." );
			System.exit( 1 );
		}
		
		this.program = args[0];
		System.out.println( "The program is set to " + this.program );
		
		String currentStr = null;
		Option current = null;
		
		for( int i = 1 ; i < args.length ; i++ )
		{
			/* New option */
			if( args[i].startsWith( "-" ) )
			{
				
				/* Of the form --option */
				if( args[i].startsWith( "-", 1 ) )
				{
					
					currentStr = args[i].substring( 2 );
					current = null;
					String[] val = currentStr.split( "=" );

					if( val.length != 2 )
					{
						System.err.println( "Could not deduce " + current );
					}
					else
					{
						currentStr = val[0];
					}
					
					for( Option o : options )
					{
						if( currentStr.equalsIgnoreCase( o.longName ) )
						{
							current = o;
							o.addValue( val[1] );

							
							o.setUsed();
						}
					}
				}
				/* Single char option of the form -o */
				else
				{
					currentStr = args[i].substring( 1 );
					current = null;
					for( Option o : options )
					{
						if( currentStr.equalsIgnoreCase( o.shortName ) )
						{
							current = o;
							o.setUsed();
						}
					}
				}
			}
			else
			{
				if( current != null )
				{
					//options.get( current ).values.add( args[i] );
					current.addValue( args[i] );
				}
			}
		}
	}
	
	public void checkOptions() throws Exception
	{
		for( Option o : options )
		{
			if( !o.optional && !o.used )
			{
				throw new Exception( o.longName + " is not used and is not optional." );
			}
			
			if( o.arguments != o.values.size() )
			{
				throw new Exception( "Incorrect arguments for option " + o.longName + ". " + o.arguments + " required." );
			}
		}
	}
	
	public void print()
	{
		System.out.println( "Printing " + options.size() + " option" + ( options.size() == 1 ? "" : "s" ) );
		
		for( Option o : options )
		{
			System.out.println( "--- " + o.longName + " ---" );
			System.out.println( "Optional: " + o.optional );
			System.out.println( "User: " + o.used );
			System.out.println( "Values:" );
			int c = 0;
			for( String s : o.values )
			{
				c++;
				System.out.println( "[" + c + "] " + s );
			}
		}
	}
	
	
}
