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

	private SlaveServer[] slaveServers;

	private int slaves = 3;

	private static MasterServer masterServer;

	@Before
	public void setUp() throws IOException {
		slaveServers=new SlaveServer[slaves];
		for (int i = 0; i < slaves; i++) {
			slaveServers[i] = new SlaveServer("localhost", ServerContext.getInstance().getConfig().getMainServerPort(),
					"test_user", "abc");
		}
	}

	@After
	public void tearDown() {
		for (int i = 0; i < slaves; i++) {
			slaveServers[i].interrupt();
		}
	}

	public void startSlaving() {
		for (int i = 0; i < slaves; i++) {
			slaveServers[i].start();
		}
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
		startSlaving();
		assertEquals(ServerContext.getInstance().getSlaves().getClusterSize(), slaves);
	}

}
