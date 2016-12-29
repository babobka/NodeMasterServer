package ru.babobka.nodemasterserver.util;

public interface TextUtil {

	static long tryParseLong(String s, long defaultValue) {
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	static int tryParseInt(String s, int defaultValue) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
