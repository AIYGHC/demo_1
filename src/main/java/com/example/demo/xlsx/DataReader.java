package com.example.demo.xlsx;

import com.example.demo.util.DBUtil;
import com.example.demo.util.XRow;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class DataReader {
	public static void main(String[] args) throws Exception {
		String filePathName="D:\\cheguohefile\\家企宽存量ONU弱光率1026\\存量弱光整治通报10.24（2.0版).xlsx";//文件路径
		String tableName ="["+ "ST_导入_31_家企宽存量ONU弱光率_整治清单"+"]"; //表名
		int firstSheet=1;//列名最后一行
		int sheetNum=9;//第几个工作表从0开始计算

//		File file=new File(filePathName);
//		FileInputStream fis = new FileInputStream(file);
//		Workbook workbook = new XSSFWorkbook(fis);
////		sheet列表
//		for (int i=0;i<workbook.getAllNames().size()-1;i++){
//			System.out.println(i +" "+workbook.getSheetName(i));
//		}
//		System.out.println("取sheet列表调试断点");


		//获取第一行作为表的注释
		XRow firstRowData = ReadExcelXLSX.getFirstRowData(new File(filePathName),firstSheet,sheetNum);


		//获取数据作为表的内容
		List<XRow> otherData = ReadExcelXLSX.getOtherData(new File(filePathName), firstRowData,firstSheet,sheetNum);

		//创建数据库表,返回表的字段
		List<String> columnList = DBUtil.createTable(firstRowData,tableName);

		//插入数据
		int[] ints = DBUtil.insertRowBatch(columnList, otherData,tableName);
	}
}
