package com.lankydan.cassandra.person.repository;

import java.time.LocalDate;
import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.lankydan.cassandra.person.Person;
import com.lankydan.cassandra.person.PersonKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.cql.support.CachedPreparedStatementCreator;
import org.springframework.data.cassandra.core.cql.support.PreparedStatementCache;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleCassandraRepository;
import org.springframework.stereotype.Repository;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Repository
public class PersonRepository extends SimpleCassandraRepository<Person, PersonKey> {

  private static final Logger log = LoggerFactory.getLogger(PersonRepository.class);

  private final Session session;
  private final CassandraOperations cassandraTemplate;
  private final PreparedStatementCache cache = PreparedStatementCache.create();

  public PersonRepository(
      Session session,
      CassandraEntityInformation entityInformation,
      CassandraOperations cassandraTemplate) {
    super(entityInformation, cassandraTemplate);
    this.session = session;
    this.cassandraTemplate = cassandraTemplate;
  }

  public List<Person> findByFirstNameAndDateOfBirth(String firstName, LocalDate dateOfBirth) {
    return cassandraTemplate
        .getCqlOperations()
        .query(
            findByFirstNameAndDateOfBirthQuery(firstName, dateOfBirth),
            (row, rowNum) -> cassandraTemplate.getConverter().read(Person.class, row));
  }

  private BoundStatement findByFirstNameAndDateOfBirthQuery(
      String firstName, LocalDate dateOfBirth) {
    return CachedPreparedStatementCreator.of(
            cache,
            select()
                .all()
                .from("people_by_first_name")
                .where(eq("first_name", bindMarker("first_name")))
                .and(eq("date_of_birth", bindMarker("date_of_birth"))))
        .createPreparedStatement(session)
        .bind()
        .setString("first_name", firstName)
        .setDate("date_of_birth", toCqlDate(dateOfBirth));
  }

  private com.datastax.driver.core.LocalDate toCqlDate(LocalDate date) {
    return com.datastax.driver.core.LocalDate.fromYearMonthDay(
        date.getYear(), date.getMonth().getValue(), date.getDayOfMonth());
  }

  public List<Person> findByFirstNameAndDateOfBirthWithoutORM(
      String firstName, LocalDate dateOfBirth) {
    return cassandraTemplate
        .getCqlOperations()
        .query(
            findByFirstNameAndDateOfBirthQuery(firstName, dateOfBirth),
            (row, rowNum) -> convert(row));
  }

  private Person convert(Row row) {
    return new Person(
        new PersonKey(
            row.getString("first_name"),
            toLocalDate(row.getDate("date_of_birth")),
            row.getUUID("person_id")),
        row.getString("last_name"),
        row.getDouble("salary"));
  }

  private LocalDate toLocalDate(com.datastax.driver.core.LocalDate date) {
    return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
  }
}
