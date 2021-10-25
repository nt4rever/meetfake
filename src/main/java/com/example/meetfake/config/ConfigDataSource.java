package com.example.meetfake.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ConfigDataSource {
//	private String url = "jdbc:mysql://nnsgluut5mye50or.cbetxkdyhwsb.us-east-1.rds.amazonaws.com:3306/utf9zq5vfcv2q8sd";
//	private String user = "pumk3tz4esvc5m6a";
//	private String password = "y3ieue1e1ju01rpi";
	
//	private String url = "jdbc:mysql://containers-us-west-20.railway.app:7184/railway";
//	private String user = "root";
//	private String password = "itrHTS5aZPArmIKEuYsY";

//	private String url = "jdbc:mysql://sql6.freemysqlhosting.net:3306/sql6446393";
//	private String user = "sql6446393";
//	private String password = "JfuxEUmIHy";
	
	 private String url = "jdbc:mysql://localhost:3306/meet";
	 private String user = "root";
	 private String password = "";
	
	
	private String driverClass = "com.mysql.cj.jdbc.Driver";

	@Bean(name = "dataSource")
	public DataSource dataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(url);
		hikariConfig.setDriverClassName(driverClass);
		hikariConfig.setUsername(user);
		hikariConfig.setPassword(password);
		HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
		return hikariDataSource;
	}

	@Bean(name = "transactionManager")
	public DataSourceTransactionManager dataSourceTransactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
		final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);

		/* Set the mapper file location */
		sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
				.getResources("classpath:com/example/meetfake/mapper/sql/*xml"));

		/* Set entity class mapping rules: Underscore -> Hump */
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		sessionFactory.setConfiguration(configuration);
		return sessionFactory.getObject();
	}
}
