package ru.babobka.nodeServer.webController;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.util.StreamUtil;
import ru.babobka.nodeServer.xml.XMLServerConfigData;
import ru.babobka.vsjws.model.HttpRequest;
import ru.babobka.vsjws.model.HttpResponse;
import ru.babobka.vsjws.webcontroller.WebController;

public class MainPageWebController extends WebController {

	@Override
	public HttpResponse onGet(HttpRequest request) throws IOException, JAXBException, URISyntaxException {
		JAXBContext context = JAXBContext.newInstance(XMLServerConfigData.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		m.marshal(Server.getConfigData().toXML(), sw);
		String xmlString = sw.toString();
		return HttpResponse.xsltResponse(xmlString, new StreamSource(
				StreamUtil.getRunningFolder() + File.separator + "web-content" + File.separator + "mainPage.xsl"));

	}
}
