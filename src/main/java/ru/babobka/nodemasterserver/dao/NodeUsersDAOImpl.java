package ru.babobka.nodemasterserver.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import ru.babobka.nodemasterserver.datasource.RedisDatasource;
import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.server.MasterServerContext;

class NodeUsersDAOImpl implements NodeUsersDAO {

	private static volatile NodeUsersDAOImpl instance;

	private static final String USERS_KEY = "users:";

	private static final String USER_KEY = "user:";

	private static final byte[] EMAIL = "email".getBytes();

	private static final byte[] HASHED_PASSWORD = "hashed_password".getBytes();

	private static final byte[] TASK_COUNT = "task_count".getBytes();

	private NodeUsersDAOImpl() {

	}

	public static NodeUsersDAOImpl getInstance() {
		NodeUsersDAOImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (NodeUsersDAOImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new NodeUsersDAOImpl();
				}
			}
		}
		return localInstance;
	}

	private Integer getUserId(String login) {

		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource();) {
			String value = jedis.hget(USERS_KEY, login);
			if (value != null) {
				return Integer.parseInt(value);
			}
		}
		return null;
	}

	private User get(String login, int id) {
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource();) {
			Map<byte[], byte[]> map = jedis.hgetAll((USER_KEY + id).getBytes());
			String email = null;
			if (map.get(EMAIL) != null) {
				email = new String(map.get(EMAIL));
			}
			int taskCount = 0;
			if (map.get(TASK_COUNT) != null) {
				taskCount = Integer.parseInt(new String(map.get(TASK_COUNT)));
			}
			byte[] hashedPassword = map.get(HASHED_PASSWORD);

			return new User(login, hashedPassword, taskCount, email);
		}
	}

	@Override
	public User get(String login) {
		Integer userId = getUserId(login);
		if (userId != null) {
			return get(login, userId);
		}
		return null;

	}

	@Override
	public List<User> getList() {
		List<User> users = new ArrayList<>();
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource();) {
			Map<String, String> map = jedis.hgetAll(USERS_KEY);
			for (Map.Entry<String, String> entry : map.entrySet()) {
				users.add(get(entry.getKey(), Integer.parseInt(entry.getValue())));
			}
		}
		return users;
	}

	@Override
	public boolean add(User user) {
		Transaction t = null;
		Jedis jedis = null;
		try {
			jedis = RedisDatasource.getInstance().getPool().getResource();
			long usersCount = jedis.incr("users_count:");
			Map<byte[], byte[]> userMap = new HashMap<>();
			if (user.getEmail() != null) {
				userMap.put(EMAIL, user.getEmail().getBytes());
			}
			userMap.put(HASHED_PASSWORD, user.getHashedPassword());
			if (user.getTaskCount() != null) {
				userMap.put(TASK_COUNT, String.valueOf(user.getTaskCount()).getBytes());
			}
			Map<String, String> loginIdMap = new HashMap<>();
			loginIdMap.put(user.getName(), String.valueOf(usersCount));
			t = jedis.multi();
			t.hmset(USERS_KEY, loginIdMap);
			t.hmset((USER_KEY + usersCount).getBytes(), userMap);
			t.exec();
			return true;
		} catch (Exception e) {
			MasterServerContext.getInstance().getLogger().log(e);
			return false;
		} finally {
			if (t != null) {
				try {
					t.close();
				} catch (IOException e) {
					MasterServerContext.getInstance().getLogger().log(e);
				}
			}
			if (jedis != null) {
				jedis.close();
			}
		}

	}

	@Override
	public boolean remove(String login) {

		Transaction t = null;
		Jedis jedis = null;
		try {
			jedis = RedisDatasource.getInstance().getPool().getResource();
			Integer userId = getUserId(login);
			if (userId != null) {
				t = jedis.multi();
				t.hdel(USERS_KEY, login);
				t.del(USER_KEY + userId);
				t.exec();
				return true;
			}
			return false;
		} catch (Exception e) {
			MasterServerContext.getInstance().getLogger().log(e);
			return false;
		} finally {
			if (t != null) {
				try {
					t.close();
				} catch (IOException e) {
					MasterServerContext.getInstance().getLogger().log(e);
				}
			}
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	@Override
	public boolean update(String login, User user) {
		Jedis jedis = null;
		Transaction t = null;
		try {
			jedis = RedisDatasource.getInstance().getPool().getResource();
			Integer userId = getUserId(login);
			t = jedis.multi();
			if (userId != null) {
				if (user.getName() != null && !user.getName().equals(login)) {
					t.hdel(USERS_KEY, login);
					t.hset(USERS_KEY, login, String.valueOf(userId));
				}
				Map<byte[], byte[]> map = new HashMap<>();
				if (user.getEmail() != null) {
					map.put(EMAIL, user.getEmail().getBytes());
				}
				if (user.getHashedPassword() != null) {
					map.put(HASHED_PASSWORD, user.getHashedPassword());
				}
				if (user.getTaskCount() != null) {
					map.put(TASK_COUNT, String.valueOf(user.getTaskCount()).getBytes());
				}
				t.hmset((USER_KEY + userId).getBytes(), map);
				t.exec();
				return true;
			}

		} catch (Exception e) {
			MasterServerContext.getInstance().getLogger().log(e);
		} finally {
			if (t != null) {
				try {
					t.close();
				} catch (IOException e) {
					MasterServerContext.getInstance().getLogger().log(e);
				}
			}
			if (jedis != null) {
				jedis.close();
			}
		}
		return false;

	}

	@Override
	public boolean incrTaskCount(String login) {
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource();) {
			Integer userId = getUserId(login);
			if (userId != null) {
				jedis.hincrBy(USER_KEY + userId, new String(TASK_COUNT), 1L);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean exists(String login) {
		if (login == null)
			return false;
		try (Jedis jedis = RedisDatasource.getInstance().getPool().getResource();) {
			Integer userId = getUserId(login);
			if (userId != null) {
				return true;
			}
		}
		return false;
	}

}
