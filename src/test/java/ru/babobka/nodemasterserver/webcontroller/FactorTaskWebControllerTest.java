package ru.babobka.nodemasterserver.webcontroller;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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

public class FactorTaskWebControllerTest {

	static {
		new MasterServerContainerStrategy(StreamUtil.getLocalResource(
				MasterServer.class, MasterServer.MASTER_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
		new SlaveServerContainerStrategy(StreamUtil.getLocalResource(
				SlaveServer.class, SlaveServer.SLAVE_SERVER_TEST_CONFIG))
						.contain(Container.getInstance());
	}

	private static SlaveServer[] slaveServers;

	private static final int SLAVES = 5;

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

	private static final String FACTOR_TASK_NAME = "Elliptic curve factor";

	@BeforeClass
	public static void runServers() throws IOException {
		masterServer = new MasterServer();
		masterServer.start();
		createSlaves();
		startSlaves();
	}

	@AfterClass
	public static void closeServers() {

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

	private void generateTest(int test, int factorBits) {
		for (int i = 0; i < test; i++) {
			BigInteger number = BigInteger
					.probablePrime(factorBits, new Random()).multiply(
							BigInteger.probablePrime(factorBits, new Random()));
			JSONObject json = getFactorJson(number);
			BigInteger factor = json.getJSONObject("resultMap")
					.getBigInteger("factor");
			assertEquals(number.mod(factor), BigInteger.ZERO);

		}
	}

	@Test
	public void testLittleNumberFactor() {
		generateTest(100, 8);
	}

	@Test
	public void testInvalidTask() {
		assertEquals(
				getFactorHttpResponse(BigInteger.valueOf(-10)).getStatusLine()
						.getStatusCode(),
				ru.babobka.vsjws.model.HttpResponse.ResponseCode.BAD_REQUEST
						.getCode());
	}


	@Test
	public void testMediumNumberFactor() {
		generateTest(100, 16);
	}

	@Test
	public void testBigNumberFactor() {
		generateTest(10, 32);
	}

	@Test
	public void testRandomNumbers() {
		Random random = new Random();
		for (int i = 0; i < 50; i++) {
			int bits = random.nextInt(40) + 2;
			BigInteger number = BigInteger.probablePrime(bits, new Random())
					.multiply(BigInteger.probablePrime(bits, new Random()));
			JSONObject json = getFactorJson(number);
			BigInteger factor = json.getJSONObject("resultMap")
					.getBigInteger("factor");
			assertEquals(number.mod(factor), BigInteger.ZERO);
		}
	}

	@Test
	public void testRandomNumbersParallel() throws InterruptedException {
		Random random = new Random();
		Thread[] threads = new Thread[10];
		final AtomicInteger failedTests = new AtomicInteger(0);
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < 5; i++) {
						int bits = random.nextInt(40) + 2;
						BigInteger number = BigInteger
								.probablePrime(bits, new Random())
								.multiply(BigInteger.probablePrime(bits,
										new Random()));
						JSONObject json = getFactorJson(number);
						BigInteger factor = json.getJSONObject("resultMap")
								.getBigInteger("factor");
						if (!number.mod(factor).equals(BigInteger.ZERO)) {
							failedTests.incrementAndGet();
							break;
						}

					}

				}
			});
		}
		for (Thread thread : threads) {
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}
		if (failedTests.get() > 0) {
			fail();
		}

	}

	@Test
	public void testVeryBigNumberFactor() {
		generateTest(15, 40);
	}

	@Test
	public void testExtraBigNumberFactor() {
		generateTest(5, 48);
	}

	public static void createSlaves() throws IOException {
		slaveServers = new SlaveServer[SLAVES];
		for (int i = 0; i < SLAVES; i++) {
			slaveServers[i] = new SlaveServer("localhost",
					config.getMainServerPort(), LOGIN, PASSWORD);
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

	private HttpResponse getFactorHttpResponse(BigInteger number) {
		HttpGet get = null;
		try {
			String url = URL + "/"
					+ URLEncoder.encode(FACTOR_TASK_NAME, "UTF-8") + "?number="
					+ number + "&noCache=true";
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

	private JSONObject getFactorJson(BigInteger number) {

		HttpGet get = null;
		try {
			String url = URL + "/"
					+ URLEncoder.encode(FACTOR_TASK_NAME, "UTF-8") + "?number="
					+ number + "&noCache=true";
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
