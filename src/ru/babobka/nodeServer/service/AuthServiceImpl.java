package ru.babobka.nodeServer.service;

import ru.babobka.nodeServer.Server;
import ru.babobka.nodeServer.dao.NodeUsersDAO;
import ru.babobka.nodeServer.dao.NodeUsersDAOImpl;
import ru.babobka.nodeServer.model.AuthResult;
import ru.babobka.nodeServer.model.User;
import ru.babobka.nodeServer.util.StreamUtil;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.RSA;

import java.math.BigInteger;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by dolgopolov.a on 29.10.15.
 */
public class AuthServiceImpl implements AuthService {

	private final NodeUsersDAO nodeUsersDAO = NodeUsersDAOImpl.getInstance();

	private static volatile AuthServiceImpl instance;

	private AuthServiceImpl() {

	}

	public static AuthServiceImpl getInstance() {
		AuthServiceImpl localInstance = instance;
		if (localInstance == null) {
			synchronized (AuthServiceImpl.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new AuthServiceImpl();
				}
			}
		}
		return localInstance;
	}

	private boolean auth(String login, BigInteger hashedPassword) {
		User user = nodeUsersDAO.get(login);
		if (user != null && user.getHashedPassword().equals(hashedPassword)) {
			return true;
		}
		return false;
	}

	@Override
	public AuthResult getAuthResult(RSA rsa, Socket socket) {
		try {
			StreamUtil.sendObject(rsa.getPublicKey(), socket);

			NodeResponse authResponse = (NodeResponse) StreamUtil.receiveObject(socket);
			if (authResponse.isAuthResponse()) {
				BigInteger password = rsa.decrypt((BigInteger) authResponse.getAddition().get("password"));
				String login = (String) authResponse.getAddition().get("login");
				@SuppressWarnings("unchecked")
				LinkedList<String> uriList = (LinkedList<String>) authResponse.getAddition().get("uriList");
				if (uriList.isEmpty()) {
					return new AuthResult(false);
				}
				Set<String> taskSet = new HashSet<>();
				for (String uri : uriList) {
					taskSet.add(uri);
				}
				boolean authSuccess = auth(login, password);
				StreamUtil.sendObject(authSuccess, socket);
				if (authSuccess) {
					return new AuthResult(true, login, taskSet);
				}
				return new AuthResult(false);
			} else {
				return new AuthResult(false);
			}
		} catch (Exception e) {
			Server.getLogger().log(e);
			return new AuthResult(false);
		}
	}
}
