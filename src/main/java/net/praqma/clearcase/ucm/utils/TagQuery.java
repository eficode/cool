package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;

import net.praqma.utils.Tuple;

public class TagQuery extends ArrayList<Tuple<String, String>>
{
	public TagQuery( )
	{
	}
	
	public TagQuery( List<Tuple<String, String>> qlist )
	{
		this.addAll( qlist );
	}
	
	public void AddCondition( String key, String condition )
	{
		this.add( new Tuple<String, String>( key, condition ) );
	}
}