package com.jetbrains.testcontainersdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public class CustomerIntegrationTests {

    @Autowired
    private CustomerDao customerDao;

    //     static = one testcontainers database for each class

    // Was macht testcontainers eigentlich?
    // PostgresSQLContainer container = new PostgreSQLContainer... wird übersetzt in 'docker run -e ...'
    // Argumente von Testcontainer Containers sind eigentlich run arguments von docker
    @Container
    private static PostgreSQLContainer container = new PostgreSQLContainer("postgres:latest");

//    -> testcontainers reuses a existing
//    ~/.testcontainers.properties -> damit reuse geht, muss das in testcontainers properties geht
//    .withReuse(true)

//    wait for the port to be mapped -> can be used with a custom image (GenericContainer)
//    -> wait until the port is ready
//    .withExposedPorts(5432);


//    @Container
//    private static MySQLContainer container = new MySQLContainer(
//            "mysql:latest")
//            .withDatabaseName("somedatabase")
//            .withUsername("root")
//            .withPassword("letsgomarco");

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        System.out.println("container.getJdbcUrl() = " + container.getJdbcUrl());
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Test
    void when_using_a_clean_db_this_should_be_empty() {
        // files auf dem classpath können dem docker container zu verfügung stehen
        container.withClasspathResourceMapping("application.properties",
                "/tmp/application.properties", BindMode.READ_ONLY);

//        Dasselbe wie ClasspathResourceMapping, bloß auf dem File System
//        container.withFileSystemBind()

        try {
            container.execInContainer("echo helloworld");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        container.getLogs(OutputFrame.OutputType.STDOUT);

//        container.withLogConsumer(new Slf4jLogConsumer());

        // siehe port mappings für docker compose file
        container.getMappedPort(5432);

        System.out.println("customerDao = " + customerDao);
        System.out.println("container.getJdbcUrl() = " + container.getJdbcUrl());
        List<Customer> customers = customerDao.findAll();
        assertThat(customers).hasSize(2);
    }
}
