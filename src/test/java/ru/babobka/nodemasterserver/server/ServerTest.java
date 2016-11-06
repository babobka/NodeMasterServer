package ru.babobka.nodemasterserver.server;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.nodemasterserver.builder.TestUserBuilder;
import ru.babobka.nodeslaveserver.server.SlaveServer;

public class ServerTest {

	private static SlaveServer[] slaveServers;

	private static final int SLAVES = 5;

	private static MasterServer masterServer;

	private static final String LOGIN = TestUserBuilder.LOGIN;

	private static final String PASSWORD = TestUserBuilder.PASSWORD;

	private static final int TESTS = 5;

	@BeforeClass
	public static void runMasterServer() throws IOException {
		masterServer = MasterServer.getInstance();
		masterServer.start();
	}

	@AfterClass
	public static void closeServers() {
		closeSlaves();
		closeMasterServer();

	}

	@Test
	public void logInMass() throws IOException {
		for (int i = 0; i < TESTS; i++) {
			createSlaves(SLAVES);
			startSlaves();
			assertEquals(ServerContext.getInstance().getSlaves().getClusterSize(), SLAVES);
			closeSlaves();
		}
	}

	@Test
	public void logInTooMuch() throws IOException {

		createSlaves(ServerContext.getInstance().getConfig().getMaxClients() + 10);
		startSlaves();
		assertEquals(ServerContext.getInstance().getSlaves().getClusterSize(), SLAVES);
		closeSlaves();

	}

	@Test
	public void logOutMass() throws IOException, InterruptedException {
		for (int i = 0; i < TESTS; i++) {
			createSlaves(SLAVES);
			startSlaves();
			closeSlaves();
			Thread.sleep(200);
			assertEquals(ServerContext.getInstance().getSlaves().getClusterSize(), 0);
		}
	}

	@Test
	public void logFailBadAddress() {

		try {
			new SlaveServer("localhost123", ServerContext.getInstance().getConfig().getMainServerPort(), "test_user",
					"abc");
			fail();
		} catch (IOException e) {

		}

	}

	@Test
	public void logFailBadPassword() {
		try {
			new SlaveServer("localhost", ServerContext.getInstance().getConfig().getMainServerPort(), LOGIN + "abc",
					PASSWORD);
			fail();
		} catch (IOException e) {

		}
	}

	public static void createSlaves(int size) throws IOException {
		slaveServers = new SlaveServer[size];
		for (int i = 0; i < size; i++) {
			slaveServers[i] = new SlaveServer("localhost", ServerContext.getInstance().getConfig().getMainServerPort(),
					LOGIN, PASSWORD);
		}
	}

	public static void startSlaves() {
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i].start();
		}
	}

	public static void closeSlaves() {
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i].interrupt();
		}

		for (int i = 0; i < SLAVES; i++) {
			try {
				slaveServers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static void closeMasterServer() {

		if (masterServer != null)
			masterServer.interrupt();
		try {
			if (masterServer != null)
				masterServer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
