package com.example.demo.csv;

import com.example.demo.util.DBUtil;
import com.example.demo.util.XRow;

import java.io.*;
import java.util.List;

public class DataReader {

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		//文件一次读取多少行
		int count = 50000;
		String filePathName = "D:\\cheguohefile\\家企宽存量ONU弱光率1026\\集中性能.csv";//文件路径
		String tableName = "[" + "ST_导入_31_家企宽存量ONU弱光率_全量资源_深圳市test" + "]"; //表名
		//获取第一行作为表的注释
		XRow firstRowData = ReadCSV.getFirstRowData(filePathName);
		System.out.println("_____________________________________________");
		//创建数据库表,返回表的字段
		List<String> columnList = DBUtil.createTable(firstRowData, tableName);

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filePathName)));
		BufferedReader in = new BufferedReader(new InputStreamReader(bis, "GBK"), 10 * 1024 * 1024);//10M缓存

		int a = 0;
		while (in.ready()){
			a++;
			in.readLine();
		}
		in.close();
		System.out.println("文件共"+a+"行");
		int forCount=0;
		if (a%count==0){
			forCount=a/count;
		}else {
			forCount=a/count +1;
		}
		for (int i=0;i<forCount;i++){
			System.out.println("第"+(i+1)+"次读取文件");
//			System.out.println(i);
//		//获取数据作为表的内容
			List<XRow> otherData = ReadCSV.getOtherData(filePathName, firstRowData,columnList,tableName,i);
//		//插入剩下数据
     		if ((a-i*forCount)<count) {
				System.out.println("最后数据导入");
				int[] ints = DBUtil.insertRowBatch(columnList, otherData, tableName);
				DBUtil.closeConnection();
			}
		}


		long endTime = System.currentTimeMillis();
		System.out.println("总用时"+(endTime - startTime)/60000+"分");

	}
}
