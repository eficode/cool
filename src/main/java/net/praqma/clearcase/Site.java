package net.praqma.clearcase;

public class Site extends Cool {
    
    private String name;
    
    public Site( String name ) {
	this.name = name;
    }
    
    public void getVobs( Region region ) {
	context.getVobs(region);
    }
}
