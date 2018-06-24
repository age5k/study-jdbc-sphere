package jdbcsphere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.InlineShardingStrategyConfiguration;

public class MyExample {

	public static void main(String[] args) throws Exception {

		// 配置真实数据源
		Map<String, DataSource> dataSourceMap = new HashMap<>();

		// 配置第一个数据源
		BasicDataSource dataSource1 = new BasicDataSource();
		dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource1.setUrl("jdbc:mysql://localhost:3306/ds_0");
		dataSource1.setUsername("root");
		dataSource1.setPassword("");
		dataSourceMap.put("ds_0", dataSource1);

		// 配置第二个数据源
		BasicDataSource dataSource2 = new BasicDataSource();
		dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource2.setUrl("jdbc:mysql://localhost:3306/ds_1");
		dataSource2.setUsername("root");
		dataSource2.setPassword("");
		dataSourceMap.put("ds_1", dataSource2);

		// 配置Order表规则
		TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
		orderTableRuleConfig.setLogicTable("t_order");
		orderTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${0..1}");

		// 配置分库 + 分表策略
		orderTableRuleConfig.setDatabaseShardingStrategyConfig(
				new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
		orderTableRuleConfig.setTableShardingStrategyConfig(
				new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));

		// 配置分片规则
		ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
		shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

		// 省略配置order_item表规则...
		// ...

		// 获取数据源对象
		DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig,
				new ConcurrentHashMap<String, Object>(), new Properties());

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
