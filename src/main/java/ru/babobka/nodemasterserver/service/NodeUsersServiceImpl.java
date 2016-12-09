package ru.babobka.nodemasterserver.service;

import java.math.BigInteger;
import java.util.List;

import ru.babobka.nodemasterserver.builder.TestUserBuilder;
import ru.babobka.nodemasterserver.dao.NodeUsersDAO;
import ru.babobka.nodemasterserver.dao.UsersDAOFactory;
import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.util.MathUtil;
import ru.babobka.nodeserials.RSA;

public class NodeUsersServiceImpl implements NodeUsersService {

	private final NodeUsersDAO userDAO = UsersDAOFactory
			.get(MasterServerContext.getConfig().isDebugDataBase());

	private static volatile NodeUsersServiceImpl instance;

	private NodeUsersServiceImpl() {

	}

	public static NodeUsersServiceImpl getInstance() {
		NodeUsersServiceImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (NodeUsersServiceImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new NodeUsersServiceImpl();
				}
			}
		}
		return localInstance;
	}

	@Override
	public List<User> getList() {
		return userDAO.getList();
	}

	@Override
	public User get(String userName) {
		return userDAO.get(userName);

	}

	@Override
	public boolean remove(String userName) {
		return userDAO.remove(userName);
	}

	@Override
	public synchronized boolean add(User user) {
		if (!userDAO.exists(user.getName())) {
			return userDAO.add(user);

		}
		return false;

	}

	@Override
	public boolean incrementTaskCount(String login) {
		return userDAO.incrTaskCount(login);
	}

	@Override
	public synchronized boolean update(String userLoginToUpdate, User user) {

		if (userLoginToUpdate != null && !userDAO.exists(user.getName())) {
			return userDAO.update(userLoginToUpdate, user);
		}

		return false;

	}

	@Override
	public boolean auth(String login, String password) {
		User user = get(login);
		if (user != null && java.util.Arrays.equals(user.getHashedPassword(), MathUtil.sha2(password))) {
			return true;
		}
		return false;

	}

	@Override
	public boolean auth(String login, BigInteger integerHashedPassword) {

		User user = get(login);
		if (user != null) {
			BigInteger userIntegerHashedPassword = RSA.bytesToHashedBigInteger(user.getHashedPassword());
			return userIntegerHashedPassword.equals(integerHashedPassword);
		}
		return false;
	}

	@Override
	public boolean addTestUser() {
		return add(TestUserBuilder.build());
	}

}
