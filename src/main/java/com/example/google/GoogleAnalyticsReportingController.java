package com.example.google;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

/**
 * @author atsushi.kitazawa
 */
public class GoogleAnalyticsReportingController {

	public static void report(String[] args) {
		try {
			Credential.init();
			// System.out.println(Credential.getCredentialMap());
			// System.out.println(Credential.getViewMap());

			String startDate = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String endDate = ZonedDateTime.now().plusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if (args.length != 0) {
				startDate = args[0];
				endDate = args[1];
			}
			Configure.init();
			ResponseOutput ro = (ResponseOutput) Configure.getResponseOutputClass()
					.getConstructor(String.class, String.class).newInstance(startDate, endDate);
			for (String customerName : Credential.getCredentialMap().keySet()) {
				GoogleAnalyticsConnect instance = new GoogleAnalyticsConnect(customerName, startDate, endDate);
				AnalyticsReporting service = instance.initializeAnalyticsReporting();

				GetReportsResponse response = instance.getReport(service);
				ro.parse(response);
				// instance.printResponse(response);
				instance.test(response);
				// break;
			}
			ro.output();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
