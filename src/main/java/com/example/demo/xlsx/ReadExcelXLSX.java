package com.example.demo.xlsx;

import com.example.demo.util.XCell;
import com.example.demo.util.XRow;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReadExcelXLSX {
	/**
	 * 获取第一行的数据,作为数据库字段的注释
	 * @param file
	 * @param firstColumnRow  起始行
	 * @param sheetNum 第几个工作表
	 * @return
	 * @throws Exception
	 */
	public static XRow getFirstRowData(File file,int firstColumnRow,int sheetNum) throws Exception {

		if (!file.exists()) {
			return null;
		}
		firstColumnRow=firstColumnRow-1;
		//获取 work
		FileInputStream fis = new FileInputStream(file);
		Workbook workbook = new XSSFWorkbook(fis);
		//只取第一个sheet
//		for (int g=0;g<15;g++){
//			System.out.println(workbook.getSheetName(g));
//		}

		Sheet sheet = workbook.getSheetAt(sheetNum);
		System.out.println(sheet.getSheetName());
		//第一行的数据封装
		XRow firstRow = new XRow();
		//取第一行
		Row row = sheet.getRow(firstColumnRow);

		firstRow.setRowIndex(sheet.getFirstRowNum()+1);
		//取出头和尾
		short firstCellNum = row.getFirstCellNum();
		short lastCellNum = row.getLastCellNum();
		List<XCell> cells = new ArrayList<>();
		//取出所有的cell
		Set<String> strs=new HashSet<>();
		for (int i = firstCellNum; i < lastCellNum; i++) {
			Cell cell = row.getCell(i);
			//单元格的数据封装
			XCell xCell = new XCell();
			xCell.setCellIndex(i+1);
			//暂时使用string类型测试,可以详细的进行value分类
			if (firstColumnRow>0&&(cell.getCellTypeEnum().toString().equals("ERROR"))){//为空,向上一行取值
				xCell.setValue("["+sheet.getRow(firstColumnRow-1).getCell(i)+"]");
			}else{
				if(cell==null||cell.getCellTypeEnum().toString().equals("ERROR")){
					if (!strs.add("[无列名]")){
						xCell.setValue("[无列名"+xCell.getCellIndex()+"]");
					}else {
						xCell.setValue("[无列名]");
					}
				}else{
					if (!strs.add("["+cell.getStringCellValue()+"]")){
						xCell.setValue("["+cell.getStringCellValue()+xCell.getCellIndex()+"]");
					}else {
						xCell.setValue("["+cell.getStringCellValue()+"]");
					}
				}
			}
			System.out.println(xCell.getValue());
			cells.add(xCell);
		}
		firstRow.setRowValue(cells);

		return firstRow;
	}

	/**
	 * 以第一行为宽度获取其余的数据
	 * @param file
	 * @param firstRow
	 * @param firstColumnRow  起始行
	 * @param sheetNum 第几个工作表
	 * @return
	 * @throws Exception
	 */
	public static List<XRow> getOtherData(File file, XRow firstRow,int firstColumnRow,int sheetNum) throws Exception {
		//所有的行数据
		List<XRow> rows = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA );
		//获取 work
		FileInputStream fis = new FileInputStream(file);
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(sheetNum);
		int firstRowNum = sheet.getFirstRowNum();
		int lastRowNum = sheet.getLastRowNum();
		//第一行的列数
		int columnSize = firstRow.getRowValue().size();
		int firstCellIndex = firstRow.getRowValue().get(0).getCellIndex();
		int lastCellIndex = firstRow.getRowValue().get(columnSize-1).getCellIndex();
		//去掉第一行
		for (int i = firstRowNum+firstColumnRow; i < lastRowNum+1; i++) {
			//遍历所有的行
			XRow xRow = new XRow();
			xRow.setRowIndex(i+1);
			Row row = sheet.getRow(i);
			List<XCell> cells = new ArrayList<>();
			//以第一行为宽度,遍历所有的cell
			for (int j = firstCellIndex-1; j < lastCellIndex; j++) {
				Cell cell = row.getCell(j);

				XCell xCell = new XCell();
				xCell.setCellIndex(j+1);
				if (cell==null){
					xCell.setValue("");
				}else{
					//暂时使用string类型测试,可以详细的进行value分类
					if (cell.getCellTypeEnum()==CellType._NONE){
						xCell.setValue("");
					}else  if (cell.getCellTypeEnum()==CellType.NUMERIC){
						try {
							if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
								DataFormatter dataFormatter=new DataFormatter();
								String date =dataFormatter.formatCellValue(cell).replace("\"","").replace("年","/").replace("月","/").replace("日","");
								xCell.setValue(date);
							} else {
								if(cell.getNumericCellValue()>10000000){
									BigDecimal bd = new BigDecimal(cell.getNumericCellValue());
									xCell.setValue( bd.toPlainString());
								}else {
									if (cell.getNumericCellValue()%1==0){
										xCell.setValue(String.valueOf((int) cell.getNumericCellValue()));
									}else {
										xCell.setValue( String.valueOf(cell.getNumericCellValue()));
									}

								}

							}
						} catch (IllegalStateException e) {
							xCell.setValue( cell.getRichStringCellValue().toString());
						}
					}else  if (cell.getCellTypeEnum()==CellType.STRING){
						xCell.setValue(cell.getStringCellValue());
					}else  if (cell.getCellTypeEnum()==CellType.FORMULA){

						try {
							if (HSSFDateUtil.isCellDateFormatted(cell)) {
								xCell.setValue( cell.getDateCellValue().toString());
							} else {
//								System.out.println(cell.getNumericCellValue());
								if(cell.getNumericCellValue()>10000000){
									BigDecimal bd = new BigDecimal(cell.getNumericCellValue());
									xCell.setValue( bd.toPlainString());
								}else {
									if (cell.getNumericCellValue()%1==0){
										xCell.setValue(String.valueOf((int) cell.getNumericCellValue()));
									}else {
										xCell.setValue( String.valueOf(cell.getNumericCellValue()));
									}

								}
							}
						} catch (IllegalStateException e) {
								try {
									xCell.setValue(cell.getStringCellValue());
								}catch (IllegalStateException w){
									xCell.setValue("");
								}
						}

					}else  if (cell.getCellTypeEnum()==CellType.BLANK){
						xCell.setValue("");
					}else  if (cell.getCellTypeEnum()==CellType.BOOLEAN){
						xCell.setValue(String.valueOf(cell.getBooleanCellValue()));
					}else  if (cell.getCellTypeEnum()==CellType.ERROR) {
						xCell.setValue("");
					}else {
						System.out.println("数据类型不准确");
					}
				}

				cells.add(xCell);
			}
			xRow.setRowValue(cells);
			rows.add(xRow);
		}

		//遍历查看
//		for (XRow row : rows) {
//			int rowIndex = row.getRowIndex();
//			List<XCell> rowValue = row.getRowValue();
//			System.out.println("======rowIndex==="+rowIndex);
//			for (XCell xCell : rowValue) {
//				System.out.println(xCell);
//			}
//		}
		return rows;
	}
}
