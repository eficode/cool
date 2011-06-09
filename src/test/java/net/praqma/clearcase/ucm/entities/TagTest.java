package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import org.junit.*;
import java.util.HashMap;
import java.util.Map;

import net.praqma.clearcase.ucm.UCMException;

public class TagTest extends Cool
{	
	@BeforeClass
	public static void startup()
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
	}
	
	/*
	@Test
	public void testPostProcess()
	{
		fail( "Not yet implemented" );
	}


	@Test
	public void testStringify()
	{
		fail( "Not yet implemented" );
	}
	*/

	@Test
	public void testTag() throws UCMException
	{
		Tag tag = (Tag) UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		
		assertNotNull( tag );
	}
	
	@Test
	public void testTag2() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:bn__1_2_3_1234@\\Cool_PVOB", true );
		Tag tag = bl.GetTag( "a", "1" );
		
		assertNotNull( tag );
		assertEquals( "a", tag.GetTagType() );
		assertEquals( "1", tag.GetTagID() );
		assertEquals( "v1", tag.GetEntry( "k1" ) );
		assertEquals( "v2", tag.GetEntry( "k2" ) );
	}

	@Test
	public void testCGIToHash()
	{
		String cgi = "k1=v1&k2=v2";
		Map<String, String> hash = Tag.CGIToHash( cgi );
		
		assertTrue( hash.containsKey( "k1" ) );
		assertTrue( hash.containsKey( "k2" ) );
		
		assertTrue( hash.get( "k1" ).equals( "v1" ) );
		assertTrue( hash.get( "k2" ).equals( "v2" ) );
	}

	/*
	@Test
	public void testQueryTag()
	{
		fail( "Not yet implemented" );
	}
	*/
	
	@Test
	public void testHashToCGIHashMapOfStringString()
	{
		Map<String, String> hash = new HashMap<String, String>();
		hash.put( "tagtype", "tt" );
		hash.put( "tagid", "001" );
		hash.put( "k1", "v1" );
		hash.put( "k2", "v2" );
		
		String cgi = Tag.HashToCGI( hash );
		
		assertEquals( "tagid=001&tagtype=tt&k1=v1&k2=v2", cgi );
	}

	@Test
	public void testHashToCGIHashMapOfStringStringBoolean()
	{
		Map<String, String> hash = new HashMap<String, String>();
		hash.put( "tagtype", "tt" );
		hash.put( "tagid", "001" );
		hash.put( "k1", "v1" );
		hash.put( "k2", "v2" );
		
		String cgi = Tag.HashToCGI( hash, true );

		assertEquals( "k1=v1&k2=v2", cgi );
	}

	@Test
	public void testSetKeyValue() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		
		tag.SetKeyValue( cgi );
	}

	@Test
	public void testSetEntry() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		
		tag.SetKeyValue( cgi );
		tag.SetEntry( "k3", "v3" );
		
		Map<String, String> hash = tag.GetEntries();
		
		assertTrue( hash.containsKey( "k1" ) );
		assertTrue( hash.containsKey( "k2" ) );
		assertTrue( hash.containsKey( "k3" ) );
		
		assertEquals( "v1", hash.get( "k1" ) );
		assertEquals( "v2", hash.get( "k2" ) );
		assertEquals( "v3", hash.get( "k3" ) );
	}

	@Test
	public void testRemoveEntry() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		
		tag.SetKeyValue( cgi );
		tag.RemoveEntry( "k2" );
		
		Map<String, String> hash = tag.GetEntries();
		
		assertTrue( hash.containsKey( "k1" ) );
		assertFalse( hash.containsKey( "k2" ) );
	}
	
	@Test
	public void testRemoveEntryFail() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		
		tag.SetKeyValue( cgi );
		tag.RemoveEntry( "k3" );
		
		Map<String, String> hash = tag.GetEntries();
		
		assertTrue( hash.containsKey( "k1" ) );
		assertTrue( hash.containsKey( "k2" ) );
		assertFalse( hash.containsKey( "k3" ) );
	}

	@Test
	public void testGetEntry() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		
		tag.SetKeyValue( cgi );

		String value = tag.GetEntry( "k1" );
		
		assertEquals( "v1", value );
	}
	
	@Test
	public void testGetEntryFail() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		
		tag.SetKeyValue( cgi );

		String value = tag.GetEntry( "k3" );
		
		assertNull( value );
	}


	@Test
	public void testGetEntries() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:bn__1_2_3_1234@\\Cool_PVOB", true );
		Tag tag = bl.GetTag( "a", "1" );
		
		assertNotNull( tag );
		
		Map<String, String> hash = tag.GetEntries();
		
		assertTrue( hash.containsKey( "k1" ) );
		assertTrue( hash.containsKey( "k2" ) );
		
		assertEquals( "v1", hash.get( "k1" ) );
		assertEquals( "v2", hash.get( "k2" ) );
	}

	@Test
	public void testSetTagEntity() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:bn__1_2_3_1234@\\Cool_PVOB", true );
		Tag tag     = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		tag.SetTagEntity( bl );
	}

	@Test
	public void testGetTagEntity() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:bn__1_2_3_1234@\\Cool_PVOB", true );
		Tag tag     = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		tag.SetTagEntity( bl );
		
		UCMEntity e = tag.GetTagEntity();
		
		assertTrue( bl.fqname.equals( e.GetFQName() ) );
	}

	@Test
	public void testPersist() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:tagtest@\\Cool_PVOB", true );
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		tag.SetTagEntity( bl );
		String cgi = "tagid=001&tagtype=tt&k1=v1&k2=v2";
		tag.SetKeyValue( cgi );
		tag.Persist();
	}

	@Test
	public void testSetCreated() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		tag.SetCreated( true );
	}

	@Test
	public void testIsCreated() throws UCMException
	{
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@123@\\Cool_PVOB", true );
		tag.SetCreated( true );
		
		assertTrue( tag.IsCreated() );
	}

	@Test
	public void testGetTagType() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:bn__1_2_3_1234@\\Cool_PVOB", true );
		Tag tag = bl.GetTag( "a", "1" );
		
		assertNotNull( tag );
		assertEquals( "a", tag.GetTagType() );
	}

	@Test
	public void testGetTagID() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:bn__1_2_3_1234@\\Cool_PVOB", true );
		Tag tag = bl.GetTag( "a", "1" );
		
		assertNotNull( tag );
		assertEquals( "1", tag.GetTagID() );
	}

}
