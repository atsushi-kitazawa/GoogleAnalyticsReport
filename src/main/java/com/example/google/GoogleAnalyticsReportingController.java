package com.example.google;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

/**
 * @author atsushi.kitazawa
 */
public class GoogleAnalyticsReportingController {

	public static void report(String[] args) {
		Credential.init();
		Configure.init();

		ResponseOutput ro = null;
		try {
			String startDate = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String endDate = ZonedDateTime.now().plusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if (args.length != 0) {
				startDate = args[0];
				endDate = args[1];
			}

			ro = (ResponseOutput) Configure.getResponseOutputClass().getConstructor(String.class, String.class)
					.newInstance(startDate, endDate);

			List<String> targetCustomers = new ArrayList<>();
			String target = Configure.getTargetCustomer();
			if ("all".equals(target)) {
				targetCustomers.addAll(Credential.getCredentialMap().keySet());
			} else {
				targetCustomers.add(target);
			}
			for (String customerName : targetCustomers) {
				GoogleAnalyticsConnect instance = new GoogleAnalyticsConnect(customerName, startDate, endDate);
				AnalyticsReporting service;
				GetReportsResponse response;
				try {
					service = instance.initializeAnalyticsReporting();
					response = instance.getReport(service);
				} catch (IOException | GeneralSecurityException e) {
					e.printStackTrace();
					continue;
				}
				ro.parse(response);
				// instance.printResponse(response);
			}
			ro.output();
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException
				| InstantiationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Optional.ofNullable(ro).ifPresent(r -> r.close());
		}
	}
}
