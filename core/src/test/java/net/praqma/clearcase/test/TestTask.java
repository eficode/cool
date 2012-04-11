package net.praqma.clearcase.test;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.BaselineTask;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.clearcase.util.setup.VariableTask;
import net.praqma.util.xml.XML;

import org.junit.Test;
import org.w3c.dom.Element;

import junit.framework.TestCase;

public class TestTask extends TestCase {
	@Test
	public void testGetValueNone() {
		XML xml = new XML( "root" );
		xml.getRoot().setAttribute( "name", "namewithout" );
		BaselineTask bt = new BaselineTask();
		
		Context context = new Context();
		String value = bt.getValue( "name", xml.getRoot(), context );
		
		assertEquals( "namewithout", value );
	}
	
	@Test
	public void testGetValueFull() throws ClearCaseException {
		XML xml = new XML( "root" );
		Element test = xml.addElement( "test" );
		test.setAttribute( "name", "${var}" );
		
		Element var = xml.addElement( "var" );
		var.setAttribute( "name", "var" );
		var.setAttribute( "value", "cool" );
		
		VariableTask vt = new VariableTask();
		BaselineTask bt = new BaselineTask();
		
		Context context = new Context();
		vt.parse( var, context );
		String value = bt.getValue( "name", test, context );
		
		assertEquals( "cool", value );
	}
	
	@Test
	public void testGetValueMargin() throws ClearCaseException {
		XML xml = new XML( "root" );
		Element test = xml.addElement( "test" );
		test.setAttribute( "name", "very_${var}" );
		
		Element var = xml.addElement( "var" );
		var.setAttribute( "name", "var" );
		var.setAttribute( "value", "cool" );
		
		VariableTask vt = new VariableTask();
		BaselineTask bt = new BaselineTask();
		
		Context context = new Context();
		vt.parse( var, context );
		String value = bt.getValue( "name", test, context );
		System.out.println( "VALUE: " + value );
		System.out.println( "VALUE: " + context.variables );
		
		System.out.println( "VALUE: " + xml.getXML() );
		
		assertEquals( "very_cool", value );
	}
}
