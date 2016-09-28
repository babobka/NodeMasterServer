package ru.babobka.nodeServer.service;

import java.util.List;

import ru.babobka.nodeServer.exception.UserAlreadyExistsException;
import ru.babobka.nodeServer.model.User;

public interface NodeUsersService {

	List<User> getList();

	User get(String userName);

	boolean remove(String userName);

	boolean add(User user) throws UserAlreadyExistsException;

	boolean incrementTaskCount(String login);

	boolean update(String login, String newLogin, String password, String email, Integer taskCount);

}
