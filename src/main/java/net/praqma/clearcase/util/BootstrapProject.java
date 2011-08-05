package net.praqma.clearcase.util;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.view.DynamicView;

public class BootstrapProject {
	public static void main( String[] args ) throws UCMException {
		PVob pvob = PVob.create("\\TEST_PVOB", null, "PVOB for testing");
		Vob vob = Vob.create("\\TEST", null, "Vob for testing");
		vob.load();
		System.out.println("Path=" + vob.getStorageLocation());
		vob.mount();
		
		Component c = Component.create("test", pvob, null, "Test component");
		System.out.println("Component=" + c);
		
		DynamicView view = DynamicView.create("test_view", null);
	}
}
