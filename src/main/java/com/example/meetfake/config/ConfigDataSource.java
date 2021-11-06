package com.example.meetfake.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ConfigDataSource {

	@Value("${db.datasource.url}")
	private String url;

	@Value("${db.datasource.username}")
	private String user;

	@Value("${db.datasource.password}")
	private String password;

	private String driverClass = "com.mysql.cj.jdbc.Driver";
	private String secrectKey = "abcd1234@@";

	@Bean(name = "dataSource")
	public DataSource dataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(AES.decrypt(url, secrectKey));
		hikariConfig.setDriverClassName(driverClass);
		hikariConfig.setUsername(AES.decrypt(user, secrectKey));
		hikariConfig.setPassword(AES.decrypt(password, secrectKey));
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
