package ru.babobka.nodemasterserver.service;

import ru.babobka.nodemasterserver.dao.CacheDAO;
import ru.babobka.container.Container;
import ru.babobka.vsjws.model.HttpRequest;

public class CacheServiceImpl implements CacheService {

	private final CacheDAO cacheDAO = Container.getInstance()
			.get(CacheDAO.class);

	@Override
	public void putContent(HttpRequest request, String content) {
		cacheDAO.put(request.getUri() + request.getUrlParams(), content);

	}

	@Override
	public String getContent(HttpRequest request) {
		return cacheDAO.get(request.getUri() + request.getUrlParams());
	}

}
