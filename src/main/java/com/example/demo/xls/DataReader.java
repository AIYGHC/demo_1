package com.example.demo.xls;

import com.example.demo.util.DBUtil;
import com.example.demo.util.XRow;

import java.io.File;
import java.util.List;

public class DataReader {
	public static void main(String[] args) throws Exception {
		String filePathName = "D:\\cheguohefile\\家企宽存量ONU弱光率1026\\全量资源\\深圳市.csv";//文件路径
		String tableName = "ST_导入_全量资源_深圳市"; //表名
		int firstSheet = 2;//列名最后一行

		//获取第一行作为表的注释
		XRow firstRowData = ReadExcelXLS.getFirstRowData(new File(filePathName), firstSheet);

		//获取数据作为表的内容
		List<XRow> otherData = ReadExcelXLS.getOtherData(new File(filePathName), firstRowData, firstSheet);

		//创建数据库表,返回表的字段
		List<String> columnList = DBUtil.createTable(firstRowData, tableName);

		//插入数据
		int[] ints = DBUtil.insertRowBatch(columnList, otherData, tableName);
		DBUtil.closeConnection();

	}
}
