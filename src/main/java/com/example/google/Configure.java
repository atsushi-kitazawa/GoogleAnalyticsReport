package com.example.google;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author atsushi.kitazawa
 */
public class Configure {

	private static Logger logger = LoggerFactory.getLogger(Configure.class);

	private static final String PROP_FILE_LOCATION = "conf/run.properties";
	private static final String RESPONSE_OUTPUT_CLASS_KEY = "response.output.class";
	private static final String TARGET_CUSTOMER_KEY = "target.customer";
	private static Map<String, String> confMap = new HashMap<>();

	public static void init() {
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(PROP_FILE_LOCATION), "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			Properties p = new Properties();
			p.load(br);
			p.forEach((k, v) -> {
				confMap.put(k.toString(), v.toString());
			});
		} catch (IOException e) {
			logger.error("init() failed.", e);
			throw new IllegalStateException(e);
		}
	}

	public static Class<?> getResponseOutputClass() throws ClassNotFoundException {
		return Class.forName(confMap.get(RESPONSE_OUTPUT_CLASS_KEY));
	}

	public static String getTargetCustomer() {
		return confMap.get(TARGET_CUSTOMER_KEY);
	}
}
