package ru.babobka.nodemasterserver.util;

public interface TextUtil {

	static long tryParseLong(String s, long defaultValue)
	{
		try{
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
