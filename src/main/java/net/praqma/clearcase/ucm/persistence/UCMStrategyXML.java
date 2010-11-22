package net.praqma.clearcase.ucm.persistence;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.utils.Debug;

public class UCMStrategyXML implements UCMStrategyInterface
{
	private static Debug logger = Debug.GetLogger();
	
	static
	{
		//logger.ExcludeClass( UCMStrategyXML.class.getName() );
	}
	
	private Document xml = null;
	private final String testBaseFile = "testbase.xml";
	
	private Element root       = null;
	private Element baselines  = null;
	private Element streams    = null;
	private Element versions   = null;
	private Element activities = null;
	private Element tags       = null;
	
	private static int tagCounter = 20001;
	
	private static final String filesep = System.getProperty( "file.separator" );
	private static final String linesep = System.getProperty( "line.separator" );
	
	public UCMStrategyXML( )
	{
		logger.trace_function();
		
		logger.log( "Using XML strategy" );
		
		/* The search result */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware( true );
		
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();

			logger.log( "Getting XML as stream" );
			xml = builder.parse( this.getClass().getClassLoader().getResourceAsStream( testBaseFile ) );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		root       = xml.getDocumentElement();
		baselines  = this.GetFirstElement( root, "baselines" );
		streams    = this.GetFirstElement( root, "streams" );
		versions   = this.GetFirstElement( root, "versions" );
		activities = this.GetFirstElement( root, "activities" );
		tags       = this.GetFirstElement( root, "tags" );
		
		logger.debug( "root=" + root.getTagName() );
		logger.debug( "baselines=" + baselines.getTagName() );
		logger.debug( "streams=" + streams.getTagName() );
		logger.debug( "activities=" + activities.getTagName() );
		logger.debug( "tags=" + tags.getTagName() );
	}
	
	/**** Baseline *****/
	
	public String LoadBaseline( String baseline )
	{
		logger.trace_function();
		logger.debug( baseline );
		
		Element ble = GetElementWithFqname( baselines, baseline );
		
		if( ble == null )
		{
			throw new UCMException( "No baselines with name " + baseline );
		}
		
		String str = "";
		
		try
		{
			str = GetElement( ble, "shortname" ).getTextContent() + "::" + 
	 	          GetElement( ble, "component" ).getTextContent() + "::" +
	 			  GetElement( ble, "stream" ).getTextContent() + "::" +
	 			  GetElement( ble, "plevel" ).getTextContent() + "::" +
	 			  GetElement( ble, "user" ).getTextContent() + "::";
		}
		catch( DOMException e )
		{
			throw new UCMException( "For baseline " + baseline + ": " + e.getMessage() );
		}
		
		return str;
	}
	
	public String GetBaselineDiff( String baseline, String other, boolean nmerge )
	{
		logger.trace_function();
		logger.debug( baseline );
		
		ArrayList<Element> acts = _GetBaselineActivities( baseline );
		
		StringBuffer sb = new StringBuffer();
		
		for( Element act : acts )
		{
			logger.debug( "---->"+act.getAttribute( "fqname" ) );
			sb.append( ">> " + act.getAttribute( "fqname" ) + linesep );
			
			sb.append( GetActivityVersions( act ) );

		}
		
		return sb.toString();
	}
	
	public String GetBaselineActivities( String baseline )
	{
		return "";
	}
	

	public String SetPromotionLevel( String baseline, String plevel )
	{
		logger.trace_function();
		logger.debug( baseline );
		
		logger.debug( "setting plevel " + plevel );
		
		Element pl = GetElement( GetElementWithFqname( baselines, baseline ), "plevel" );
		pl.setTextContent( plevel );
		
		return "";
	}
	
	/**** Baseline helper functions ****/
	
