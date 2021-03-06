package com.example.google;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static Logger logger = LoggerFactory.getLogger(GoogleAnalyticsConnect.class);

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

	public AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {
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
			Map<Integer, Dimension> dimensionMap = new TreeMap<>();
			Map<Integer, Metric> metricMap = new TreeMap<>();
			p.forEach((k, v) -> {
				if (dimensionSize > dimensionMap.keySet().size() && k.toString().matches("dimension\\.\\d++")) {
					dimensionMap.put(Integer.parseInt(k.toString().substring("dimension.".length())),
							new Dimension().setName(v.toString()));
				}
				if (metricSize > metricMap.keySet().size() && k.toString().matches("metric\\.\\d++")) {
					metricMap.put(Integer.parseInt(k.toString().substring("metric.".length())),
							new Metric().setExpression(v.toString()));
				}
			});
			dimensionList = new ArrayList<Dimension>(dimensionMap.values());
			metricList = new ArrayList<Metric>(metricMap.values());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public GetReportsResponse getReport(AnalyticsReporting service) throws IOException {
		// Create the DateRange object.
		DateRange dateRange = new DateRange();
		dateRange.setStartDate(startDate);
		dateRange.setEndDate(endDate);

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

	public void printResponse(GetReportsResponse response) {

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

	public void test(GetReportsResponse response) {
		for (Entry<String, Object> e : response.entrySet()) {
			System.out.println(e.getKey() + "=" + e.getValue());
		}
	}
}
