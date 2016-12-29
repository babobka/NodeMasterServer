package ru.babobka.nodemasterserver.service;

import java.math.BigInteger;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.babobka.container.Container;
import ru.babobka.nodemasterserver.logger.SimpleLogger;
import ru.babobka.nodemasterserver.model.AuthResult;
import ru.babobka.nodemasterserver.util.StreamUtil;
import ru.babobka.nodeserials.NodeResponse;
import ru.babobka.nodeserials.RSA;

/**
 * Created by dolgopolov.a on 29.10.15.
 */
public class AuthServiceImpl implements AuthService {

	private final NodeUsersService usersService = Container.getInstance()
			.get(NodeUsersService.class);

	private final SimpleLogger logger = Container.getInstance()
			.get(SimpleLogger.class);

	@Override
	public AuthResult getAuthResult(RSA rsa, Socket socket) {
		try {
			StreamUtil.sendObject(rsa.getPublicKey(), socket);
			NodeResponse authResponse = StreamUtil.receiveObject(socket);
			if (authResponse.isAuthResponse()) {
				BigInteger integerHashedPassword = rsa
						.decrypt(authResponse.getAdditionValue("password"));
				String login = authResponse.getAdditionValue("login");
				List<String> tasksList = authResponse
						.getAdditionValue("tasksList");
				if (tasksList != null && !tasksList.isEmpty()) {

					Set<String> taskSet = new HashSet<>();
					taskSet.addAll(tasksList);
					boolean authSuccess = usersService.auth(login,
							integerHashedPassword);
					StreamUtil.sendObject(authSuccess, socket);
					if (authSuccess) {
						return new AuthResult(true, login, taskSet);
					}
				} else {
					return new AuthResult(false);
				}
				return new AuthResult(false);
			} else {
				return new AuthResult(false);
			}
		} catch (Exception e) {
			logger.log(e);
			return new AuthResult(false);
		}
	}
}
