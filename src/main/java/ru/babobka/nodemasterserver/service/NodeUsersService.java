package ru.babobka.nodemasterserver.service;

import java.math.BigInteger;
import java.util.List;

import ru.babobka.nodemasterserver.model.User;

public interface NodeUsersService {

	List<User> getList();

	User get(String userName);

	boolean remove(String userName);

	boolean add(User user);

	boolean incrementTaskCount(String login);

	boolean update(String login, String newLogin, String password, String email, Integer taskCount);
	
	boolean auth(String login, String password);
	
	boolean auth(String login, BigInteger integerHashedPassword);
	

}