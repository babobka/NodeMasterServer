package ru.babobka.nodeServer.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

public class ServerTest {

	@After
	public void tearDown() {
		MasterServer.getInstance().stop();
	}

	@Test
	public void testRun() {
		MasterServer server = MasterServer.getInstance();
		server.run();
		assertTrue(server.isRunning());
	}

	@Test
	public void testMultipleRunStop() {
		MasterServer server = MasterServer.getInstance();
		for (int i = 0; i < 5; i++) {
			server.run();
			assertTrue(server.isRunning());
			assertFalse(server.isStopped());
			server.stop();
			assertTrue(server.isStopped());
			assertFalse(server.isRunning());
		}
	}

	@Test
	public void testDoubleRun() {
		MasterServer server = MasterServer.getInstance();
		server.run();
		server.run();
		assertTrue(server.isRunning());
	}

	@Test
	public void testStop() {
		MasterServer server = MasterServer.getInstance();
		server.run();
		assertTrue(server.isRunning());
		assertFalse(server.isStopped());
		server.stop();
		assertTrue(server.isStopped());
		assertFalse(server.isRunning());
	}

}
