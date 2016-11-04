package ru.babobka.nodemasterserver.webcontroller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.nodemasterserver.builder.TestUserBuilder;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodemasterserver.server.ServerContext;
import ru.babobka.nodeslaveserver.server.SlaveServer;

public class TaskWebControllerTest {

	private static SlaveServer[] slaveServers;

	private static final int SLAVES = 5;

	private static final int N = 100;

	private static MasterServer masterServer;

	private static final String LOGIN = TestUserBuilder.LOGIN;

	private static final String PASSWORD = TestUserBuilder.PASSWORD;

	private static final String REST_LOGIN = ServerContext.getInstance().getConfig().getRestServiceLogin();

	private static final String REST_PASSWORD = ServerContext.getInstance().getConfig().getRestServicePassword();

	private static final String LOGIN_HEADER = "X-Login";

	private static final String PASSWORD_HEADER = "X-Password";

	private static final HttpClient httpClient = HttpClientBuilder.create().build();

	private static final int PORT = ServerContext.getInstance().getConfig().getWebPort();

	private static final String URL = "http://localhost:" + PORT + "/task";

	@BeforeClass
	public static void runMasterServer() throws IOException {
		masterServer = MasterServer.getInstance();
		masterServer.start();
	}

	@AfterClass
	public static void closeMasterServer() {

		if (masterServer != null)
			masterServer.interrupt();
		try {
			if (masterServer != null)
				masterServer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Before
	public void setUp() throws IOException {
		createSlaves();
		startSlaves();
	}

	@After
	public void tearDown() {
		closeSlaves();
	}

	@Test
	public void testTenPrimes() throws ClientProtocolException, IOException {

		for (int i = 0; i < N; i++) {
			JSONObject jsonObject = getPrimesInRange(0, 29);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 10);
		}
	}

	@Test
	public void testThousandPrimes() throws IOException {

		for (int i = 0; i < N; i++) {
			JSONObject jsonObject = getPrimesInRange(0, 7919);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 1000);
		}
	}

	@Test
	public void testTenThousandsPrimes() throws IOException {

		for (int i = 0; i < N; i++) {
			JSONObject jsonObject = getPrimesInRange(0, 104729);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 10000);
		}
	}

	public static void createSlaves() throws IOException {
		slaveServers = new SlaveServer[SLAVES];
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i] = new SlaveServer("localhost", ServerContext.getInstance().getConfig().getMainServerPort(),
					LOGIN, PASSWORD);
		}
	}

	public static void startSlaves() {
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i].start();
		}
	}

	public static void closeSlaves() {
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i].interrupt();
		}

		for (int i = 0; i < SLAVES; i++) {
			try {
				slaveServers[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private JSONObject getPrimesInRange(int begin, int end) throws ClientProtocolException, IOException {
		HttpGet get = null;
		try {
			String url = URL + "/" + URLEncoder.encode("Dummy prime counter", "UTF-8") + "?begin=" + begin + "&end="
					+ end + "&noCache=true";
			get = new HttpGet(url);
			setCredentialHeaders(get);
			HttpResponse response = httpClient.execute(get);
			return new JSONObject(new BasicResponseHandler().handleResponse(response));
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}

	private void setCredentialHeaders(HttpMessage httpMessage) {
		httpMessage.setHeader(LOGIN_HEADER, REST_LOGIN);
		httpMessage.setHeader(PASSWORD_HEADER, REST_PASSWORD);
	}

}
