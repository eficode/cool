package net.praqma.util.option;

import java.util.ArrayList;
import java.util.List;

public class Option
{
	public List<String> values = new ArrayList<String>();
	public String longName  = "";
	public String shortName = "";
	public int arguments = 0;
	public boolean used = false;
	public boolean optional = false;
	public String description = "";
	
	public Option( String longName, String shortName, String value )
	{
		this.longName  = longName;
		this.shortName = shortName;
		this.values.add( value );
	}
	
	public Option( String longName, String shortName )
	{
		this.longName  = longName;
		this.shortName = shortName;
	}
	
	public Option( String longName, String shortName, boolean optional )
	{
		this.longName  = longName;
		this.shortName = shortName;
		this.optional  = optional;
	}
	
	public Option( String longName, String shortName, boolean optional, int arguments )
	{
		this.longName  = longName;
		this.shortName = shortName;
		this.optional  = optional;
		this.arguments = arguments;
	}
	
	public Option( String longName, String shortName, boolean optional, int arguments, String description )
	{
		this.longName    = longName;
		this.shortName   = shortName;
		this.optional    = optional;
		this.arguments   = arguments;
		this.description = description;
	}
	
	public void setUsed()
	{
		used = true;
	}
	
	public void addValue( String value )
	{
		values.add( value );
	}
	
	
	public String getString()
	{
		StringBuffer sb = new StringBuffer();
		
		for( String s : values )
		{
			sb.append( s );
		}			

		return sb.toString();
	}
	
	public int getSum() throws Exception
	{
		int sum = 0;
		
		for( String s : values )
		{
			try
			{
				sum += Integer.parseInt( s );
			}
			catch( NumberFormatException e )
			{
				throw new Exception( "The value " + longName + " is not an integer." );
			}
		}			

		return sum;
	}
	
	public double getRealSum() throws Exception
	{
		double sum = 0;
		
		for( String s : values )
		{
			try
			{
				sum += Double.parseDouble( s );
			}
			catch( NumberFormatException e )
			{
				throw new Exception( "The value " + longName + " is not a value." );
			}
		}			

		return sum;
	}
}
