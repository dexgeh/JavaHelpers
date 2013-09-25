package helpers.sql.oracle;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OraclePaginator {
	public static String getQuery(String select) {
		select = select.trim();
		if (!select.substring(0, 7).equalsIgnoreCase("SELECT "))
			throw new RuntimeException("Not a select statement: "+select);
		StringBuffer out = new StringBuffer(
			"SELECT *\nFROM (\n" +
				"\tSELECT INNER_TABLE.*, ROWNUM PAG_NUM FROM (\n" +
					"\t\tSELECT COUNT(*) OVER () PAG_TOT, \n");
		out.append(select.substring(7));
		out.append(
				"\n\t) INNER_TABLE WHERE ROWNUM < ? \n" +
			")\nWHERE PAG_NUM >= ? " +
			"\nORDER BY PAG_NUM ASC");
		return out.toString();
	}
	
	public static int addPaginatorParams(PreparedStatement prepStmt, int fromIndex, int pageNumber, int pageSize) throws SQLException {
		int from = 1 + ( (pageNumber - 1) * pageSize);
		int to = from + (pageSize - 1);
		prepStmt.setInt(fromIndex++, from);
		prepStmt.setInt(fromIndex++, to);
		return fromIndex;
	}
}
