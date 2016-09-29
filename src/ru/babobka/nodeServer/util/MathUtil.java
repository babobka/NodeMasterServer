package ru.babobka.nodeServer.util;

import ru.babobka.nodeserials.NodeRequest;

import java.security.MessageDigest;

/**
 * Created by dolgopolov.a on 06.07.15.
 */
public interface MathUtil {


	static byte[] sha2(String message) {
		if (message != null) {
			try {
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				byte[] messageBytes = message.getBytes();
				return sha256.digest(messageBytes);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
		return new byte[] {};
	}

	static NodeRequest[] subArray(NodeRequest[] requests, int beginIndex) {
		if (requests.length >= beginIndex) {
			int newSize = requests.length - beginIndex;
			NodeRequest[] newRequests = new NodeRequest[newSize];
			int j = 0;
			for (int i = beginIndex; i < requests.length; i++) {
				newRequests[j] = requests[i];
				j++;
			}
			return newRequests;
		} else {
			throw new IllegalArgumentException("requests size is " + requests.length + " beginIndex is " + beginIndex);
		}
	}

}