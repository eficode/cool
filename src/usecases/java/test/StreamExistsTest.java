package test;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;

public class StreamExistsTest
{
	public static void main( String[] args ) throws UCMException
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		String fqname = "stream:he j@\\med_dig";
		
		Stream.StreamExists( fqname );
	}
}
