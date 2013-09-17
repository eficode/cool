package net.praqma.clearcase.ucm.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Changeset implements Serializable {
	public ArrayList<Version> versions = new ArrayList<Version>();
}
