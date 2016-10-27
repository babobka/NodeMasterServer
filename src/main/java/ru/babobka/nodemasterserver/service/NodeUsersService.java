package ru.babobka.nodemasterserver.service;

import java.math.BigInteger;
import java.util.List;

import ru.babobka.nodemasterserver.model.User;
import ru.babobka.nodemasterserver.model.UserHttpEntity;

public interface NodeUsersService {

	List<User> getList();

	User get(String userName);

	boolean remove(String userName);

	boolean add(User user);
	
	boolean addTestUser();

	boolean incrementTaskCount(String login);

	boolean update(String userLoginToUpdate, UserHttpEntity userHttpEntity);

	boolean auth(String login, String password);

	boolean auth(String login, BigInteger integerHashedPassword);

}
