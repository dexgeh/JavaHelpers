package helpers.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DAOHelper {
	
	public static Connection getconnection(String className, String url, String username, String password) throws SQLException {
		try {
			Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException(cnfe);
		}
		return DriverManager.getConnection(url, username, password);
	}
	
	public abstract Connection getConnection() throws SQLException;
	
	public static abstract class SQLHandler {
		public abstract void prepare(PreparedStatement ps) throws SQLException;
		public abstract String getSql();
		public abstract void onError(Exception e) throws Exception;
	}
	
	public static abstract class QueryHandler extends SQLHandler {
		public abstract void onRow(ResultSet rs) throws SQLException;
	}
	
	public static abstract class StatementHandler extends SQLHandler {
		public abstract void checkResult(int result) throws Exception;
	}
	
	public void execute(SQLHandler handler) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(handler.getSql());
			handler.prepare(ps);
			try {
				QueryHandler queryHandler = (QueryHandler) handler;
				rs = ps.executeQuery();
				while (rs.next()) {
					queryHandler.onRow(rs);
				}
			} catch (ClassCastException e) {
				StatementHandler statementHandler = (StatementHandler) handler;
				int result = ps.executeUpdate();
				statementHandler.checkResult(result);
			}
		} catch (Exception e) {
			handler.onError(e);
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				// ignore
			}
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException e) {
				// ignore
			}
			try {
				if (connection != null && !connection.isClosed())
					connection.close();	
			} catch (SQLException e) {
				// ignore
			}
		}
	}
}
