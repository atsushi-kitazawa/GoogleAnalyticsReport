package com.example.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;

/**
 * 
 * @author a4dos
 * 拡張用のコード片
 *
 */
public class Extra {

	private String customer = null;
	private String startDate = null;
	private String endDate = null;
	
	// メトリックを変更可能にする場合
	public List<GetReportsResponse> getReport(AnalyticsReporting service) throws IOException {
		// Create Return Object
		List<GetReportsResponse> responses = new ArrayList<>();

		// Create the DateRange object.
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(startDate);
		dateRange.setEndDate(endDate);

		// APIの上限が10なので分けて取る必要がある
		Map<String, String> metricMap = new HashMap<>();
		metricMap.put("ga:sessions", "sessions");
		metricMap.put("ga:percentNewSessions", "percentNewSessions");
		metricMap.put("ga:newUsers", "newUsers");
		metricMap.put("ga:bounceRate", "bounceRate");
		metricMap.put("ga:pageviewsPerSession", "pageviewsPerSession");
		metricMap.put("ga:avgSessionDuration", "avgSessionDuration");
		metricMap.put("ga:transactions", "transactions");
		metricMap.put("ga:transactionRevenue", "transactionRevenue");
		metricMap.put("ga:transactionsPerSession", "transactionsPerSession");
		metricMap.put("ga:goalConversionRateAll", "goalConversionRateAll");

		List<Metric> metricList = new ArrayList<>();
		for (Entry<String, String> e : metricMap.entrySet()) {
			metricList.add(new Metric().setExpression(e.getKey()).setAlias(e.getValue()));
		}

		Dimension userType = new Dimension().setName("ga:userType");
		Dimension sourceMedium = new Dimension().setName("ga:sourceMedium");

		// Create the ReportRequest object.
		int loop = metricList.size() % 10;
		for (int i = 0; i <= loop; i++) {
			Metric[] m;
			if (i != loop) {
				m = metricList.subList((i * 10), (i * 10) + 10).toArray(new Metric[10]);
			} else {
				m = metricList.subList((i * 10), (metricList.size() - 1))
						.toArray(new Metric[metricList.size() - (i * 10)]);
			}
			ReportRequest request = new ReportRequest().setViewId(Credential.getViewMap().get(customer))
					.setDateRanges(Arrays.asList(dateRange)).setMetrics(Arrays.asList(m))
					.setDimensions(Arrays.asList(userType, sourceMedium));

			ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
			requests.add(request);

			// Create the GetReportsRequest object.
			GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

			// Call the batchGet method.
			GetReportsResponse response = service.reports().batchGet(getReport).execute();

			// Return the response.
			responses.add(response);
		}
		return responses;
	}
}
