package jdbcsphere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.sql.DataSource;

public class MyExample2 {

	public static void main(String[] args) throws Exception {
		DataSource dataSource = MyShardingUtil.createDataSource();
		boolean initData = true;

		if (initData) {
			MyShardingUtil.executeUpdate(dataSource, "delete from t_order");
			MyShardingUtil.executeInConnection(dataSource, new Consumer<Connection>() {

				@Override
				public void accept(Connection t) {
					try {
						t.setAutoCommit(false);
						try (PreparedStatement s = t
								.prepareStatement("insert into t_order(user_id,order_id)values(?,?)")) {

							for (int i = 0; i < 12; i++) {
								s.setInt(1, i % 3);
								s.setInt(2, i);
								s.executeUpdate();

							}

						}
						//t.rollback();
						t.commit();
					} catch (SQLException e) {
						throw new RuntimeException("", e);
					}

				}
			});

		}

		{

			String sql = "SELECT * FROM t_order o WHERE o.user_id=? AND o.order_id=2";
			try (Connection conn = dataSource.getConnection();
					PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
				preparedStatement.setInt(1, 0);
				// preparedStatement.setInt(2, 0);
				try (ResultSet rs = preparedStatement.executeQuery()) {
					while (rs.next()) {
						System.out.println(rs.getInt(1));
						System.out.println(rs.getInt(2));
					}
				}
			}
		}
		// TODO see https://blog.csdn.net/clypm/article/details/54378502
		{

			String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
			try (Connection conn = dataSource.getConnection();
					PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
				preparedStatement.setInt(1, 10);
				preparedStatement.setInt(2, 1001);
				try (ResultSet rs = preparedStatement.executeQuery()) {
					while (rs.next()) {
						System.out.println(rs.getInt(1));
						System.out.println(rs.getInt(2));
					}
				}
			}
		}
	}
}
