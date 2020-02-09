package com.example.google;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

/**
 * @author atsushi.kitazawa
 */

public class GoogleAnalyticsConnect {
	private static final String APPLICATION_NAME = "Google Analytics Reporting";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String PROP_FILE_LOCATION = "conf/ga_report.properties";

	private static List<Metric> metricList = new ArrayList<>();
	private static List<Dimension> dimensionList = new ArrayList<>();

	private String customer;
	private String startDate;
	private String endDate;

	static {
		initDimensionMap();
	}

	public GoogleAnalyticsConnect(String customer, String startDate, String endDate) {
		Optional.ofNullable(customer).ifPresent(v -> this.customer = v);
		Optional.ofNullable(startDate).ifPresent(v -> this.startDate = v);
		Optional.ofNullable(endDate).ifPresent(v -> this.endDate = v);
	}

	/**
	 * Initializes an Analytics Reporting API V4 service object.
	 *
	 * @return An authorized Analytics Reporting API V4 service object.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public AnalyticsReporting initializeAnalyticsReporting() throws Exception {
		/** Construct the Analytics Reporting service object. */
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, Credential.getCredentialMap().get(customer))
				.setApplicationName(APPLICATION_NAME).build();
	}

	private static void initDimensionMap() {
		try (FileInputStream is = new FileInputStream(PROP_FILE_LOCATION)) {
			Properties p = new Properties();
			p.load(is);
			int dimensionSize = Integer.parseInt(p.getProperty("dimension.size"));
			int metricSize = Integer.parseInt(p.getProperty("metric.size"));
			p.forEach((k, v) -> {
				if (dimensionSize > dimensionList.size() && k.toString().matches("dimension\\.\\d++")) {
					dimensionList.add(new Dimension().setName(v.toString()));
				}
				if (metricSize > metricList.size() && k.toString().matches("metric\\.\\d++")) {
					metricList.add(
							new Metric().setExpression(v.toString()).setAlias(v.toString().replaceFirst("ga:", "")));
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Queries the Analytics Reporting API V4.
	 *
	 * @param service
	 *            An authorized Analytics Reporting API V4 service object.
	 * @return GetReportResponse The Analytics Reporting API V4 response.
	 * @throws IOException
	 */
	public GetReportsResponse getReport(AnalyticsReporting service) throws IOException {
		// Create the DateRange object.
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(startDate);
		dateRange.setEndDate(endDate);

		// Create Metric
		// Metric session = new
		// Metric().setExpression("ga:sessions").setAlias("sessions");
		// Metric percentNewSessions = new
		// Metric().setExpression("ga:percentNewSessions").setAlias("percentNewSessions");
		// Metric newUsers = new
		// Metric().setExpression("ga:newUsers").setAlias("newUsers");
		// Metric bounceRate = new
		// Metric().setExpression("ga:bounceRate").setAlias("bounceRate");
		// Metric pageSession = new
		// Metric().setExpression("ga:pageviewsPerSession").setAlias("pageviewsPerSession");
		// Metric avgSessionTime = new
		// Metric().setExpression("ga:avgSessionDuration").setAlias("avgSessionDuration");
		// Metric transactions = new
		// Metric().setExpression("ga:transactions").setAlias("transactions");
		// Metric transactionRevenue = new
		// Metric().setExpression("ga:transactionRevenue").setAlias("transactionRevenue");
		// Metric transactionsPerSession = new
		// Metric().setExpression("ga:transactionsPerSession")
		// .setAlias("transactionsPerSession");
		// Metric goalConversionRateAll = new
		// Metric().setExpression("ga:goalConversionRateAll")
		// .setAlias("goalConversionRateAll");

		// Create Dimension
		// Dimension userType = new Dimension().setName("ga:userType");
		// Dimension sourceMedium = new Dimension().setName("ga:sourceMedium");

		// Create the ReportRequest object.
		// ReportRequest request = new
		// ReportRequest().setViewId(Credential.getViewMap().get(customer))
		// .setDateRanges(Arrays.asList(dateRange))
		// .setMetrics(
		// Arrays.asList(session, percentNewSessions, newUsers, bounceRate,
		// pageSession, avgSessionTime,
		// transactions, transactionRevenue, transactionsPerSession,
		// goalConversionRateAll))
		// .setDimensions(Arrays.asList(userType, sourceMedium));
		ReportRequest request = new ReportRequest().setViewId(Credential.getViewMap().get(customer))
				.setDateRanges(Arrays.asList(dateRange)).setMetrics(metricList).setDimensions(dimensionList);

		ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
		requests.add(request);

		// Create the GetReportsRequest object.
		GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

		// Call the batchGet method.
		GetReportsResponse response = service.reports().batchGet(getReport).execute();

		// Set Customer Name.
		response.put("customer", customer);

		// Return the response.
		return response;
	}

	/**
	 * Parses and prints the Analytics Reporting API V4 response.
	 *
	 * @param response
	 *            An Analytics Reporting API V4 response.
	 */
	public static void printResponse(GetReportsResponse response) {

		for (Report report : response.getReports()) {
			ColumnHeader header = report.getColumnHeader();
			List<String> dimensionHeaders = header.getDimensions();
			List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
			List<ReportRow> rows = report.getData().getRows();

			if (rows == null) {
				System.out.println("No data found for " + "");
				return;
			}

			// Create Header Field
			for (String dimension : dimensionHeaders) {
				System.out.print(dimension + ", ");
			}
			for (MetricHeaderEntry entry : metricHeaders) {
				System.out.print(entry.getName() + ", ");
			}
			System.out.println();

			// Create Data Field
			for (ReportRow row : rows) {
				List<String> dimensions = row.getDimensions();
				List<DateRangeValues> metrics = row.getMetrics();
				System.out.print(dimensions.get(0) + ", ");
				for (DateRangeValues values : metrics) {
					for (String value : values.getValues()) {
						System.out.print(value + ", ");
					}
				}
				System.out.println();
			}
		}
	}
}
