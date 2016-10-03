package ru.babobka.nodemasterserver.webcontroller;


import ru.babobka.nodemasterserver.dao.CacheDAO;
import ru.babobka.nodemasterserver.dao.CacheDAOImpl;
import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.webcontroller.WebFilter;

public class CacheWebFilter implements WebFilter {

	private CacheDAO cacheDAO = CacheDAOImpl.getInstance();

	@Override
	public void afterFilter(HttpRequest request, HttpResponse response) {
		if (response.getResponseCode().equals(ResponseCode.OK)) {
			cacheDAO.put(request.getUri()+request.getUrlParams(), response.getContent());
		}
	}

	@Override
	public HttpResponse onFilter(HttpRequest request) {

		String noCache = request.getUrlParam("noCache");
		if (noCache != null && noCache.equals("true")) {
			return null;
		} else if (request.getMethod().equals(Method.GET)) {
			String cachedContent = cacheDAO.get(request.getUri()+request.getUrlParams());
			if (cachedContent != null) {
				return HttpResponse.textResponse(cachedContent, ContentType.JSON);
			}
		}
		return null;
	}
}
