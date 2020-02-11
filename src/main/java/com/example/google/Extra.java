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
	
	// レスポンスの中身
	// reports:[{
	// "columnHeader":
	// {"dimensions":["ga:userType","ga:sourceMedium"],
	// "metricHeader":{"metricHeaderEntries":[{"name":"sessions","type":"INTEGER"},{"name":"goalConversionRateAll","type":"PERCENT"},{"name":"percentNewSessions","type":"PERCENT"},{"name":"newUsers","type":"INTEGER"},{"name":"bounceRate","type":"PERCENT"},{"name":"pageviewsPerSession","type":"FLOAT"},{"name":"avgSessionDuration","type":"TIME"},{"name":"transactions","type":"INTEGER"},{"name":"transactionRevenue","type":"CURRENCY"},{"name":"transactionsPerSession","type":"PERCENT"}]}},
	// "data":{"isDataGolden":true,"maximums":[{"values":["2532","0.0","100.0","2532","100.0","1.4","102.14814814814815","0","0.0","0.0"]}],"minimums":[{"values":["1","0.0","0.0","0","80.0","1.0","0.0","0","0.0","0.0"]}],"rowCount":22,"rows":[{"dimensions":["New
	// Visitor","(direct) /
	// (none)"],"metrics":[{"values":["518","0.0","100.0","518","90.92664092664093","1.1274131274131274","40.4015444015444","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","13.230.115.161:6080 /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","bing /
	// organic"],"metrics":[{"values":["20","0.0","100.0","20","85.0","1.2","38.6","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","cptl.corp.yahoo.co.jp /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","duckduckgo /
	// organic"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","google /
	// organic"],"metrics":[{"values":["2532","0.0","100.0","2532","88.42812006319114","1.1524486571879937","50.989731437598735","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","luozengbin.github.io /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","reehappy.com /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","search.fenrir-inc.com /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","search.smt.docomo /
	// organic"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","sp-web.search.auone.jp /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","webblock011.ddreams.jp:15871 /
	// referral"],"metrics":[{"values":["1","0.0","100.0","1","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","webblock015.ddreams.jp:15871 /
	// referral"],"metrics":[{"values":["3","0.0","100.0","3","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["New
	// Visitor","yahoo /
	// organic"],"metrics":[{"values":["92","0.0","100.0","92","89.13043478260869","1.1521739130434783","32.72826086956522","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","(direct) /
	// (none)"],"metrics":[{"values":["132","0.0","0.0","0","84.0909090909091","1.2803030303030303","73.82575757575758","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","13.230.115.161:6080 /
	// referral"],"metrics":[{"values":["1","0.0","0.0","0","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","172.23.38.1 /
	// referral"],"metrics":[{"values":["1","0.0","0.0","0","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","bbs.wankuma.com /
	// referral"],"metrics":[{"values":["2","0.0","0.0","0","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","bing /
	// organic"],"metrics":[{"values":["5","0.0","0.0","0","80.0","1.4","2.6","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","cn.bing.com /
	// referral"],"metrics":[{"values":["2","0.0","0.0","0","100.0","1.0","0.0","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","google /
	// organic"],"metrics":[{"values":["1054","0.0","0.0","0","85.67362428842505","1.1783681214421253","63.18785578747628","0","0.0","0.0"]}]},{"dimensions":["Returning
	// Visitor","yahoo /
	// organic"],"metrics":[{"values":["27","0.0","0.0","0","85.18518518518519","1.2592592592592593","102.14814814814815","0","0.0","0.0"]}]}],"totals":[{"values":["4398","0.0","72.16916780354707","3174","87.94906775807185","1.1600727603456116","52.963392451114146","0","0.0","0.0"]}]}}]
	// customer:cred1
}
