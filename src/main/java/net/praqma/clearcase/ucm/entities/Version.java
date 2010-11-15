package net.praqma.clearcase.ucm.entities;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version extends UCMEntity
{
	/* Version specific fields */
	private String kind        = null;
	private String date        = null;
	private String user        = null;
	private String machine     = null;
	private boolean checkedout = false;
	private String comment     = null;
	private String branch      = null;
	
	private String file        = null;
	private int revision       = 0;
	
	private static String rx_revision = "(\\d+)$";
	private static Pattern p_revision = Pattern.compile( "(\\d+)$" );
	
	Version()
	{
	}
	
	/**
	 * This method is only available to the package, because only ClearcaseEntity should
	 * be allowed to call it.
	 * @return A new Version Entity
	 */
	static Version GetEntity()
	{
		return new Version();
	}
	
	public void PostProcess()
	{
		logger.trace_function();

		
		String fqname = this.fqname.matches( "^\\S:\\\\.*" ) ? this.fqname : System.getProperty( "user.dir" ) + filesep + this.fqname;
		
		this.fqname = fqname;
		
		Matcher m = p_revision.matcher( this.fqname );
		if( m.find() )
		{
			this.revision = Integer.parseInt( m.group( 1 ) );
		}
		else
		{
			this.revision = 0;
		}
		
		String tmp = this.fqname;
		tmp        = tmp.replaceFirst( "(?m)\\@\\@.*$", "" );
		tmp        = tmp.replaceFirst( "(?m)^\\s+", "" );
		this.file  = tmp;
	}
	
	/* Getters */
	
	public String GetUser()
	{
		if( !loaded ) Load();
		
		return this.user;
	}
	
	public String GetFile()
	{
		if( !loaded ) Load();
		
		return this.file;
	}
	
	public String GetDate()
	{
		if( !loaded ) Load();
		
		return this.date;
	}
	
	public int GetRevision()
	{
		if( !loaded ) Load();
		
		return this.revision;
	}
	
	public void Load()
	{
		logger.trace_function();

		HashMap<String, String> result = context.GetVersion( this );
		
		this.date       = result.get( "date" );
		this.user       = result.get( "user" );
		this.machine    = result.get( "machine" );
		this.comment    = result.get( "comment" );
		this.checkedout = result.get( "checkedout" ).length() > 0 ? true : false;
		this.kind       = result.get( "kind" );
		this.branch     = result.get( "branch" );
		
		this.loaded = true;
	}
	
	
	public String Stringify()
	{	
		StringBuffer sb = new StringBuffer();
		sb.append( super.Stringify() + linesep );
		
		sb.append( "Filename: " + this.file + linesep );
		sb.append( "Revision: " + this.revision + linesep );
		
		return sb.toString();
	}
}
