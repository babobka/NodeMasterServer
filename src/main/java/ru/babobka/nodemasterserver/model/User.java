package ru.babobka.nodemasterserver.model;

import java.math.BigInteger;

import org.json.JSONException;
import org.json.JSONObject;

import ru.babobka.nodemasterserver.constant.RegularPatterns;
import ru.babobka.nodemasterserver.exception.InvalidUserException;
import ru.babobka.nodemasterserver.util.MathUtil;

/**
 * Created by dolgopolov.a on 29.10.15.
 */
public class User {

	private final String name;

	private final BigInteger hashedPassword;

	private final Integer taskCount;

	private final String email;

	private final Integer id;

	public User(String name, BigInteger password, Integer taskCount,
			String email) throws InvalidUserException {
		super();
		if (name != null) {
			this.name = name;
		} else {
			throw new InvalidUserException("'name' must be set");
		}

		if (password != null) {
			this.hashedPassword = password.abs();
		} else {
			throw new InvalidUserException("'password' must be set");
		}
		if (email != null) {
			if (!email.matches(RegularPatterns.EMAIL)) {
				throw new InvalidUserException("'email' is not valid");
			}
			this.email = email;
		} else {
			this.email = null;
		}
		if (taskCount != null) {
			if (taskCount < 0) {
				throw new InvalidUserException("'taskCount' is negative");
			}
			this.taskCount = taskCount;
		} else {
			this.taskCount = null;
		}
		this.id = hash(this.name);
	}
	
	public User(String name, String password, Integer taskCount,
			String email) throws InvalidUserException{
		this(name, new BigInteger(MathUtil.sha2(password)),taskCount, email);
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

	public BigInteger getHashedPassword() {
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
