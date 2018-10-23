package com.lankydan.cassandra;

import com.lankydan.cassandra.person.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.mapping.CassandraPersistentEntity;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.MappingCassandraEntityInformation;

@Configuration
public class CassandraConfig {

  @Bean
  public CassandraEntityInformation information(CassandraOperations cassandraTemplate) {
    CassandraPersistentEntity<Person> entity =
        (CassandraPersistentEntity<Person>)
            cassandraTemplate
                .getConverter()
                .getMappingContext()
                .getRequiredPersistentEntity(Person.class);
    return new MappingCassandraEntityInformation<>(entity, cassandraTemplate.getConverter());
  }
}
