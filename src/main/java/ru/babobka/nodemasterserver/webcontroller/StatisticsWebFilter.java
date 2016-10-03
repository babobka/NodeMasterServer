package ru.babobka.nodemasterserver.webcontroller;

import ru.babobka.nodemasterserver.dao.StatisticsDAOImpl;
import ru.babobka.nodemasterserver.dao.StatisticsDAO;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebFilter;

public class StatisticsWebFilter implements WebFilter{

	private StatisticsDAO statisticsDAO=StatisticsDAOImpl.getInstance();
	
	@Override
	public void afterFilter(HttpRequest arg0, HttpResponse arg1) {
		statisticsDAO.incrementRequests();	
	}

	@Override
	public HttpResponse onFilter(HttpRequest arg0) {		
		return null;
	}

}
