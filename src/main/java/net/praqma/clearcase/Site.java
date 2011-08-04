package net.praqma.clearcase;

import java.util.List;

public class Site extends Cool {
    
    private String name;
    
    public Site( String name ) {
	this.name = name;
    }
    
    public List<PVob> getVobs( Region region ) {
	return context.getVobs(region);
    }
}
