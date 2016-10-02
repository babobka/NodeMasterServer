package ru.babobka.nodeServer.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

public class ServerTest {

	private final int tests = 15;

	private final ServerExecutor serverExecutor = new ServerExecutor(MasterServer.getInstance());

	@After
	public void tearDown() {
		serverExecutor.stop();
	}

	@Test
	public void testRun() {

		serverExecutor.run();
		assertTrue(serverExecutor.isRunning());
	}

	@Test
	public void testMultipleRunStop() {

		for (int i = 0; i < tests; i++) {
			serverExecutor.run();
			assertTrue(serverExecutor.isRunning());
			assertFalse(serverExecutor.isStopped());
			serverExecutor.stop();
			assertTrue(serverExecutor.isStopped());
			assertFalse(serverExecutor.isRunning());
		}
	}

	@Test
	public void testDoubleRun() {

		serverExecutor.run();
		serverExecutor.run();
		assertTrue(serverExecutor.isRunning());
	}

	@Test
	public void testStop() {

		serverExecutor.run();
		assertTrue(serverExecutor.isRunning());
		assertFalse(serverExecutor.isStopped());
		serverExecutor.stop();
		assertTrue(serverExecutor.isStopped());
		assertFalse(serverExecutor.isRunning());
	}

}
