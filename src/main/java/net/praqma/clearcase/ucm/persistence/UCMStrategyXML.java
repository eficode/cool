package net.praqma.clearcase.ucm.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import net.praqma.clearcase.Region;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.structure.Tuple;


public class UCMStrategyXML implements UCMStrategyInterface
{
	//private static Logger logger = Logger.getLogger();
	
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
	private Element views      = null;
	
	private static Integer IDCounter = 20001;
	
	private static final String filesep = System.getProperty( "file.separator" );
	private static final String linesep = System.getProperty( "line.separator" );
	
	public UCMStrategyXML( )
	{
//		logger.trace_function();
//		
//		logger.log( "Using XML strategy" );
		
		/* The search result */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware( true );
		
		DocumentBuilder builder;
		try
		{
			builder = factory.newDocumentBuilder();

//			logger.log( "Getting XML as stream = " + this.getClass().getClassLoader().getResourceAsStream( testBaseFile ) );
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
		views      = this.GetFirstElement( root, "views" );
		
		//		logger.debug( "root=" + root.getTagName() );
		//		logger.debug( "baselines=" + baselines.getTagName() );
		//	logger.debug( "streams=" + streams.getTagName() );
		//	logger.debug( "activities=" + activities.getTagName() );
		//	logger.debug( "tags=" + tags.getTagName() );
	}
	
	/**** Baseline *****/
	
	public String LoadBaseline( String baseline ) throws UCMException
	{
	    //logger.trace_function();
	    //logger.debug( baseline );
		
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
	
	public List<String> GetBaselineDiff( File dir, String baseline, String other, boolean nmerge, String pvob )
	{
	    //logger.trace_function();
	    //logger.debug( baseline );
		
		ArrayList<Element> acts = _GetBaselineActivities( baseline );
		
		//StringBuffer sb = new StringBuffer();
		List<String> list = new ArrayList<String>();
		
		for( Element act : acts )
		{
		    //	logger.debug( "---->"+act.getAttribute( "fqname" ) );
			//sb.append( ">> " + act.getAttribute( "fqname" ) + linesep );
			list.add( ">> " + act.getAttribute( "fqname" ) + linesep );
			
			//sb.append( GetActivityVersions( act ) );
			list.addAll( GetActivityVersions( act ) );

		}
		
		//return sb.toString();
		return list;
	}
	
	public String GetBaselineActivities( String baseline )
	{
		return "";
	}
	

	public void SetPromotionLevel( String baseline, String plevel )
	{
	    //logger.trace_function();
	    //logger.debug( baseline );
		
	    //logger.debug( "setting plevel " + plevel );
		
		Element pl = GetElement( GetElementWithFqname( baselines, baseline ), "plevel" );
		pl.setTextContent( plevel );
	}
	
	/**** Baseline helper functions ****/
	
	private ArrayList<Element> _GetBaselineActivities( String baseline )
	{
	    //logger.trace_function();
	    //logger.debug( baseline );
		
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
	private List<String> GetActivityVersions( Element activity )
	{
		/* Get the changeset from an activity */
		Element ce = GetElement( activity, "changeset" );
		//StringBuffer sb = new StringBuffer();
		List<String> alist = new ArrayList<String>();
		
		NodeList list = ce.getChildNodes( );
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
    		if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			if( node.getNodeName().equalsIgnoreCase( "version" ) )
    			{
    				//sb.append( node.getTextContent() + linesep );
    				alist.add( node.getTextContent() + linesep );
    			}
    		}
		}
		
		//return sb.toString();
		return alist;
	}
	
	/* Component */
	
	public ArrayList<String> GetBaselines( String component, String stream, String plevel )
	{
	    //logger.trace_function();
	    //logger.debug( component + ", " + stream + ", " + plevel );
		
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
	    //logger.trace_function();
	    //logger.debug( stream + ", " + baseline );
		
		Element e = GetElement( GetElementWithFqname( streams, stream ), "recommended_baseline" );
		
		if( e == null )
		{
			throw new UCMException( "Could not set recommended baseline" );
		}
		
		e.setTextContent( baseline );
	}
	
	public String GetRecommendedBaselines( String stream ) throws UCMException
	{
	    //logger.trace_function();
	    //logger.debug( stream );
		
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
	
	public void CreateStream( String pstream, String nstream, boolean readonly, String baseline )
	{
	    //logger.trace_function();
	    //logger.debug( "PARENT=" + pstream + ". NEW=" + nstream );
		
		Element stream = xml.createElement( "stream" );
		
		stream.setAttribute( "fqname", nstream );
		stream.setAttribute( "readonly", ( readonly ? "1" : "0" ) );
		
		Element recbl = xml.createElement( "recommended_baseline" );
		Element bl    = xml.createElement( "baseline" );
		bl.setTextContent( baseline );
		
		stream.appendChild( recbl );
		stream.appendChild( bl );
		
		streams.appendChild( stream );
	}
	
	public boolean StreamExists( String fqname )
	{
	    //logger.trace_function();
	    //logger.debug( fqname );
		
		Element stream = GetElementWithFqname( streams, fqname );
		
		if( stream != null )
		{
			return true;
		}
		
		return false;
	}
	
	
	public String GetStreamFromView( String viewtag ) throws UCMException
	{
	    //logger.trace_function();
	    //logger.debug( viewtag );
		
		List<Element> list = GetElementsWithAttribute( views, "viewtag", viewtag );
		
		if( list.size() != 1 )
		{
		    //logger.warning( "There is no unique view! Found " + list.size() + " views." );
			throw new UCMException( "There is no unique view! Found " + list.size() + " views." );
		}
		
		//return list.get( 0 ).getAttribute( "viewroot" );
		return GetElement( list.get( 0 ), "stream" ).getTextContent();
	}
	
	public File GetCurrentViewRoot( File viewroot ) throws UCMException
	{
	    //logger.trace_function();
	    //logger.debug( viewroot.toString() );
		
		List<Element> list = GetElementsWithAttribute( views, "viewroot", viewroot.toString() );
		
		if( list.size() != 1 )
		{
		    //logger.warning( "There is no unique view! Found " + list.size() + " views." );
			throw new UCMException( "There is no unique view! Found " + list.size() + " views." );
		}
		
		return new File( list.get( 0 ).getAttribute( "viewroot" ) );
	}
	
	public String ViewrootIsValid( File viewroot ) throws UCMException
	{
		List<Element> list = GetElementsWithAttribute( views, "viewroot", viewroot.toString() );
		
		if( list.size() != 1 )
		{
		    //logger.warning( "There is no unique view! Found " + list.size() + " views." );
			throw new UCMException( "There is no unique view! Found " + list.size() + " views." );
		}
		
		return list.get( 0 ).getAttribute( "viewtag" );
	}
	
	
	/* Version */
	
	public String GetVersion( String version, String separator )
	{
	    //logger.trace_function();
	    //logger.debug( version );
		
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
	public List<Tuple<String, String>> GetTags( String name )
	{
	    //logger.trace_function();
	    //logger.debug( name );
		
		ArrayList<Element> tags = GetElementsWithAttribute( this.tags, "entity", name );
		
		//StringBuffer sb = new StringBuffer();
		List<Tuple<String, String>> list = new ArrayList<Tuple<String, String>>();
		
		for( Element t : tags )
		{
			//sb.append( t.getAttribute( "fqname" ) + "\n" );
			list.add( new Tuple<String, String>( t.getAttribute( "fqname" ), t.getTextContent() ) );
		}
		
		return list;		
	}
	
	public String GetTag( String fqname )
	{
	    //logger.trace_function();
	    //logger.debug( fqname );
		
		Element tag = GetElementWithFqname( tags, fqname );
		
		return tag.getTextContent();		
	}
	
	public String PutTag( String fqname, String keyval, UCMEntity entity )
	{
	    //logger.trace_function();
	    //logger.debug( fqname );
		
		Element tag = GetElementWithFqname( tags, fqname );
		
		/* Replace the old with new */
		/* TODO Get the next oid for the new tag. The old oid SHOULD NOT be reused */
		tag.setTextContent( keyval );
		tag.setAttribute( "fqname", "tag@" + IDCounter + "@" + entity.getPvobString() );
		tag.setAttribute( "entity", entity.getFullyQualifiedName() );
		
		IDCounter++;
		
		return tag.getAttribute( "fqname" );
	}
	
	public String NewTag( UCMEntity entity, String cgi )
	{
	    //logger.trace_function();
	    //logger.debug( "CGI=" + cgi );
		
		Element tag = xml.createElement( "tag" );
		tag.setAttribute( "fqname", "tag@" + IDCounter + "@" + entity.getPvobString() );
		tag.setAttribute( "entity", entity.getFullyQualifiedName() );
		tag.setTextContent( cgi );
		
		tags.appendChild( tag );
		
		IDCounter++;
		
		return tag.getAttribute( "fqname" );
	}
	
	public void DeleteTag( String fqname )
	{
	    //logger.trace_function();
	    //logger.debug( fqname );
		
		ArrayList<Element> ts = GetElementsWithAttribute( tags, "fqname", fqname );
		for( Element e : ts )
		{
		    //logger.debug( "Deleting Tag: " + e.getAttribute( "fqname" ) );
			tags.removeChild( e );
		}	
	}
	
	public void DeleteTagsWithID( String tagType, String tagID, String entity )
	{
	    //logger.trace_function();
	    //logger.debug( tagType + tagID );
		
		ArrayList<Element> ts = GetElementsWithAttribute( tags, "entity", entity );
		for( Element e : ts )
		{
			if( e.getTextContent().matches( "^.*tagtype=" + tagType + ".*$" ) && e.getTextContent().matches( "^.*tagid=" + tagID + ".*$" ) )
			{
			    //logger.debug( "Deleting Tag: " + e.getAttribute( "fqname" ) );
				tags.removeChild( e );
			}
		}		
	}
	
	public void DeleteTagsWithID( String ID )
	{
		
	}
	
	
	/* Snapshot views */
	public void MakeSnapshotView( String stream, File viewroot, String viewtag )
	{
	    //logger.trace_function();
	    //logger.debug( stream.toString() + " ," + viewroot + ". " + viewroot.toString() );
		
		Element view = xml.createElement( "view" );
		view.setAttribute( "uuid", IDCounter.toString() );
		view.setAttribute( "viewroot", viewroot.toString() );
		view.setAttribute( "viewtag", viewtag );
		
		Element st1 = xml.createElement( "stream" );
		st1.setTextContent( stream );
		
		view.appendChild( st1 );
		views.appendChild( view );
		
		IDCounter++;
		
		//return tag.getAttribute( "fqname" );
	}
	
	public boolean ViewExists( String viewtag )
	{
	    //logger.trace_function();
	    //logger.debug( viewtag );
		
		List<Element> list = GetElementsWithAttribute( views, "viewtag", viewtag );
		
		if( list.size() == 0 )
		{
			return false;
		}
		
		if( list.size() > 1 )
		{
		    //logger.warning( "More than one view with tag " + viewtag );
		}
		
		return true;
	}
	
	public void RegenerateViewDotDat( File dir, String viewtag ) throws UCMException
	{
		/* This function is not applicable for XML */
	}
	
	
	/* XML FUNCTIONALITY */
	
	private Element GetElementWithFqname( Element e, String fqname )
	{
	    //logger.trace_function();
		//logger.debug( "Getting " + e.getNodeName() + " element with fqname: " + fqname );
		
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
		
		//logger.warning( fqname + " was not found" );
		
		return null;
	}
	
	private ArrayList<Element> GetElementsWithFqname( Element e, String fqname )
	{
	    //logger.trace_function();
		//logger.debug( "Getting " + e.getNodeName() + " element with fqname: " + fqname );
		
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
	
	private ArrayList<Element> GetElementsWithAttribute( Element e, String attr, String name )
	{
	    //logger.trace_function();
		//logger.debug( "Getting " + e.getNodeName() + " elements with " + attr + " = " + name );
		
		NodeList list = e.getChildNodes( );
		
		ArrayList<Element> result = new ArrayList<Element>();
		
		for( int i = 0 ; i < list.getLength( ) ; i++ )
		{
	    	Node node = list.item( i );
	    	
	    	if( node.getNodeType( ) == Node.ELEMENT_NODE )
    		{
    			HashMap<String, String> attrs = GetAttributes( (Element)node );
    			
    			//((Element)node).getAttributeNode( "" )

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
	    //logger.trace_function();
		//logger.debug( "Getting "+e.getNodeName()+" element: " + tag );
		
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
	    //logger.trace_function();
		
		NamedNodeMap nnm = element.getAttributes( );
		int size = nnm.getLength( );
		HashMap<String, String> list = new HashMap<String, String>( );
		
		for( int i = 0 ; i < size ; i++ )
		{
			Attr at = (Attr)nnm.item( i );
			list.put( at.getName( ), at.getValue( ) );
			//logger.debug( "ATTR="+at.getNodeName() );
		}
		
		return list;
	}


	private Element GetFirstElement( Element root, String element )
	{
	    //logger.trace_function();
		
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
    public void checkViewContext(File dir) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getMastership(String fqname) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getProjectFromStream(String stream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getModifiableComponents(String project) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String loadProject(String project) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String loadActivity(String activity) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String loadBaseline(String baseline) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getBaselineDiff(File dir, String baseline, String other, boolean nmerge, String pvob) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPromotionLevel(String baseline, String plevel) throws UCMException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public String getBaselineActivities(String baseline) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deliver(String baseline, String stream, String target, File viewroot, String viewtag, boolean force, boolean complete, boolean abort)
            throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelDeliver(File viewcontext) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String deliverStatus(String stream) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getBaselines(String component, String stream, Plevel plevel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRootDir(String component) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String loadComponent(String component) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String loadHyperLink(String fqname, File dir) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Tuple<String, String>> getHlinks(String fqname, String hlinkType, File dir) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recommendBaseline(String stream, String baseline) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getRecommendedBaselines(String stream) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStreamFromView(String viewtag) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createStream(String pstream, String nstream, boolean readonly, String baseline) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean streamExists(String fqname) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean rebaseStream(String viewtag, String stream, String baseline, boolean complete) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRebasing(String stream) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void cancelRebase(String stream) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getLatestBaselines(String stream) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void generate(String stream) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String loadStream(String stream) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Stream> getChildStreams(String fqstream) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVersion(String version, String separator) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVersionExtension(File file, File viewroot) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String[]> getTags(String fqname) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTag(String fqname) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String newTag(UCMEntity entity, String cgi) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteTag(String fqname) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteTagsWithID(String tagType, String tagID, String entity) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String putTag(String fqname, String keyval, UCMEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void makeSnapshotView(String stream, File viewroot, String viewtag) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public File getCurrentViewRoot(File viewroot) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String viewrootIsValid(File viewroot) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean viewExists(String viewtag) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void regenerateViewDotDat(File dir, String viewtag) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, Integer> swipeView(File viewroot, boolean excludeRoot) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String viewUpdate(File viewroot, boolean overwrite, String loadrules) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttribute(String fqname, String attribute) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getAttributes(String fqname) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getAttributes(String fqname, File dir) throws UCMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(String fqname, String attribute, String value) throws UCMException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<PVob> getVobs(Region region) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getXML() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveState() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<UCMView> getViews( Region region ) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public List<Project> getProjects(PVob vob) throws UCMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createVob(String vobname, boolean UCMProject, String path, String comment)
			throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createComponent(String name, PVob pvob, String root, String comment, File view) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> loadVob(Vob vob) throws UCMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mountVob(Vob vob) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createView(String tag, String path, boolean snapshotView, Stream stream)
			throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeVob( Vob vob ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unmountVob( Vob vob ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createProject( String name, String root, PVob pvob, int policy, String comment, Component... mcomps ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createIntegrationStream( String name, Project project, Baseline baseline ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeView( UCMView view ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> loadView( UCMView view ) throws UCMException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void createBaseline( String baseline, Component component, File view, boolean incremental, boolean identical, Activity[] activities, Component[] depends ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createActivity( String name, PVob pvob, boolean force, String comment, File view ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addToSourceControl( File file, File view ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkOut( File file, File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkIn( File file, boolean identical, File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeVersion( Version version, File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startView( UCMView view ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Version> baselineDifferences( Baseline bl1, Baseline bl2, boolean nomerge, SnapshotView view ) throws UCMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<File> getUnchecedInFiles( File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void uncheckout( File file, boolean keep, File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeName( File file, boolean checkedOut, File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveFile( File file, File destination, File viewContext ) throws UCMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadVersion( Version version ) throws UCMException {
		// TODO Auto-generated method stub
		
	}
}
