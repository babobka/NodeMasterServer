package ru.babobka.nodemasterserver.dao;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ru.babobka.nodemasterserver.model.User;

public class DebugNodeUsersDAOImpl implements NodeUsersDAO {



	private final Map<String, User> debugDataMap = new ConcurrentHashMap<>();

	@Override
	public User get(String login) {

		return debugDataMap.get(login);
	}

	@Override
	public List<User> getList() {
		List<User> users = new LinkedList<>();
		Set<Map.Entry<String, User>> userEntries = debugDataMap.entrySet();
		for (Map.Entry<String, User> userEntry : userEntries) {
			users.add(userEntry.getValue());
		}
		return users;
	}

	@Override
	public boolean add(User user) {
		debugDataMap.putIfAbsent(user.getName(), user);
		return true;
	}

	@Override
	public boolean exists(String login) {
		if (login == null) {
			return false;
		}
		return debugDataMap.containsKey(login);
	}

	@Override
	public boolean remove(String login) {

		return debugDataMap.remove(login) != null;
	}

	@Override
	public boolean update(String login, User userToUpdate) {
		User user = get(login);
		if (user != null) {
			if (userToUpdate.getEmail() != null) {
				user.setEmail(userToUpdate.getEmail());
			}
			if (userToUpdate.getHashedPassword() != null) {
				user.setHashedPassword(userToUpdate.getHashedPassword());
			}
			if (userToUpdate.getName() != null) {
				user.setName(userToUpdate.getName());
			}
			if (userToUpdate.getTaskCount() != null) {
				user.setTaskCount(userToUpdate.getTaskCount());
			}
			return true;

		}
		return false;
	}

	@Override
	public boolean incrTaskCount(String login) {
		User user = get(login);
		if (user != null) {
			synchronized (user) {
				user.setTaskCount(user.getTaskCount() + 1);
			}
			return true;
		}
		return false;
	}

}
