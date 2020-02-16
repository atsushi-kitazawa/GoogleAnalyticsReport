package com.example.google;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;

/**
 * @author atsushi.kitazawa
 */
public class Credential {

	private static Logger logger = LoggerFactory.getLogger(Credential.class);

	private static final String KEY_FILE_LOCATION = "credential";
	private static final String CREDENTIAL_FILE_SUFFIX = ".json";
	private static final String VIEW_FILE_SUFFIX = ".view";

	private static Map<String, GoogleCredential> credentialMap = new HashMap<>();
	private static Map<String, String> viewMap = new HashMap<>();

	public static void init() {
		File rootDir = new File(KEY_FILE_LOCATION);
		for (File dir : rootDir.listFiles()) {
			if (!dir.isDirectory())
				continue;
			createMap(dir);
		}
		logger.debug("credentialMap={}", credentialMap);
		logger.debug("viewMap={}", viewMap);
	}

	private static void createMap(File customerDir) {
		File[] files = customerDir.listFiles();
		GoogleCredential credential = null;
		String viewId = null;
		for (File f : files) {
			try {
				if (f.getName().endsWith(CREDENTIAL_FILE_SUFFIX)) {
					credential = GoogleCredential.fromStream(new FileInputStream(f.getAbsoluteFile()))
							.createScoped(AnalyticsReportingScopes.all());
				}
				if (f.getName().endsWith(VIEW_FILE_SUFFIX)) {
					try (BufferedReader br = new BufferedReader(new FileReader(f))) {
						viewId = br.readLine();
					}
				}
			} catch (IOException e) {
				logger.error("init() failed.", e);
				throw new IllegalStateException(e);
			}
		}
		credentialMap.put(customerDir.getName(), credential);
		viewMap.put(customerDir.getName(), viewId);
	}

	public static Map<String, GoogleCredential> getCredentialMap() {
		return credentialMap;
	}

	public static Map<String, String> getViewMap() {
		return viewMap;
	}
}
