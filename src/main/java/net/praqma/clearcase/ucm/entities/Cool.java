package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public abstract class Cool
{
	public static Logger logger = PraqmaLogger.getLogger( false );
	public static void setLogger( Logger logger )
	{
		Cool.logger = PraqmaLogger.getLogger( logger );
		Cleartool.setCLILogger( Cool.logger );
	}
	
	public static List<String> getPromotionLevels(){
		
		List<String> retval = new ArrayList<String>();
		for (Plevel plevel : Plevel.values()) {
			retval.add(plevel.toString());
		}
		return retval;
	}
}
