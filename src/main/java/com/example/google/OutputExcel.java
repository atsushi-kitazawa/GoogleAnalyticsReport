package com.example.google;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

/**
 * @author atsushi.kitazawa
 */
public class OutputExcel implements ResponseOutput {

	private static final String EXCEL_NAME_PREFIX = "ga";
	private static final String EXCEL_NAME_SUFFIX = ".xlsx";
	private static final String SHEET_NAME = "データセット";
	private static final String PROP_FILE_LOCATION = "conf/output.properties";

	private static Workbook wb = new XSSFWorkbook();
	private static CreationHelper createHelper = wb.getCreationHelper();
	private static String wbName;
	private static int no = 1;
	private static String[] header = { "No", "店舗", "ユーザタイプ", "参照元", "メディア", "セッション", "新規セッション率", "新規ユーザー", "直帰率",
			"ページ/セッション", "平均セッション時間", "トランザクション数", "収益", "eコマースのコンバージョン率", "コンバージョン率" };
//	private static String[] header;

	private Map<Integer, List<String>> parseMap = new TreeMap<>();

	public OutputExcel(String startDate, String endDate) {
		wbName = EXCEL_NAME_PREFIX + "_" + startDate + "_" + endDate + EXCEL_NAME_SUFFIX;

//		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(PROP_FILE_LOCATION), "UTF-8");
//				BufferedReader br = new BufferedReader(isr)) {
//			Properties p = new Properties();
//			p.load(br);
//			List<String> list = new ArrayList<>();
//			p.forEach((k, v) -> {
//				list.add(v.toString());
//			});
//			header = list.toArray(new String[list.size()]);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void parse(GetReportsResponse response) {
		String customer = (String) response.get("customer");
		for (Report report : response.getReports()) {
			List<ReportRow> rows = report.getData().getRows();
			if (rows == null) {
				System.out.println("No data found " + customer);
				return;
			}
			for (int i = 0; i < rows.size(); i++) {
				List<String> parseList = new ArrayList<>();
				parseList.add(Integer.toString(no));
				parseList.add(customer);
				ReportRow row = rows.get(i);
				List<String> dimensions = row.getDimensions();
				List<DateRangeValues> metrics = row.getMetrics();
				// Add UserType Dimension
				parseList.add(dimensions.get(0));
				// Add Reference And Medium
				String[] refmedium = dimensions.get(1).split("/");
				parseList.add(refmedium[0]);
				parseList.add(refmedium[1]);
				// Add Metrics
				for (DateRangeValues values : metrics) {
					for (String value : values.getValues()) {
						parseList.add(value);
					}
				}
				parseMap.put(no, parseList);
				no++;
			}
		}
		// System.out.println(parseMap);
	}

	@Override
	public void output() throws IOException {
		try (FileOutputStream fileOut = new FileOutputStream(wbName)) {
			// ヘッダの作成
			Sheet dataset = wb.createSheet(WorkbookUtil.createSafeSheetName(SHEET_NAME));
			Row headerColume = dataset.createRow((short) 0);
			for (int i = 0; i < header.length; i++) {
				Cell c = headerColume.createCell(i);
				c.setCellValue(createHelper.createRichTextString(header[i]));
			}

			for (Map.Entry<Integer, List<String>> entry : parseMap.entrySet()) {
				Row dataRow = wb.getSheet(SHEET_NAME).createRow(entry.getKey());
				for (int i = 0; i < header.length; i++) {
					Cell c = dataRow.createCell(i);
					c.setCellValue(createHelper.createRichTextString(entry.getValue().get(i)));
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
