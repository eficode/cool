package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Component;

import org.junit.Test;

public class UCMEntityTest {

	@Test
	public void testInitialize() throws UnableToInitializeEntityException {
		String c = "component:_System@\\ccucm-9-22339111_PVOB";
		Component.get( c );

        String c2 = "component:sødræv@\\ccucm-9-22339111_PVOB";
        Component comp2 = Component.get( c2 );

        String c3 = "component:_System@\\rød_PVOB";
        Component.get( c3 );
	}

    @Test( expected = UnableToInitializeEntityException.class )
    public void testInitializeFail() throws UnableToInitializeEntityException {
        String c2 = "component:sødræv@@@@@\\ccucm-9-22339111_PVOB";
        Component.get( c2 );
    }

    @Test( expected = UnableToInitializeEntityException.class )
    public void testInitializeFail2() throws UnableToInitializeEntityException {
        String c2 = "component:_System@\\coo l_PVOB";
        Component comp2 = Component.get( c2 );
        System.out.println( comp2 );
    }
}
