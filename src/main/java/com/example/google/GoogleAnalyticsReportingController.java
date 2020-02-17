package com.example.google;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

/**
 * @author atsushi.kitazawa
 */
public class GoogleAnalyticsReportingController {

	private static Logger logger = LoggerFactory.getLogger(GoogleAnalyticsReportingController.class);

	public static void report(String[] args) {
		logger.info("report() run.");
		Credential.init();
		Configure.init();

		ResponseOutput ro = null;
		try {
			String startDate = ZonedDateTime.now().minusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String endDate = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if (args.length != 0) {
				startDate = args[0];
				endDate = args[1];
			}
			logger.info("startDate=" + startDate + " endDate=" + endDate);

			List<String> targetCustomers = new ArrayList<>();
			String target = Configure.getTargetCustomer();
			if ("all".equals(target)) {
				targetCustomers.addAll(Credential.getCredentialMap().keySet());
				ro = (ResponseOutput) Configure.getResponseOutputClass().getConstructor(String.class, String.class)
						.newInstance(startDate, endDate);
			} else {
				logger.info("target customer is " + target);
				ro = (ResponseOutput) Configure.getResponseOutputClass()
						.getConstructor(String.class, String.class, String.class)
						.newInstance(startDate, endDate, target);
				targetCustomers.add(target);
			}
			for (String customerName : targetCustomers) {
				logger.debug("start report " + customerName);
				GoogleAnalyticsConnect instance = new GoogleAnalyticsConnect(customerName, startDate, endDate);
				AnalyticsReporting service;
				GetReportsResponse response;
				try {
					service = instance.initializeAnalyticsReporting();
					response = instance.getReport(service);
				} catch (IOException | GeneralSecurityException e) {
					logger.warn(customerName + "'s reporting failed.", e);
					System.out.println(customerName + "'s reporting failed.");
					continue;
				}
				ro.parse(response);
				// instance.printResponse(response);
			}
			logger.debug("start output.");
			ro.output();
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException
				| InstantiationException e) {
			logger.warn("report() is failed", e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.warn("report() is failed", e);
		} finally {
			Optional.ofNullable(ro).ifPresent(r -> r.close());
		}
	}
}
