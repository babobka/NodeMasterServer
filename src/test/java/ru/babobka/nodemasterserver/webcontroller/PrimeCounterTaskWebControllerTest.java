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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.babobka.nodemasterserver.builder.TestUserBuilder;
import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.server.MasterServer;
import ru.babobka.nodemasterserver.server.MasterServerConfig;
import ru.babobka.nodemasterserver.server.MasterServerContainerStrategy;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeslaveserver.server.SlaveServer;
import ru.babobka.nodeslaveserver.server.SlaveServerContainerStrategy;

public class PrimeCounterTaskWebControllerTest {

	static {
		new MasterServerContainerStrategy(StreamUtil.getLocalResource(
				MasterServer.class, MasterServer.MASTER_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
		new SlaveServerContainerStrategy(StreamUtil.getLocalResource(
				SlaveServer.class, SlaveServer.SLAVE_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
	}

	private static SlaveServer[] slaveServers;

	private static final int SLAVES = 3;

	private static MasterServer masterServer;

	private static final String LOGIN = TestUserBuilder.LOGIN;

	private static final String PASSWORD = TestUserBuilder.PASSWORD;

	private static final MasterServerConfig config = Container.getInstance()
			.get(MasterServerConfig.class);

	private static final String REST_LOGIN = config.getRestServiceLogin();

	private static final String REST_PASSWORD = config.getRestServicePassword();

	private static final String LOGIN_HEADER = "X-Login";

	private static final String PASSWORD_HEADER = "X-Password";

	private static final HttpClient httpClient = HttpClientBuilder.create()
			.build();

	private static final int PORT = config.getWebPort();

	private static final String URL = "http://localhost:" + PORT + "/task";

	private static final String DUMMY_PRIME_COUNTER_TASK_NAME = "Dummy prime counter";

	@BeforeClass
	public static void runServers() throws IOException, InterruptedException {
		masterServer = new MasterServer();
		masterServer.start();
		createSlaves();
		startSlaves();
	}

	@AfterClass
	public static void closeServers() throws InterruptedException {

		if (masterServer != null)
			masterServer.interrupt();
		try {
			if (masterServer != null)
				masterServer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		closeSlaves();

	}

	@Test
	public void testTenPrimes() {

		for (int i = 0; i < 5000; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 29);
			assertEquals(
					jsonObject.getJSONObject("resultMap").getInt("primeCount"),
					10);
		}
	}

	@Test
	public void testInvalidTask() {

		assertEquals(getPrimesInRangeHttpResponse(1_000_000, 500_000)
				.getStatusLine().getStatusCode(), 400);

	}

	@Test
	public void testMassInvalidTasks() {
		for (int i = 0; i < 500; i++)
			assertEquals(getPrimesInRangeHttpResponse(1_000_000, 500_000)
					.getStatusLine().getStatusCode(), 400);

	}

	@Test
	public void testMassDummyTasks() {
		for (int i = 0; i < 500; i++)
			assertEquals(getPrimesInRangeHttpResponse(1_000_000, 1_000_001)
					.getStatusLine().getStatusCode(), 200);

	}

	@Test
	public void testThousandPrimes() {

		for (int i = 0; i < 500; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 7919);
			assertEquals(
					jsonObject.getJSONObject("resultMap").getInt("primeCount"),
					1000);
		}
	}

	@Test
	public void testTenThousandsPrimes() {

		for (int i = 0; i < 50; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 104729);
			assertEquals(
					jsonObject.getJSONObject("resultMap").getInt("primeCount"),
					10000);
		}
	}

	@Test
	public void testMillionPrimes() throws IOException {

		for (int i = 0; i < 10; i++) {
			JSONObject jsonObject = getPrimesInRangeJson(0, 15_485_863);
			assertEquals(
					jsonObject.getJSONObject("resultMap").getInt("primeCount"),
					1_000_000);
		}
	}

	public static void createSlaves() throws IOException {
		slaveServers = new SlaveServer[SLAVES];
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i] = new SlaveServer("localhost",
					config.getMainServerPort(), LOGIN,
					PASSWORD);
		}
	}

	public static void startSlaves() throws InterruptedException {
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i].start();
		}
	}

	public static void closeSlaves() throws InterruptedException {
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i].interrupt();
		}

	}

	private HttpResponse getPrimesInRangeHttpResponse(int begin, int end) {
		HttpGet get = null;
		try {
			String url = URL + "/"
					+ URLEncoder.encode(DUMMY_PRIME_COUNTER_TASK_NAME, "UTF-8")
					+ "?begin=" + begin + "&end=" + end + "&noCache=true";
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
			String url = URL + "/"
					+ URLEncoder.encode(DUMMY_PRIME_COUNTER_TASK_NAME, "UTF-8")
					+ "?begin=" + begin + "&end=" + end + "&noCache=true";
			get = new HttpGet(url);
			setCredentialHeaders(get);
			return new JSONObject(new BasicResponseHandler()
					.handleResponse(httpClient.execute(get)));

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
