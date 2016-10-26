package ru.babobka.nodemasterserver.server;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.nodeslaveserver.server.SlaveServer;

public class ServerTest {

	private SlaveServer slaveServer;

	private static MasterServer masterServer;

	@Before
	public void setUp() {
		slaveServer = new SlaveServer("localhost", ServerContext.getInstance().getConfig().getMainServerPort(), "bbk",
				"abc");
	}

	@After
	public void tearDown() {
		slaveServer.interrupt();
	}

	@AfterClass
	public static void closeMasterServer() {
		if (masterServer != null)
			masterServer.interrupt();
	}

	@BeforeClass
	public static void runMasterServer() throws IOException {
		masterServer = MasterServer.getInstance();
		masterServer.start();
	}

	@Test
	public void logIn() throws InterruptedException {
		slaveServer.start();
		Thread.sleep(1000);
		assertEquals(ServerContext.getInstance().getSlaves().getClusterSize(), 1);
	}

}
