package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.*;

import net.praqma.clearcase.ucm.UCMException;

public class StreamTest {
	@BeforeClass
	public static void startup()
	{
		UCM.setContext( UCM.ContextType.CLEARTOOL );
	}

}
