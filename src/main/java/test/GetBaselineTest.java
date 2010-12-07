package test;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BaselineList;

public class GetBaselineTest
{

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		for( String s : args )
		{

		}
		
		System.out.println( "Starting..." );
		
		String comp = "component:_Server@\\Cool_PVOB";
		String stre = "stream:Server_int@\\Cool_PVOB";
		
		Stream st1 = UCMEntity.GetStream( stre );
		Component co1 = UCMEntity.GetComponent( comp );
		BaselineList bls = co1.GetBaselines( st1, Project.Plevel.INITIAL );

		System.out.println( "SIZE=" + bls.size() );
		
		for( Baseline b : bls )
		{
			System.out.println( b.toString() );
		}
		
		System.out.println( bls.get( 0 ).Stringify() );
	}
	
	public class Options
	{
		Map<String, Option<?>> options = new HashMap<String, Option<?>>();
		
		public void Bind( Option<?> option )
		{
			options.put( option.GetArg(), option );
		}
		
		public void Parse( String args )
		{
			StringBuffer sb = new StringBuffer();
			Iterator<Entry<String, Option<?>>> it = options.entrySet().iterator();
		    while( it.hasNext() )
		    {
		    	Map.Entry<String, Option<?>> entry = (Map.Entry<String, Option<?>>)it.next();
		    }
		}
		
		public Option GetOption( String arg )
		{
			return options.get( arg );
		}
	}
	
	public interface Option <T>
	{
		public T GetValue( String arg );
		public String GetArg();
	}
	
	public class OptionDual<T> implements Option<T>
	{
		T value = null;
		private String arg = "";
		private String shortform = "";
		
		public String GetArg()
		{
			return arg;
		}
		
		public OptionDual( T dval, String arg )
		{
			this.arg = arg;
		}
		public OptionDual( T dval, String arg, String shorthand )
		{
			this.arg = arg;
			this.shortform = shorthand;
		}

		@Override
		public T GetValue( String arg )
		{
			// TODO Auto-generated method stub
			return null;
		}
	}

}
