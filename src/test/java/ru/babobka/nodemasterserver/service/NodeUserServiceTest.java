package ru.babobka.nodemasterserver.service;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeserials.RSA;
import ru.babobka.nodeslaveserver.server.SlaveServer;
import ru.babobka.nodeslaveserver.server.SlaveServerContext;

public class NodeUserServiceTest {

	static {
		MasterServerContext.setConfigPath(StreamUtil.getLocalResourcePath(MasterServer.class, "master_config.json"));
		SlaveServerContext.setConfigPath(StreamUtil.getLocalResourcePath(SlaveServer.class, "slave_config.json"));
	}

	private NodeUsersService userService = NodeUsersServiceImpl.getInstance();

	private final String userName = "bbk_test";

	private final String password = "123";

	private final User testUser = new User(userName, password, 0, "test@email.com");

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
		assertTrue(userService.update(testUser.getName(), new User(jsonObject)));
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
		BigInteger integerHashedPassword = RSA.bytesToHashedBigInteger(testUser.getHashedPassword());
		assertTrue(userService.auth(testUser.getName(), integerHashedPassword));
	}

	@Test
	public void testBadPasswordAuth() {
		userService.add(testUser);
		BigInteger integerHashedPassword = RSA.stringToBigInteger(password + "abc");
		assertFalse(userService.auth(testUser.getName(), integerHashedPassword));
	}

	@Test
	public void testBadLoginAuth() {
		userService.add(testUser);
		BigInteger integerHashedPassword = RSA.bytesToHashedBigInteger(testUser.getHashedPassword());
		assertFalse(userService.auth(testUser.getName() + "abc", integerHashedPassword));
	}

}
