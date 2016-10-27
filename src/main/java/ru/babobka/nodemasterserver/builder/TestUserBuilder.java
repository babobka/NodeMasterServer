package ru.babobka.nodemasterserver.builder;

import ru.babobka.nodemasterserver.model.User;

public interface TestUserBuilder {

	String LOGIN = "test_user";

	String PASSWORD = "abc";

	public static User build() {
		return new User(LOGIN, PASSWORD, 0, "foo@bar.com");
	}
}
