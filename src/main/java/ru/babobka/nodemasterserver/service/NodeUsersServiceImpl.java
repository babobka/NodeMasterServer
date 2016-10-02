package ru.babobka.nodemasterserver.service;

import java.util.List;

import ru.babobka.nodemasterserver.dao.NodeUsersDAO;
import ru.babobka.nodemasterserver.dao.NodeUsersDAOImpl;
import ru.babobka.nodemasterserver.model.User;

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
	public boolean add(User user) {
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
	public boolean update(String login, String newLogin, String password, String email, Integer taskCount) {
		if (login != null) {
			return userDAO.update(login, newLogin, password, email, taskCount);
		}
		return false;

	}

}
