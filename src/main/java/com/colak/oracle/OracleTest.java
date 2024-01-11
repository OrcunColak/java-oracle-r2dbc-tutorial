package com.colak.oracle;

import com.colak.model.Customer;
import com.colak.r2dbctemplate.R2dbcTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleTest {

    private static R2dbcTemplate r2dbcTemplate;

    public static void main(String[] args) throws Exception {
        r2dbcTemplate = new R2dbcTemplate();
        r2dbcTemplate.getConnectionFactory("/oracle.properties");

        createTable();
        insertRows();

        querySingle();

        queryAll();
    }

    private static void queryAll() {
        r2dbcTemplate.findAll("SELECT id, name, region FROM customers",
                        readable -> {
                            Integer id = readable.get(0, Integer.class);
                            String name = readable.get(1, String.class);
                            String region = readable.get(2, String.class);
                            Customer customer = new Customer(id, name, region);
                            log.info("Customer : {}", customer);
                            return customer;
                        })
                .blockLast();
    }

    private static void querySingle() {
        String result = r2dbcTemplate.querySingle("SELECT 'Hello, R2DBC!' FROM sys.dual").block();
        log.info(result);
    }

    private static void createTable() {
        Long result = r2dbcTemplate.executeSql("""
                CREATE TABLE customers (
                id NUMBER(20),
                name VARCHAR2(20 BYTE),
                region VARCHAR2(20 BYTE)
                )
                """
        );
        log.info("Table creation result {}", result);
    }

    private static void insertRows() {
        Long insert = r2dbcTemplate.insert("INSERT INTO customers (id, name, region) VALUES (?, ?, ?)", (statement ->
                statement
                        .bind(0, 1)
                        .bind(1, "Juarez")
                        .bind(2, "Dublin"))
        );

        log.info("Insert result {}", insert);
    }

}