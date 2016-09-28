package ru.babobka.nodeServer.model;

import java.util.Set;

public class AuthResult {

	private final boolean valid;

	private final String login;

	private final Set<String> taskSet = new ConcurrentHashSet<>();

	public AuthResult(boolean valid, String login, Set<String> taskSet) {
		this.valid = valid;
		this.login = login;
		if (taskSet != null) {
			this.taskSet.addAll(taskSet);
		}

	}

	public AuthResult(boolean valid) {
		this.valid = valid;
		this.login = null;
	}

	public String getLogin() {
		return login;
	}

	public Set<String> getTaskSet() {
		return taskSet;
	}

	public boolean isValid() {
		return valid;
	}

}