	private ArrayList<Element> _GetBaselineActivities( String baseline )
	{
		logger.trace_function();
		logger.debug( baseline );
		
		Element ble = GetElementWithFqname( baselines, baseline );
		Element act = GetElement( ble, "activities" );
		
		NodeList list = act.getChildNodes( );
		ArrayList<Element> result = new ArrayList<Element>();
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
    		if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			if( node.getNodeName().equalsIgnoreCase( "activity" ) )
    			{
    				/* Get activity element with fqname */
    				Element ace = GetElementWithFqname( activities, node.getTextContent() );
    				//Element cse = GetElement( ace, "changeset" );
    				
    				result.add( ace );
    			}
    		}
		}
		
		return result;
	}
	
	/**
	 * Retrieve the versions from a given activity
	 * @param activity
	 * @return A String, similar to cleartool, with versions
	 */
	private String GetActivityVersions( Element activity )
	{
		logger.debug( "" );
		
		/* Get the changeset from an activity */
		Element ce = GetElement( activity, "changeset" );
		StringBuffer sb = new StringBuffer();
		
		NodeList list = ce.getChildNodes( );
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
    		if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			if( node.getNodeName().equalsIgnoreCase( "version" ) )
    			{
    				sb.append( node.getTextContent() + linesep );
    			}
    		}
		}
		
		return sb.toString();
	}
	
	/* Component */
	
	public ArrayList<String> GetBaselines( String component, String stream, String plevel )
	{
		logger.trace_function();
		logger.debug( component + ", " + stream + ", " + plevel );
		
		NodeList list = baselines.getChildNodes( );
		ArrayList<String> result = new ArrayList<String>();
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
    		if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			HashMap<String, String> attrs = GetAttributes( (Element)node );
    			
    			String c = GetElement( (Element)node, "component" ).getTextContent();
    			String s = GetElement( (Element)node, "stream" ).getTextContent();
    			String p = GetElement( (Element)node, "plevel" ).getTextContent();
    			/* If plevel is the empty string, we don't care about the plevel of the baseline */
    			p = plevel.equals( "" ) ? "" : p;
    			if( c.equals( component ) && s.equals( stream ) && ( p.equals( plevel ) ) )
    			{
    				result.add( attrs.get( "sname" ) );
    			}
    		}
		}
		
		return result;
	}

	
	/* Stream */
	
	public void RecommendBaseline( String stream, String baseline ) throws UCMException
	{
		logger.trace_function();
		logger.debug( stream + ", " + baseline );
		
		Element e = GetElement( GetElementWithFqname( streams, stream ), "recommended_baseline" );
		
		if( e == null )
		{
			throw new UCMException( "Could not set recommended baseline" );
		}
		
		e.setTextContent( baseline );
	}
	
	public String GetRecommendedBaselines( String stream )
	{
		logger.trace_function();
		logger.debug( stream );
		
		Element se = GetElementWithFqname( streams, stream );
		Element e = GetElement( se, "recommended_baseline" );
		
		if( e != null )
		{
			return e.getTextContent();
		}
		else
		{
			throw new UCMException( "Recommended baseline not found." );
		}
	}
	
	
	
	/* Version */
	
	public String GetVersion( String version, String separator )
	{
		logger.trace_function();
		logger.debug( version );
		
		Element ve = GetElementWithFqname( versions, version );
		
		return GetElement( ve, "date" ).getTextContent() + separator +
		       GetElement( ve, "user" ).getTextContent() + separator +
		       GetElement( ve, "machine" ).getTextContent() + separator +
		       GetElement( ve, "comment" ).getTextContent() + separator +
		       GetElement( ve, "checkedout" ).getTextContent() + separator +
		       GetElement( ve, "kind" ).getTextContent() + separator +
		       GetElement( ve, "branch" ).getTextContent();

	}
	
	
	/* TAGS */
	public String GetTags( String name )
	{
		logger.trace_function();
		logger.debug( name );
		
		ArrayList<Element> tags = GetElementsWithName( this.tags, "entity", name );
		
		StringBuffer sb = new StringBuffer();
		for( Element t : tags )
		{
			sb.append( t.getAttribute( "fqname" ) + "\n" );
		}
		
		return sb.toString();		
	}
	
	public String GetTag( String fqname )
	{
		logger.trace_function();
		logger.debug( fqname );
		
		Element tag = GetElementWithFqname( tags, fqname );
		
		return tag.getTextContent();		
	}
	
	public String PutTag( String fqname, String keyval, UCMEntity entity )
	{
		logger.trace_function();
		logger.debug( fqname );
		
		Element tag = GetElementWithFqname( tags, fqname );
		
		/* Replace the old with new */
		/* TODO Get the next oid for the new tag. The old oid SHOULD NOT be reused */
		tag.setTextContent( keyval );
		tag.setAttribute( "fqname", "tag@" + tagCounter + "@" + entity.GetPvob() );
		tag.setAttribute( "entity", entity.GetFQName() );
		
		tagCounter++;
		
		return tag.getAttribute( "fqname" );
	}
	
	public String NewTag( UCMEntity entity, String cgi )
	{
		logger.trace_function();
		logger.debug( "CGI=" + cgi );
		
		Element tag = xml.createElement( "tag" );
		tag.setAttribute( "fqname", "tag@" + tagCounter + "@" + entity.GetPvob() );
		tag.setAttribute( "entity", entity.GetFQName() );
		tag.setTextContent( cgi );
		
		tags.appendChild( tag );
		
		tagCounter++;
		
		return tag.getAttribute( "fqname" );
	}
	
	public void DeleteTagsWithID( String tagType, String tagID, String entity )
	{
		logger.trace_function();
		logger.debug( tagType + tagID );
		
		ArrayList<Element> ts = GetElementsWithName( tags, "entity", entity );
		for( Element e : ts )
		{
			tags.removeChild( e );
		}		
	}
	
	public void DeleteTagsWithID( String ID )
	{
		
	}
	
	
	/* Snapshot views */
	public void MakeSnapshotView( String stream, String viewtag, String viewroot )
	{
		
	}
	
	
	/* XML FUNCTIONALITY */
	
	private Element GetElementWithFqname( Element e, String fqname )
	{
		logger.trace_function();
		logger.debug( "Getting " + e.getNodeName() + " element with fqname: " + fqname );
		
		NodeList list = e.getChildNodes( );
		//NodeList list = e.getElementsByTagName( "stream" );
		
		
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
	    	if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			HashMap<String, String> attrs = GetAttributes( (Element)node );

    			if( attrs.get( "fqname" ) != null && attrs.get( "fqname" ).equals( fqname ) )
    			{
    				return (Element)node;
    			}
    		}
		}
		
		logger.warning( fqname + " was not found" );
		
		return null;
	}
	
	private ArrayList<Element> GetElementsWithFqname( Element e, String fqname )
	{
		logger.trace_function();
		logger.debug( "Getting " + e.getNodeName() + " element with fqname: " + fqname );
		
		NodeList list = e.getChildNodes( );
		//NodeList list = e.getElementsByTagName( "stream" );
		
		ArrayList<Element> result = new ArrayList<Element>();
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
	    	if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			HashMap<String, String> attrs = GetAttributes( (Element)node );

    			if( attrs.get( "fqname" ) != null && attrs.get( "fqname" ).equals( fqname ) )
    			{
    				result.add( (Element)node );
    			}
    		}
		}
		
		return result;
	}
	
	private ArrayList<Element> GetElementsWithName( Element e, String attr, String name )
	{
		logger.trace_function();
		logger.debug( "Getting " + e.getNodeName() + " elements with " + attr + " = " + name );
		
		NodeList list = e.getChildNodes( );
		
		ArrayList<Element> result = new ArrayList<Element>();
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
	    	if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			HashMap<String, String> attrs = GetAttributes( (Element)node );

    			if( attrs.get( attr ) != null && attrs.get( attr ).equals( name ) )
    			{
    				result.add( (Element)node );
    			}
    		}
		}
		
		return result;
	}
	
	private Element GetElement( Element e, String tag ) throws DOMException
	{
		logger.trace_function();
		logger.debug( "Getting "+e.getNodeName()+" element: " + tag );
		
		NodeList list = e.getChildNodes( );
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
    		if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			if( node.getNodeName().equals( tag ) )
    			{
    				return (Element)node;
    			}
    		}
		}
		
		throw new DOMException( DOMError.SEVERITY_WARNING, "Could not GetElement " + tag );
	}



	private HashMap<String, String> GetAttributes( Element element )
	{
		logger.trace_function();
		
		NamedNodeMap nnm = element.getAttributes( );
		int size = nnm.getLength( );
		HashMap<String, String> list = new HashMap<String, String>( );
		
		for( int i = 0 ; i < size ; i++ )
		{
			Attr at = (Attr)nnm.item( i );
			list.put( at.getName( ), at.getValue( ) );
			logger.debug( "ATTR="+at.getNodeName() );
		}
		
		return list;
	}


	private Element GetFirstElement( Element root, String element )
	{
		logger.trace_function();
		
		//NodeList sections = root.getElementsByTagName( element );
		NodeList sections = root.getChildNodes();
		
	    int numSections = sections.getLength();

	    for ( int i = 0 ; i < numSections ; i++ )
	    {
	    	Node node = sections.item( i );
	    	
    		if( node.getNodeType( ) == Node.ELEMENT_NODE && node.getNodeName().equals( element ) )
    		{
    			return (Element)node;
    		}
	    }
	    
	    return null;
	}
	
	
	public String GetXML()
	{
		StringWriter out = new StringWriter();
		
		try
		{
		    TransformerFactory factory = TransformerFactory.newInstance();
		    //factory.setAttribute( "indent-number", new Integer( 4 ) );
		    		    
		    Transformer transformer = factory.newTransformer();
		    
		    transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
		    transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
		    		    
		    //aTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
		    transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
	
		    Source src = new DOMSource( xml );
		    
	    	Result dest = new StreamResult( out );
	    	transformer.transform( src, dest );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
		return out.toString();
	}
	
	public void SaveState()
	{
		String xml = GetXML();
		try
		{
			FileWriter fw = new FileWriter( "backupbase.xml" );
			BufferedWriter bw = new BufferedWriter( fw );
			bw.append( xml );
			bw.close();
			fw.close();
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void DeleteTag( String fqname )
	{
		// TODO Auto-generated method stub
		
	}

}