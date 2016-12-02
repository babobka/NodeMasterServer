package ru.babobka.nodemasterserver.model;

import java.util.Arrays;

import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.InvalidUserException;
import ru.babobka.nodemasterserver.util.MathUtil;

/**
 * Created by dolgopolov.a on 29.10.15.
 */
public final class User {

	public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private String name;

	private byte[] hashedPassword;

	private int taskCount;

	private String email;

	public User() {
	}

	public User(String name, byte[] hashedPassword, int taskCount, String email) {
		if (name != null) {
			this.name = name;
		} else {
			throw new InvalidUserException("'name' must be set");
		}

		if (hashedPassword != null && hashedPassword.length > 0) {
			this.hashedPassword = hashedPassword;
		} else {
			throw new InvalidUserException("'password' must be set");
		}
		if (email != null) {
			if (email.matches(EMAIL_PATTERN)) {
				this.email = email;
			} else {
				throw new InvalidUserException("invalid email " + email);
			}
		} else {
			this.email = null;
		}

		if (taskCount < 0) {
			throw new InvalidUserException("'taskCount' is negative");
		}
		this.taskCount = taskCount;
	}

	public User(String name, String password, Integer taskCount, String email) {
		this(name, MathUtil.sha2(password), taskCount, email);
	}

	public User(JSONObject json) {

		if (!json.isNull("name")) {
			name = json.getString("name");
		}

		if (!json.isNull("password")) {
			hashedPassword = MathUtil.sha2(json.getString("password"));
		}
		if (!json.isNull("email")) {
			email = json.getString("email");
		}
		if (!json.isNull("taskCount")) {
			taskCount = json.getInt("taskCount");
		}

	}

	public String getEmail() {
		return email;
	}

	public byte[] getHashedPassword() {
		return hashedPassword;
	}

	public String getName() {
		return name;
	}

	public Integer getTaskCount() {
		return taskCount;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHashedPassword(byte[] hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void validate() {
		if (name == null) {
			throw new InvalidUserException("'name' must be set");
		}
		if (hashedPassword == null) {
			throw new InvalidUserException("'password' is null");
		} else if (hashedPassword.length <= 0) {
			throw new InvalidUserException("'password' must be set");
		}
		if (email == null) {
			throw new InvalidUserException("empty email");
		} else if (!email.matches(EMAIL_PATTERN)) {
			throw new InvalidUserException("invalid email " + email);
		}
		if (taskCount < 0) {
			throw new InvalidUserException("'taskCount' is negative");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + Arrays.hashCode(hashedPassword);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + taskCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (!Arrays.equals(hashedPassword, other.hashedPassword))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (taskCount != other.taskCount)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", hashedPassword=" + Arrays.toString(hashedPassword) + ", taskCount=" + taskCount
				+ ", email=" + email + "]";
	}
	
	
}
