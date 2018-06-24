package jdbcsphere;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class PsqlExample {

	public static void main(String[] args) throws Exception {
		new PsqlExample().start();
	}

	public void start() throws Exception {
		int totalDs = 2;
		for (int i = 0; i < totalDs; i++) {
			this.initDb(totalDs, "test" + i);
		}

	}

	public void initDb(int totalDs, String dbName) throws Exception {
		String url = "jdbc:postgresql://192.168.110.128/" + dbName;
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "");
		// props.setProperty("ssl", "true");

		try (Connection conn = DriverManager.getConnection(url, props)) {
			for (int i = 0; i < totalDs; i++) {

				String orderTable = "t_order_" + i;
				String orderItemTable = "t_order_item_" + i;
				{
					String sql = "CREATE TABLE " + orderTable + "(" + //
							"order_id integer," + //
							"user_id integer," + //
							"PRIMARY KEY(order_id)" + //
							");" + //
							"";//
					try (PreparedStatement s = conn.prepareStatement(sql)) {
						s.executeUpdate();
					}

				}
				{
					String sql = "CREATE TABLE " + orderItemTable + "(" + //
							"order_id integer," + //
							"user_id integer," + //
							"item_id integer," + //
							"PRIMARY KEY(item_id)" + //
							");" + //
							"";//
					try (PreparedStatement s = conn.prepareStatement(sql)) {
						s.executeUpdate();
					}
				}

			}

		}

	}

}
