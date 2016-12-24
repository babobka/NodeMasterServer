package ru.babobka.nodemasterserver.service;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodemasterserver.server.MasterServerContainerStrategy;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeserials.RSA;
import ru.babobka.nodeslaveserver.server.SlaveServer;
import ru.babobka.nodeslaveserver.server.SlaveServerContainerStrategy;


public class NodeUserServiceTest {

	static {
		new MasterServerContainerStrategy(StreamUtil.getLocalResource(
				MasterServer.class, MasterServer.MASTER_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
		new SlaveServerContainerStrategy(StreamUtil.getLocalResource(
				SlaveServer.class, SlaveServer.SLAVE_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
	}

	private NodeUsersService userService = Container.getInstance()
			.get(NodeUsersService.class);

	private static final String USER_NAME = "bbk_test";

	private static final String PASSWORD = "123";

	private final User testUser = new User(USER_NAME, PASSWORD, 0,
			"test@email.com");

	@After
	public void tearDown() {
		userService.remove(testUser.getName());
	}

	@Test
	public void testAdd() {
		assertTrue(userService.add(testUser));
		assertNotNull(userService.get(testUser.getName()));
	}

	@Test
	public void testDoubleAdd() {
		assertTrue(userService.add(testUser));
		assertFalse(userService.add(testUser));
	}

	@Test
	public void testRemove() {
		userService.add(testUser);
		assertTrue(userService.remove(testUser.getName()));
		assertNull(userService.get(testUser.getName()));
	}

	@Test
	public void testList() {
		List<User> users = userService.getList();
		int oldSize = users.size();
		userService.add(testUser);
		users = userService.getList();
		assertEquals(oldSize + 1, users.size());
	}

	@Test
	public void testGet() {
		userService.add(testUser);
		User user = userService.get(testUser.getName());
		assertEquals(user, testUser);
	}

	@Test
	public void testUpdate() {
		int oldTaskCount = testUser.getTaskCount();
		userService.add(testUser);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("taskCount", testUser.getTaskCount() + 1);
		assertTrue(
				userService.update(testUser.getName(), new User(jsonObject)));
		User user = userService.get(testUser.getName());
		assertEquals(oldTaskCount + 1, user.getTaskCount().intValue());
	}

	@Test
	public void testInvalidUpdate() {
		userService.add(testUser);
		assertFalse(userService.update(null, new User(new JSONObject())));
	}

	@Test
	public void testAuth() {
		userService.add(testUser);
		BigInteger integerHashedPassword = RSA
				.bytesToHashedBigInteger(testUser.getHashedPassword());
		assertTrue(userService.auth(testUser.getName(), integerHashedPassword));
	}

	@Test
	public void testBadPasswordAuth() {
		userService.add(testUser);
		BigInteger integerHashedPassword = RSA
				.stringToBigInteger(PASSWORD + "abc");
		assertFalse(
				userService.auth(testUser.getName(), integerHashedPassword));
	}

	@Test
	public void testBadLoginAuth() {
		userService.add(testUser);
		BigInteger integerHashedPassword = RSA
				.bytesToHashedBigInteger(testUser.getHashedPassword());
		assertFalse(userService.auth(testUser.getName() + "abc",
				integerHashedPassword));
	}

}
