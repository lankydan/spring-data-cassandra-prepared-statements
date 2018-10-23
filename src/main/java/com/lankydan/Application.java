package com.lankydan;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.lankydan.cassandra.person.Person;
import com.lankydan.cassandra.person.PersonKey;
import com.lankydan.cassandra.person.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  private final PersonRepository personRepository;

  @Autowired
  public Application(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  public static void main(final String args[]) {
    SpringApplication.run(Application.class);
  }

  @Override
  public void run(String... args) {
    LocalDate date = LocalDate.now();
    Person john = new Person(new PersonKey("John", date, UUID.randomUUID()), "A", 1000);

    personRepository.insert(john);
    Stopwatch timer = Stopwatch.createStarted();
    log.info("with ORM: {}", personRepository.findByFirstNameAndDateOfBirth("John", date));
    log.info("time taken without cache: {}", timer.stop().elapsed(TimeUnit.MILLISECONDS));
    timer.reset().start();
    log.info("with ORM: {}", personRepository.findByFirstNameAndDateOfBirth("John", date));
    log.info("time taken with cache: {}", timer.stop().elapsed(TimeUnit.MILLISECONDS));
    timer.reset().start();
    log.info("with ORM: {}", personRepository.findByFirstNameAndDateOfBirth("John", date));
    log.info("time taken with cache: {}", timer.stop().elapsed(TimeUnit.MILLISECONDS));

    log.info(
        "without ORM: {}", personRepository.findByFirstNameAndDateOfBirthWithoutORM("John", date));
  }
}
