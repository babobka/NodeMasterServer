package ru.babobka.nodemasterserver.service;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodeserials.RSA;

public class NodeUserServiceTest {

	private NodeUsersService userService = NodeUsersServiceImpl.getInstance();

	private final String userName = "test_user";

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
		assertTrue(users.isEmpty());
		userService.add(testUser);
		users = userService.getList();
		assertEquals(users.size(), 1);
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
		assertTrue(userService.update(testUser.getName(), null, null, null, testUser.getTaskCount() + 1));
		User user = userService.get(testUser.getName());
		assertEquals(oldTaskCount + 1, user.getTaskCount().intValue());
	}

	@Test
	public void testInvalidUpdate() {
		userService.add(testUser);
		assertFalse(userService.update(null, null, null, null, null));
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
