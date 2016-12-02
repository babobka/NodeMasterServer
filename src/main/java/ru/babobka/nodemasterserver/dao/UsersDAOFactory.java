package ru.babobka.nodemasterserver.dao;

public interface UsersDAOFactory {

	public static NodeUsersDAO get(boolean debug) {
		if (debug) {
			return DebugNodeUsersDAOImpl.getInstance();
		} else {
			return NodeUsersDAOImpl.getInstance();
		}
	}

}
