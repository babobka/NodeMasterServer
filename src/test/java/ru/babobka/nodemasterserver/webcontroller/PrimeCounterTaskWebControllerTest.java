package ru.babobka.nodemasterserver.webcontroller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
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
import ru.babobka.nodemasterserver.server.MasterServerContext;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeslaveserver.server.SlaveServer;

public class PrimeCounterTaskWebControllerTest {

	static {
		MasterServerContext.setConfigPath(StreamUtil.getLocalResourcePath("master_config.json"));
	}

	private static SlaveServer[] slaveServers;

	private static final int SLAVES = 3;

	private static MasterServer masterServer;

	private static final String LOGIN = TestUserBuilder.LOGIN;

	private static final String PASSWORD = TestUserBuilder.PASSWORD;

	private static final String REST_LOGIN = MasterServerContext.getInstance().getConfig().getRestServiceLogin();

	private static final String REST_PASSWORD = MasterServerContext.getInstance().getConfig().getRestServicePassword();

	private static final String LOGIN_HEADER = "X-Login";

	private static final String PASSWORD_HEADER = "X-Password";

	private static final HttpClient httpClient = HttpClientBuilder.create().build();

	private static final int PORT = MasterServerContext.getInstance().getConfig().getWebPort();

	private static final String URL = "http://localhost:" + PORT + "/task";

	private static final String DUMMY_PRIME_COUNTER_TASK_NAME = "Dummy prime counter";

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
	public void testTenPrimes() {

		for (int i = 0; i < 5000; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 29);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 10);
		}
	}

	@Test
	public void testInvalidTask() {

		assertEquals(getPrimesInRangeHttpResponse(1_000_000, 500_000).getStatusLine().getStatusCode(), 400);

	}

	@Test
	public void testMassInvalidTasks() {
		for (int i = 0; i < 500; i++)
			assertEquals(getPrimesInRangeHttpResponse(1_000_000, 500_000).getStatusLine().getStatusCode(), 400);

	}

	@Test
	public void testMassDummyTasks() {
		for (int i = 0; i < 500; i++)
			assertEquals(getPrimesInRangeHttpResponse(1_000_000, 1_000_001).getStatusLine().getStatusCode(), 200);

	}

	@Test
	public void testThousandPrimes() {

		for (int i = 0; i < 500; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 7919);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 1000);
		}
	}

	@Test
	public void testInternalError() {
		closeSlaves();
		for (int i = 0; i < 500; i++) {
			assertEquals(getPrimesInRangeHttpResponse(0, 1_000_000).getStatusLine().getStatusCode(),
					ru.babobka.vsjws.model.HttpResponse.ResponseCode.INTERNAL_SERVER_ERROR.getCode());
		}
	}

	@Test
	public void testTenThousandsPrimes() {

		for (int i = 0; i < 50; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 104729);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 10000);
		}
	}

	@Test
	public void testMillionPrimes() throws IOException {

		for (int i = 0; i < 3; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 15_485_863);
			assertEquals(jsonObject.getJSONObject("resultMap").getInt("primeCount"), 1_000_000);
		}
	}

	public static void createSlaves() throws IOException {
		slaveServers = new SlaveServer[SLAVES];
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i] = new SlaveServer("localhost",
					MasterServerContext.getInstance().getConfig().getMainServerPort(), LOGIN, PASSWORD);
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

	private HttpResponse getPrimesInRangeHttpResponse(int begin, int end) {
		HttpGet get = null;
		try {
			String url = URL + "/" + URLEncoder.encode(DUMMY_PRIME_COUNTER_TASK_NAME, "UTF-8") + "?begin=" + begin
					+ "&end=" + end + "&noCache=true";
			get = new HttpGet(url);
			setCredentialHeaders(get);
			return httpClient.execute(get);

		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}

	private JSONObject getPrimesInRangeJson(int begin, int end) {

		HttpGet get = null;
		try {
			String url = URL + "/" + URLEncoder.encode(DUMMY_PRIME_COUNTER_TASK_NAME, "UTF-8") + "?begin=" + begin
					+ "&end=" + end + "&noCache=true";
			get = new HttpGet(url);
			setCredentialHeaders(get);
			return new JSONObject(new BasicResponseHandler().handleResponse(httpClient.execute(get)));

		} catch (Exception e) {
			throw new RuntimeException(e);

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
