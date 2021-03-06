package com.example.google;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

/**
 * @author atsushi.kitazawa
 */
public class OutputExcel implements ResponseOutput {

	private static Logger logger = LoggerFactory.getLogger(OutputExcel.class);

	private static final String EXCEL_NAME_PREFIX = "ga";
	private static final String EXCEL_NAME_SUFFIX = ".xlsx";
	private static final String SHEET_NAME = "データセット";
	private static final String PROP_FILE_LOCATION = "conf/output.properties";

	private static Workbook wb = new XSSFWorkbook();
	private static CreationHelper createHelper = wb.getCreationHelper();
	private static DataFormat format = wb.createDataFormat();
	private static String wbName;
	private static int no = 1;

	/** ex) Map<ga:session, "セッション"> */
	private static Map<String, String> headerConvertMap = new HashMap<>();
	/** ex) Map<1, "ga:session"> */
	private static Map<Integer, String> headerOrderMap = new TreeMap<>();
	/** ex) Map<1, INTEGER> */
	private static Map<Integer, String> formatMap = new TreeMap<>();
	/** ex) Map<1, Map<"ga:session", "from,to">> */
	private Map<String, String> dataConvertMap = new HashMap<>();
	/** ex) Map<1, Map<"セッション", "100">> */
	private Map<Integer, Map<String, String>> parseMap = new TreeMap<>();

	public OutputExcel(String startDate, String endDate) {
		wbName = EXCEL_NAME_PREFIX + "_" + startDate + "_" + endDate + EXCEL_NAME_SUFFIX;
		loadProp();
	}

	public OutputExcel(String startDate, String endDate, String customer) {
		wbName = EXCEL_NAME_PREFIX + "_" + startDate + "_" + endDate + "_" + customer + EXCEL_NAME_SUFFIX;
		loadProp();
	}

	private void loadProp() {
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(PROP_FILE_LOCATION), "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			Properties p = new Properties();
			p.load(br);
			p.forEach((k, v) -> {
				if (k.toString().startsWith("header.order.")) {
					headerOrderMap.put(Integer.parseInt(k.toString().substring("header.order.".length())),
							v.toString());
				} else if (k.toString().startsWith("header.format.")) {
					formatMap.put(Integer.parseInt(k.toString().substring("header.format.".length())), v.toString());
				} else if (k.toString().startsWith("data.convert.")) {
					dataConvertMap.put(k.toString().substring("data.convert.".length()), v.toString());
				} else {
					headerConvertMap.put(k.toString(), v.toString());
				}
			});
		} catch (IOException e) {
			throw new RuntimeException("OutputExcel create instance failed.", e);
		}
		logger.debug("headerConvertMap={}", headerConvertMap);
		logger.debug("headerOrderMap={}", headerOrderMap);
		logger.debug("formatMap={}", formatMap);
		logger.debug("dataConvertMap={}", dataConvertMap);
	}

	@Override
	public void parse(GetReportsResponse response) {
		String customer = (String) response.get("customer");
		for (Report report : response.getReports()) {
			ColumnHeader header = report.getColumnHeader();
			List<String> dimensionHeaders = header.getDimensions();
			List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();

			List<ReportRow> rows = report.getData().getRows();
			if (rows == null) {
				logger.info(customer + " is no date.");
				return;
			}
			for (int i = 0; i < rows.size(); i++) {
				Map<String, String> map = new HashMap<>();
				// Add No And Customer Data
				map.put(headerConvertMap.get("no"), Integer.toString(no));
				map.put(headerConvertMap.get("customer"), customer);

				// Add Dimension And Metric Data
				ReportRow row = rows.get(i);
				List<String> dimensions = row.getDimensions();
				for (int j = 0; j < dimensionHeaders.size(); j++) {
					if ("ga:sourceMedium".equals(dimensionHeaders.get(j))) {
						String[] refmedium = dimensions.get(j).split("/");
						map.put(headerConvertMap.get(dimensionHeaders.get(j) + ".1"), refmedium[0]);
						map.put(headerConvertMap.get(dimensionHeaders.get(j) + ".2"), refmedium[1]);
					} else {
						map.put(headerConvertMap.get(dimensionHeaders.get(j)), dimensions.get(j));
					}
				}
				List<DateRangeValues> metrics = row.getMetrics();
				for (DateRangeValues values : metrics) {
					for (int j = 0; j < metricHeaders.size(); j++) {
						map.put(headerConvertMap.get(metricHeaders.get(j).getName()), values.getValues().get(j));
					}
				}
				parseMap.put(no, map);
				no++;
			}
		}
	}

	@Override
	public void output() throws IOException {
		logger.info("output() run");
		try (FileOutputStream fileOut = new FileOutputStream(wbName)) {
			// create excel header row.
			Sheet dataset = wb.createSheet(WorkbookUtil.createSafeSheetName(SHEET_NAME));
			Row headerColume = dataset.createRow((short) 0);
			// headerList is [ga:xxxxx, ga:yyyyy, .....]
			List<String> headerList = new ArrayList<String>(headerOrderMap.values());
			for (int i = 0; i < headerList.size(); i++) {
				Cell c = headerColume.createCell(i);
				c.setCellValue(createHelper.createRichTextString(headerConvertMap.get(headerList.get(i))));
			}
			// create excel data row.
			for (Map.Entry<Integer, Map<String, String>> entry : parseMap.entrySet()) {
				Row dataRow = wb.getSheet(SHEET_NAME).createRow(entry.getKey());
				for (int i = 0; i < headerList.size(); i++) {
					Cell c = dataRow.createCell(i);
					String val = entry.getValue().get(headerConvertMap.get(headerList.get(i))).trim();
					// data convert if val exist in dataConvertMap.
					if("ga:sourceMedium.2".equals(headerList.get(i))) {
						for(Entry<String, String> e : dataConvertMap.entrySet()) {
							String from = e.getValue().split(",")[0];
							String to = e.getValue().split(",")[1];
							if(val.equals(from)) {
								val = to;
							}
						}
					} else {
						// other header not implementation...
					}
					switch (formatMap.get(i + 1)) {
					case "STRING":
						c.setCellValue(createHelper.createRichTextString(val));
						break;
					case "INTEGER":
						c.setCellValue(Integer.parseInt(val));
						CellStyle csInt = wb.createCellStyle();
						csInt.setDataFormat(format.getFormat("0"));
						c.setCellStyle(csInt);
						break;
					case "PERCENT":
						c.setCellValue(Double.parseDouble(val) / 100);
						CellStyle csPer = wb.createCellStyle();
						csPer.setDataFormat(format.getFormat("0.000%"));
						c.setCellStyle(csPer);
						break;
					case "FLOAT":
					case "TIME":
					case "CURRENCY":
						c.setCellValue(Float.parseFloat(val));
						CellStyle csFloat = wb.createCellStyle();
						csFloat.setDataFormat(format.getFormat("0.00"));
						c.setCellStyle(csFloat);
						break;
					default:
						break;
					}
				}
			}
			wb.write(fileOut);
		}
	}

	@Override
	public void close() {
		try {
			wb.close();
		} catch (IOException e) {
			logger.warn("close() failed.");
		}
	}

	public static Map<String, String> getHeaderConvertMap() {
		return headerConvertMap;
	}

	public static Map<Integer, String> getHeaderOrderMap() {
		return headerOrderMap;
	}

	public static Map<Integer, String> getFormatMap() {
		return formatMap;
	}

	public Map<String, String> getDataConvertMap() {
		return dataConvertMap;
	}

	public Map<Integer, Map<String, String>> getParseMap() {
		return parseMap;
	}
}
