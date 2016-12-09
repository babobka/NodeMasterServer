package ru.babobka.nodemasterserver.service;

import ru.babobka.nodemasterserver.dao.CacheDAO;
import ru.babobka.nodemasterserver.dao.CacheDAOFactory;
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.vsjws.model.HttpRequest;

public class CacheServiceImpl implements CacheService {

	private final CacheDAO cacheDAO = CacheDAOFactory.get(MasterServerContext.getConfig().isDebugDataBase());

	private static volatile CacheServiceImpl instance;

	private CacheServiceImpl() {

	}

	public static CacheServiceImpl getInstance() {
		CacheServiceImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (CacheServiceImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new CacheServiceImpl();
				}
			}
		}
		return localInstance;
	}

	@Override
	public void putContent(HttpRequest request, String content) {
		cacheDAO.put(request.getUri() + request.getUrlParams(), content);

	}

	@Override
	public String getContent(HttpRequest request) {
		return cacheDAO.get(request.getUri() + request.getUrlParams());
	}

}
