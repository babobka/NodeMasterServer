package ru.babobka.nodemasterserver.server;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class UsersRestServerTest {

	// TODO 'java.net.SocketException: Broken pipe' was found. Fix it.

	private static final ServerExecutor serverExecutor = new ServerExecutor(MasterServer.getInstance());

	private static final int PORT = ServerContext.getInstance().getConfig().getWebPort();

	private static final String URL = "http://localhost:" + PORT + "/users";

	private static final String USER_NAME = "test_user";

	private static JSONObject normalUserJson;

	private static JSONObject badEmailUserJson;

	private static final int SUCCESS_STATUS = 200;

	private static final int UNAUTHORIZED_STATUS = 401;

	private static final String LOGIN = ServerContext.getInstance().getConfig().getRestServiceLogin();

	private static final String PASSWORD = ServerContext.getInstance().getConfig().getRestServicePassword();

	private static final String LOGIN_HEADER = "X-Login";

	private static final String PASSWORD_HEADER = "X-Password";

	private final HttpClient httpClient = HttpClientBuilder.create().build();

	@BeforeClass
	public static void init() {
		normalUserJson = new JSONObject();
		normalUserJson.put("name", USER_NAME);
		normalUserJson.put("taskCount", 0);
		normalUserJson.put("password", "abc");
		normalUserJson.put("email", "babobka@bk.ru");
		badEmailUserJson = new JSONObject(normalUserJson.toString());
		badEmailUserJson.put("email", "abc");
		serverExecutor.run();
	}

	@After
	public void tearDown() throws ClientProtocolException, IOException {
		delete(USER_NAME);
	}

	@Test
	public void testDelete() throws ClientProtocolException, IOException {
		assertNotEquals(delete(normalUserJson.getString("name")).getStatusLine().getStatusCode(), SUCCESS_STATUS);
		assertNotEquals(get(normalUserJson.getString("name")).getStatusLine().getStatusCode(), SUCCESS_STATUS);
	}

	@Test
	public void testGet() throws ClientProtocolException, IOException {
		assertNotEquals(get(normalUserJson.getString("name")).getStatusLine().getStatusCode(), SUCCESS_STATUS);
		add(normalUserJson).getStatusLine().getStatusCode();
		assertEquals(get(normalUserJson.getString("name")).getStatusLine().getStatusCode(), SUCCESS_STATUS);
		assertNotEquals(get(normalUserJson.getString("name") + "abc").getStatusLine().getStatusCode(), SUCCESS_STATUS);

	}

	@Test
	public void testBadAdd() throws ClientProtocolException, IOException {
		assertNotEquals(add(badEmailUserJson).getStatusLine().getStatusCode(), SUCCESS_STATUS);
		assertNotEquals(get(badEmailUserJson.getString("name")).getStatusLine().getStatusCode(), SUCCESS_STATUS);

	}

	@Test
	public void tesAdd() throws ClientProtocolException, IOException {
		assertEquals(add(normalUserJson).getStatusLine().getStatusCode(), SUCCESS_STATUS);
		assertEquals(get(normalUserJson.getString("name")).getStatusLine().getStatusCode(), SUCCESS_STATUS);
	}

	@Test
	public void testDoubleAdd() throws ClientProtocolException, IOException {
		add(normalUserJson).getStatusLine().getStatusCode();
		assertNotEquals(add(normalUserJson).getStatusLine().getStatusCode(), SUCCESS_STATUS);
	}

	@Test
	public void testAuth() throws ClientProtocolException, JSONException, IOException {
		assertNotEquals(get(normalUserJson.getString("name")).getStatusLine().getStatusCode(), UNAUTHORIZED_STATUS);
	}

	@Test
	public void testBadAuth() throws ClientProtocolException, JSONException, IOException {
		assertEquals(badGet(normalUserJson.getString("name")).getStatusLine().getStatusCode(), UNAUTHORIZED_STATUS);
	}

	private HttpResponse add(JSONObject userJSON) throws ClientProtocolException, IOException {
		HttpPatch patch = null;
		try {
			patch = new HttpPatch(URL);
			setCredentialHeaders(patch);
			StringEntity entity = new StringEntity(userJSON.toString());
			patch.setEntity(entity);
			return httpClient.execute(patch);
		} finally {
			if (patch != null) {
				patch.releaseConnection();
			}
		}
	}

	private HttpResponse delete(String userName) throws ClientProtocolException, IOException {
		HttpDelete delete = null;
		try {
			delete = new HttpDelete(URL + "?userName=" + userName);
			setCredentialHeaders(delete);
			return httpClient.execute(delete);
		} finally {
			if (delete != null) {
				delete.releaseConnection();
			}
		}
	}

	private HttpResponse get(String userName) throws ClientProtocolException, IOException {
		HttpGet get = null;
		try {
			get = new HttpGet(URL + "?userName=" + userName);
			setCredentialHeaders(get);
			return httpClient.execute(get);
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}

	private void setCredentialHeaders(HttpMessage httpMessage) {
		httpMessage.setHeader(LOGIN_HEADER, LOGIN);
		httpMessage.setHeader(PASSWORD_HEADER, PASSWORD);
	}

	private HttpResponse badGet(String userName) throws ClientProtocolException, IOException {
		HttpGet get = null;
		try {
			get = new HttpGet(URL + "?userName=" + userName);
			setBadCredentialHeaders(get);
			return httpClient.execute(get);
		} finally {
			if (get != null)
				get.releaseConnection();
		}
	}

	private void setBadCredentialHeaders(HttpMessage httpMessage) {
		httpMessage.setHeader(LOGIN_HEADER, LOGIN + "abc");
		httpMessage.setHeader(PASSWORD_HEADER + "abc", PASSWORD);
	}

}
