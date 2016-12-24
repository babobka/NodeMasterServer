package ru.babobka.nodemasterserver.webcontroller;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.service.CacheService;
import ru.babobka.vsjws.constant.ContentType;
import ru.babobka.vsjws.constant.Method;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.model.HttpResponse.ResponseCode;
import ru.babobka.vsjws.webcontroller.WebFilter;

public class CacheWebFilter implements WebFilter {

	private CacheService cacheService = Container.getInstance()
			.get(CacheService.class);

	@Override
	public void afterFilter(HttpRequest request, HttpResponse response) {
		if (response.getResponseCode() == ResponseCode.OK) {
			cacheService.putContent(request, new String(response.getContent()));
		}
	}

	@Override
	public HttpResponse onFilter(HttpRequest request) {

		String noCache = request.getUrlParam("noCache");
		if (noCache != null && noCache.equals("true")) {
			return null;
		} else if (request.getMethod().equals(Method.GET)) {
			String cachedContent = cacheService.getContent(request);
			if (cachedContent != null) {
				return HttpResponse.textResponse(cachedContent,
						ContentType.JSON);
			}
		}
		return null;
	}
}
