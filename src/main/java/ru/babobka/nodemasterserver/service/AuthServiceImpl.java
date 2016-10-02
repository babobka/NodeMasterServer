package ru.babobka.nodemasterserver.service;

import java.math.BigInteger;
import java.net.Socket;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import ru.babobka.nodemasterserver.model.AuthResult;
import ru.babobka.nodemasterserver.server.ServerContext;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.RSA;

/**
 * Created by dolgopolov.a on 29.10.15.
 */
public class AuthServiceImpl implements AuthService {

	private final NodeUsersService usersService = NodeUsersServiceImpl.getInstance();

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

	@Override
	public AuthResult getAuthResult(RSA rsa, Socket socket) {
		try {
			StreamUtil.sendObject(rsa.getPublicKey(), socket);

			NodeResponse authResponse = (NodeResponse) StreamUtil.receiveObject(socket);
			if (authResponse.isAuthResponse()) {
				BigInteger integerHashedPassword = rsa.decrypt((BigInteger) authResponse.getAddition().get("password"));
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
				boolean authSuccess = usersService.auth(login, integerHashedPassword);
				StreamUtil.sendObject(authSuccess, socket);
				if (authSuccess) {
					return new AuthResult(true, login, taskSet);
				}
				return new AuthResult(false);
			} else {
				return new AuthResult(false);
			}
		} catch (Exception e) {
			ServerContext.getInstance().getLogger().log(e);
			return new AuthResult(false);
		}
	}
}
