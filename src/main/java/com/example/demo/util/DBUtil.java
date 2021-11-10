package com.example.demo.util;

import com.google.common.base.Joiner;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBUtil {

	private static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static String url = "jdbc:sqlserver://193.112.218.97:14998;DatabaseName=MT_CR_PLATFORM";
	private static String name = "dtauser";
	private static String password = "dtauser";
	private static int count = 5000;
	private static int numCount = 0;


	private static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, name, password);
		} catch (ClassNotFoundException e) {
			System.out.println("can not load jdbc driver" + e);
		} catch (SQLException e) {
			System.out.println("get connection failure" + e);
		}
		return conn;
	}



	/**
	 * 关闭数据库连接
	 * @param conn
	 */
	private static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 1.创建数据库表
	 * @param xRow 表格的列名作为数据库的列名的注释
	 * @return 返回一个数据库的列名 column_x
	 */
	public static List<String> createTable(XRow xRow,String tableName) {
		Connection conn = getConnection();
		List<XCell> columnList = xRow.getRowValue();
		System.out.println(columnList.toString());
		Statement stmt;
		List<String> dbColumnList = new ArrayList<>();
		Set<String> tableNames=getDbAllTables();

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE ").append(tableName).append("(");
			sb.append("id INT identity(1,1) not NULL PRIMARY key, ");
			int index = 0;
			for (XCell colimn : columnList) {
				index++;
				dbColumnList.add(colimn.getValue());
				sb.append(colimn.getValue()+" ");
				sb.append("varchar(8000) null ");
				if (index < columnList.size()) {
					sb.append(", ");
				} else {
					sb.append("");
				}
			}
			sb.append(");");
			stmt = conn.createStatement();
			String string="";
			if (tableName.startsWith("[")&& tableName.endsWith("]")){
				string = tableName.substring(1, tableName.length() - 1);
			}else {
				string=tableName;
			}
			if (!tableNames.add(string)){
				System.out.println("删除表！");
				stmt.executeLargeUpdate("DROP TABLE "+tableName);
			}
			if (0 == stmt.executeLargeUpdate(sb.toString())) {
				System.out.println("成功创建表！");
			} else {
				System.out.println("创建表失败！");
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
		}
		//返回的是当前的列.
		return dbColumnList;
	}

	/**
	 * 2.生成一条插入数据的SQL语句
	 * @param columnList 数据库的列名,从createTable方法中获取
	 * @return 返回一个SQL语句
	 */
	private static String generateInsertSQL(List<String> columnList,String tableName) {
		List<String> columnDataList = new ArrayList<>();
		for (int i = 0; i < columnList.size(); i++) {
			columnDataList.add("?");
		}
		String columnNameStr = Joiner.on(",").join(columnList);
		String columnDataStr = Joiner.on(",").join(columnDataList);
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(tableName);
		sb.append(" (");
		sb.append(columnNameStr);
		sb.append(") values(");
		sb.append(columnDataStr);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * 插入一个SQL
	 *
	 * @param columnList 列名
	 * @param row        行数据封装类
	 * @return
	 */
	public static int insertRow(List<String> columnList, XRow row,String tableName) {
		String insertSQL = generateInsertSQL(columnList,tableName);
		int a = 0;
		Connection conn = getConnection();
		try {
			PreparedStatement pst = conn.prepareStatement(insertSQL);
			if (row != null) {
				List<XCell> rowValue = row.getRowValue();
				for (int i = 0; i < rowValue.size(); i++) {
					XCell xCell = rowValue.get(i);
					pst.setString(i + 1, xCell.getValue());
				}
			}
			a = pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
		}
		return a;
	}

	/**
	 * 3.批量插入SQL语句
	 * @param columnList 列名
	 * @param otherData  批量行数据
	 * @return
	 */
	public static int[] insertRowBatch(List<String> columnList, List<XRow> otherData,String tableName) {
		String insertSQL = generateInsertSQL(columnList,tableName);
		int[] a = null;
		Connection conn = getConnection();

		try {
			PreparedStatement pst = conn.prepareStatement(insertSQL);
			int j=0;
			for (XRow rows : otherData) {
				List<XCell> rowValue = rows.getRowValue();
				for (int i = 0; i < rowValue.size(); i++) {
					XCell xCell = rowValue.get(i);
					pst.setString(i+1 , xCell.getValue());
				}
				pst.addBatch();
				j++;

				if (j % count == 0 || j == otherData.size()) {
					long startTime = System.currentTimeMillis();
					a = pst.executeBatch();
					long endTime = System.currentTimeMillis();
					numCount=numCount+j;
					System.out.println("插入数据"+j+"条,插入用时" + (endTime - startTime)/1000+"s"+",累计插入"+numCount+"条");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection(conn);
		}
		//返回-2是执行成功
		return a;
	}

	/**
	 * 4.获取数据库下的所有表名
	 */
	public static Set<String>  getDbAllTables() {
		/*t_roles  t_user  tablename  tablename1  uf_1122  sys_config*/
		Set<String> tableNames = new HashSet<>();
		Connection conn = getConnection();
		ResultSet rs = null;
		try {
			//获取数据库的元数据
			DatabaseMetaData db = conn.getMetaData();
			//从元数据中获取到所有的表名
			rs = db.getTables(null, null, null, new String[]{"TABLE"});
			while (rs.next()) {
				tableNames.add(rs.getString(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tableNames;
	}
}

