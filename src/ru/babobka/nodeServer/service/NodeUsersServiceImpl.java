package ru.babobka.nodeServer.service;

import java.math.BigInteger;
import java.util.List;

import ru.babobka.nodeServer.dao.NodeUsersDAO;
import ru.babobka.nodeServer.dao.NodeUsersDAOImpl;
import ru.babobka.nodeServer.exception.UserAlreadyExistsException;
import ru.babobka.nodeServer.model.User;
import ru.babobka.nodeServer.util.MathUtil;

public class NodeUsersServiceImpl implements NodeUsersService {

	private final NodeUsersDAO userDAO = NodeUsersDAOImpl.getInstance();

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
	public boolean add(User user) throws UserAlreadyExistsException {
		if (!userDAO.exists(user.getName())) {
			return userDAO.add(user);
		}
		throw new UserAlreadyExistsException();

	}

	@Override
	public boolean incrementTaskCount(String login) {
		return userDAO.incrTaskCount(login);
	}

	@Override
	public boolean update(String login, String newLogin, String password,
			String email, Integer taskCount) {
		return userDAO
				.update(login, newLogin,
						new BigInteger(MathUtil.sha2(password)).abs(), email,
						taskCount);
	}

}
