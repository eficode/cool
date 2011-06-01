package net.praqma.clearcase.ucm.entities;

import net.praqma.clearcase.ucm.UCMException;

public class Project extends UCMEntity {
    /* Project specific fields */
    private Stream stream = null;

    Project() {
    }

    /**
     * This method is only available to the package, because only UCMEntity
     * should be allowed to call it.
     * 
     * @return A new Project Entity
     */
    static Project getEntity() {
	return new Project();
    }

    /* For now, the project implements the Plevel functionality */
    public enum Plevel {
	REJECTED, INITIAL, BUILT, TESTED, RELEASED;
    }

    /**
     * Given a String, return the corresponding Promotion Level.
     * 
     * @param str
     *            , if not a valid Promotion Level INITAL is returned.
     * @return A Promotion Level
     */
    public static Plevel getPlevelFromString(String str) {
	Plevel plevel = Plevel.INITIAL;

	try {
	    plevel = Plevel.valueOf(str);
	} catch (Exception e) {
	    /* Do nothing... */
	}

	return plevel;
    }

    public static Plevel promoteFrom(Plevel plevel) {
	switch (plevel) {
	case INITIAL:
	    plevel = Plevel.BUILT;
	    break;
	case BUILT:
	    plevel = Plevel.TESTED;
	    break;
	case TESTED:
	    plevel = Plevel.RELEASED;
	    break;
	case RELEASED:
	    plevel = Plevel.RELEASED;
	    break;
	}

	return plevel;
    }

    public void load() throws UCMException {
	context.loadProject(this);
    }

    public void setStream(Stream stream) {
	this.stream = stream;
    }

    public Stream getIntegrationStream() throws UCMException {
	if (!this.loaded)
	    load();
	return stream;
    }
}
