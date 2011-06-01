package net.praqma.clearcase;

import java.util.List;

public class Region extends Cool {

    private Site site;
    private String name;
    
    public Region( String name, Site site ) {
	this.name = name;
	this.site = site;
    }
    
    public List<Vob> getVobs() {
	return site.getVobs(this);
    }
    
    public String getName() {
	return name;
    }
}
