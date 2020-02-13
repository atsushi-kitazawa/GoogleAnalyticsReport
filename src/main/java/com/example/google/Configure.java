package com.example.google;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author atsushi.kitazawa
 */
public class Configure {

	private static final String PROP_FILE_LOCATION = "conf/run.properties";
	private static final String RESPONSE_OUTPUT_CLASS_KEY = "response.output.class";
	private static Map<String, String> runMap = new HashMap<>();

	public static void init() {
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(PROP_FILE_LOCATION), "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			Properties p = new Properties();
			p.load(br);
			p.forEach((k, v) -> {
				runMap.put(k.toString(), v.toString());
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static Class<?> getResponseOutputClass() throws ClassNotFoundException {
		return Class.forName(runMap.get(RESPONSE_OUTPUT_CLASS_KEY));
	}
}
