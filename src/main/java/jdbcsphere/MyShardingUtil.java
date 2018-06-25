package jdbcsphere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;

public class MyShardingUtil {

	public static DataSource createDataSource() throws SQLException {
		String url0 = "jdbc:postgresql://192.168.110.128/test0";
		String url1 = "jdbc:postgresql://192.168.110.128/test1";
		String user = "postgres";
		String driverClass = "org.postgresql.Driver";

		// 配置真实数据源
		Map<String, DataSource> dataSourceMap = new HashMap<>();

		// 配置第一个数据源
		BasicDataSource dataSource0 = new BasicDataSource();
		dataSource0.setDriverClassName(driverClass);
		dataSource0.setUrl(url0);
		dataSource0.setUsername(user);
		dataSource0.setPassword("");
		dataSourceMap.put("ds_0", dataSource0);

		// 配置第二个数据源
		BasicDataSource dataSource1 = new BasicDataSource();
		dataSource1.setDriverClassName(driverClass);
		dataSource1.setUrl(url1);
		dataSource1.setUsername(user);
		dataSource1.setPassword("");
		dataSourceMap.put("ds_1", dataSource1);

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
		TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
		orderItemTableRuleConfig.setLogicTable("t_order_item");
		orderItemTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_item_${0..1}");

		// 配置分库 + 分表策略
		orderItemTableRuleConfig.setDatabaseShardingStrategyConfig(
				new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
		orderItemTableRuleConfig.setTableShardingStrategyConfig(
				new InlineShardingStrategyConfiguration("order_id", "t_order_item_${order_id % 2}"));
		shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);

		// 获取数据源对象
		Properties props = new Properties();
		props.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), "true");
		DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig,
				new ConcurrentHashMap<String, Object>(), props);
		return dataSource;
	}

	public static void executeInConnection(DataSource dataSource, Consumer<Connection> callback) throws SQLException {
		try (Connection con = dataSource.getConnection()) {
			callback.accept(con);
		}
	}

	public static void executeUpdate(DataSource dataSource, String sql) throws SQLException {
		try (Connection conn = dataSource.getConnection(); PreparedStatement s = conn.prepareStatement(sql)) {
			s.executeUpdate();
		}
	}

	public static void executeUpdate(DataSource dataSource, String sql, Object... args) throws SQLException {
		try (Connection conn = dataSource.getConnection(); PreparedStatement s = conn.prepareStatement(sql)) {
			int i = 1;
			for (Object arg : args) {
				s.setObject(i++, arg);
			}
			s.executeUpdate();
		}
	}

	public static void executeQuery(DataSource dataSource, String sql) throws SQLException {
		try (Connection conn = dataSource.getConnection(); PreparedStatement s = conn.prepareStatement(sql)) {
			s.executeQuery();
		}
	}
}
