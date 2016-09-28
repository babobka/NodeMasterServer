package ru.babobka.nodeServer.dao;

import java.math.BigInteger;
import java.util.List;

import ru.babobka.nodeServer.model.User;

/**
 * Created by dolgopolov.a on 05.12.15.
 */
public interface NodeUsersDAO {

	User get(String login);

	List<User> getList();

	boolean add(User user);

	boolean exists(String login);

	boolean remove(String login);

	boolean update(String login, String newLogin, BigInteger hashedPassword, String email, Integer taskCount);

	boolean incrTaskCount(String login);

}
