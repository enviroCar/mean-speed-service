package org.envirocar.meanspeed.configuration;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgreSQLConfiguration {

	@Bean
	public PostgreSQLDatabase postgreSQLDatabase(@Value("${postgres.host}") String host,
			@Value("${postgres.port}") String port, @Value("${postgres.dbname}") String dbname,
			@Value("${postgres.username}") String username, @Value("${postgres.password}") String password) {

		return new PostgreSQLDatabase(host, port, dbname, username, password);		
	}

}
