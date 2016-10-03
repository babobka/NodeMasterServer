package ru.babobka.nodemasterserver.model;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodemasterserver.exception.InvalidUserException;
import ru.babobka.nodemasterserver.util.MathUtil;

/**
 * Created by dolgopolov.a on 29.10.15.
 */
public class User {

	public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private final String name;

	private final byte[] hashedPassword;

	private final int taskCount;

	private final String email;

	private final Integer id;

	public User(String name, byte[] hashedPassword, int taskCount, String email) throws InvalidUserException {
		super();
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

		this.id = hash(this.name);
	}

	public User(String name, String password, Integer taskCount, String email) throws InvalidUserException {
		this(name, MathUtil.sha2(password), taskCount, email);
	}

	public static User fromJson(JSONObject json) throws InvalidUserException {
		try {
			String name = null, password = null, email = null;
			Integer taskCount = null;
			if (!json.isNull("name")) {
				name = json.getString("name");
			}

			if (!json.isNull("password")) {
				password = json.getString("password");
			}
			if (!json.isNull("email")) {
				email = json.getString("email");
			}
			if (!json.isNull("taskCount")) {
				taskCount = json.getInt("taskCount");
			}

			return new User(name, password, taskCount, email);

		} catch (JSONException e) {
			throw new InvalidUserException("Invalid json structure", e);
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

	public Integer getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + Arrays.hashCode(hashedPassword);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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

	private static int hash(String s) {
		char[] chars = s.toCharArray();
		int hash = 31;
		for (int i = 0; i < chars.length; i++) {
			hash *= (int) chars[i];
		}
		hash += chars.length;
		return hash;
	}

}
