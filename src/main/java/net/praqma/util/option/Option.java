package net.praqma.util.option;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents an CLI option.
 * 
 * @author wolfgang
 *
 */
public class Option
{
	public List<String> values = new ArrayList<String>();
	public String longName  = "";
	public String shortName = "";
	public int arguments = 0;
	public boolean used = false;
	public boolean required = true;
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
	
	public Option( String longName, String shortName, boolean required )
	{
		this.longName  = longName;
		this.shortName = shortName;
		this.required  = required;
	}
	
	public Option( String longName, String shortName, boolean optional, int arguments )
	{
		this.longName  = longName;
		this.shortName = shortName;
		this.required  = optional;
		this.arguments = arguments;
	}
	
	/**
	 * Constructor for Option, given a full set of parameters.
	 * @param longName The long name for an option. Multiple characters.<br>Of the form:<br>--option="some option"<br>--option=a b c<br>--option a b c
	 * @param shortName The short name of an option. A single character.<br>Of the form:<br>-o<br>-o 1
	 * @param required Is the option required? true / false.
	 * @param arguments How many arguments does the option have? 0 ... n
	 * @param description A description for the display method.
	 */
	public Option( String longName, String shortName, boolean required, int arguments, String description )
	{
		this.longName    = longName;
		this.shortName   = shortName;
		this.required    = required;
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
		
		if( values.size() == 0 )
		{
			return null;
		}
		
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
