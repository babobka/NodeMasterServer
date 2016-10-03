package ru.babobka.nodemasterserver.model;

import org.json.JSONObject;

public class UserHttpEntity {

	private final String name;

	private final String password;

	private final Integer taskCount;

	private final String email;

	public UserHttpEntity(JSONObject jsonObject) {
		if (!jsonObject.isNull("name")) {
			this.name = jsonObject.getString("name");
		} else {
			this.name = null;
		}

		if (!jsonObject.isNull("password")) {
			this.password = jsonObject.getString("password");
		} else {
			this.password = null;
		}

		if (!jsonObject.isNull("email")) {
			this.email = jsonObject.getString("email");
		} else {
			this.email = null;
		}

		if (!jsonObject.isNull("taskCount")) {
			this.taskCount = jsonObject.getInt("taskCount");
		} else {
			this.taskCount = null;
		}
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public Integer getTaskCount() {
		return taskCount;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "UserHttpEntity [name=" + name + ", password=" + password + ", taskCount=" + taskCount + ", email="
				+ email + "]";
	}

}
