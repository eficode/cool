package net.praqma.clearcase;

public class Region extends Cool {

    private Site site;
    private String name;
    
    public Region( String name, Site site ) {
	this.name = name;
	this.site = site;
    }
    
    public void getVobs() {
	site.getVobs(this);
    }
    
    public String getName() {
	return name;
    }
}
