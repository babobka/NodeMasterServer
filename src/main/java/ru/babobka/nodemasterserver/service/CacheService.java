package ru.babobka.nodemasterserver.service;

import ru.babobka.vsjws.model.HttpRequest;

public interface CacheService {

	void putContent(HttpRequest request, String content);

	String getContent(HttpRequest request);

}
