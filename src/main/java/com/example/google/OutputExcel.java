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
import java.util.Properties;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

	private static final String EXCEL_NAME_PREFIX = "ga";
	private static final String EXCEL_NAME_SUFFIX = ".xlsx";
	private static final String SHEET_NAME = "�f�[�^�Z�b�g";
	private static final String PROP_FILE_LOCATION = "conf/output.properties";

	private static Workbook wb = new XSSFWorkbook();
	private static CreationHelper createHelper = wb.getCreationHelper();
	private static String wbName;
	private static int no = 1;

	/** ex) Map<ga:session, "�Z�b�V����"> */
	private static Map<String, String> headerConvertMap = new HashMap<>();
	/** ex) Map<1, "�Z�b�V����"> */
	private static Map<Integer, String> headerOrderMap = new TreeMap<>();
	/** ex) Map<1, Map<"�Z�b�V����", "100">> */
	private Map<Integer, Map<String, String>> parseMap = new TreeMap<>();

	public OutputExcel(String startDate, String endDate) {
		wbName = EXCEL_NAME_PREFIX + "_" + startDate + "_" + endDate + EXCEL_NAME_SUFFIX;

		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(PROP_FILE_LOCATION), "UTF-8");
				BufferedReader br = new BufferedReader(isr)) {
			Properties p = new Properties();
			p.load(br);
			p.forEach((k, v) -> {
				if (k.toString().startsWith("header.")) {
					headerOrderMap.put(Integer.parseInt(k.toString().substring("header.".length())), v.toString());
				} else {
					headerConvertMap.put(k.toString(), v.toString());
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println(headerConvertMap);
		// System.out.println(headerOrderMap);
		// System.exit(0);
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
				System.out.println("No data found " + customer);
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
				// System.out.println(map);
				no++;
			}
		}
		// System.exit(0);
		// System.out.println(parseMap);
	}

	@Override
	public void output() throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(wbName)) {
			// �w�b�_�̍쐬
			Sheet dataset = wb.createSheet(WorkbookUtil.createSafeSheetName(SHEET_NAME));
			Row headerColume = dataset.createRow((short) 0);
			List<String> headerOrder = new ArrayList<String>(headerOrderMap.values());
			for (int i = 0; i < headerOrder.size(); i++) {
				Cell c = headerColume.createCell(i);
				c.setCellValue(createHelper.createRichTextString(headerOrder.get(i)));
			}
			// �f�[�^�s�̍쐬
			for (Map.Entry<Integer, Map<String, String>> entry : parseMap.entrySet()) {
				Row dataRow = wb.getSheet(SHEET_NAME).createRow(entry.getKey());
				for (int i = 0; i < headerOrder.size(); i++) {
					Cell c = dataRow.createCell(i);
					c.setCellValue(createHelper.createRichTextString(entry.getValue().get(headerOrder.get(i))));
				}
			}

			wb.write(fileOut);
			wb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			wb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
