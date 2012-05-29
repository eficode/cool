package net.praqma.clearcase.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.BaselineTask;
import net.praqma.clearcase.util.setup.EnvironmentParser;
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
		
		assertEquals( "very_cool", value );
	}
	
	@Test
	public void testGetValueMultiple() throws ClearCaseException {
		XML xml = new XML( "root" );
		Element test = xml.addElement( "test" );
		test.setAttribute( "name", "very_${var}_but_${var}" );
		
		Element var = xml.addElement( "var" );
		var.setAttribute( "name", "var" );
		var.setAttribute( "value", "cool" );
		
		VariableTask vt = new VariableTask();
		BaselineTask bt = new BaselineTask();
		
		Context context = new Context();
		vt.parse( var, context );
		String value = bt.getValue( "name", test, context );
		
		assertEquals( "very_cool_but_cool", value );
	}
	
	@Test
	public void testGetValueMultipleDifferent() throws ClearCaseException {
		XML xml = new XML( "root" );
		Element test = xml.addElement( "test" );
		test.setAttribute( "name", "very_${var1}_but_${var2}" );
		
		Element var1 = xml.addElement( "var1" );
		var1.setAttribute( "name", "var1" );
		var1.setAttribute( "value", "cool" );
		
		Element var2 = xml.addElement( "var2" );
		var2.setAttribute( "name", "var2" );
		var2.setAttribute( "value", "hot" );
		
		VariableTask vt = new VariableTask();
		BaselineTask bt = new BaselineTask();
		
		Context context = new Context();
		vt.parse( var1, context );
		vt.parse( var2, context );
		String value = bt.getValue( "name", test, context );
		
		assertEquals( "very_cool_but_hot", value );
	}
	
	@Test
	public void testGetValueAdvanced() throws ClearCaseException {
		XML xml = new XML( "root" );
		Element test = xml.addElement( "test" );
		test.setAttribute( "name", "very_${var1}_but_${var2}___${var1}__${var1}--${var2}" );
		
		Element var1 = xml.addElement( "var1" );
		var1.setAttribute( "name", "var1" );
		var1.setAttribute( "value", "a very long sentence" );
		
		Element var2 = xml.addElement( "var2" );
		var2.setAttribute( "name", "var2" );
		var2.setAttribute( "value", "also a very long sentence" );
		
		VariableTask vt = new VariableTask();
		BaselineTask bt = new BaselineTask();
		
		Context context = new Context();
		vt.parse( var1, context );
		vt.parse( var2, context );
		String value = bt.getValue( "name", test, context );
		
		assertEquals( "very_a very long sentence_but_also a very long sentence___a very long sentence__a very long sentence--also a very long sentence", value );
	}
	
	@Test
	public void testPredefinedVariables() throws ClearCaseException, IOException, Exception {
		
		Map<String, String> variables = new HashMap<String, String>();

		variables.put( "pvobname", "pvob10101" );
		
		XML xml = new XML( "root" );
		Element var1 = xml.addElement( "var" );
		var1.setAttribute( "name", "pvobname" );
		var1.setAttribute( "value", "pvobabc" );
		
		EnvironmentParser p = new EnvironmentParser( xml.getXML() );
		Context c = p.parse( variables );
            		
		System.out.println( "C: " + c.variables );
		
		assertEquals( "pvob10101", c.variables.get( "pvobname" ).value );
	}
}
