package ru.babobka.nodemasterserver.service;

import java.math.BigInteger;
import java.util.List;

import ru.babobka.nodemasterserver.model.User;

public interface NodeUsersService {

	List<User> getList();

	User get(String userName);

	boolean remove(String userName);

	boolean add(User user);

	boolean addTestUser();

	boolean incrementTaskCount(String login);

	boolean update(String userLoginToUpdate, User user);

	boolean auth(String login, String password);

	boolean auth(String login, BigInteger integerHashedPassword);

}
