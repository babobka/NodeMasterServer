package ru.babobka.nodeServer.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

public class ServerTest {

	@After
	public void tearDown() {
		Server.getInstance().stop();
	}

	@Test
	public void testRun() {
		Server server = Server.getInstance();
		server.run();
		assertTrue(server.isRunning());
	}

	@Test
	public void testMultipleRunStop() {
		Server server = Server.getInstance();
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
		Server server = Server.getInstance();
		server.run();
		server.run();
		assertTrue(server.isRunning());
	}

	@Test
	public void testStop() {
		Server server = Server.getInstance();
		server.run();
		assertTrue(server.isRunning());
		assertFalse(server.isStopped());
		server.stop();
		assertTrue(server.isStopped());
		assertFalse(server.isRunning());
	}

}
