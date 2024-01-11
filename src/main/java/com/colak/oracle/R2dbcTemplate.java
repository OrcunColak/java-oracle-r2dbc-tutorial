package com.colak.oracle;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.R2dbcException;
import io.r2dbc.spi.Readable;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class R2dbcTemplate {

    ConnectionFactory connectionFactory;

    public void getConnectionFactory() {
        connectionFactory = ConnectionFactories
                .get(ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "oracle")
                        .option(ConnectionFactoryOptions.HOST, OracleConfig.getHost())
                        .option(ConnectionFactoryOptions.PORT, OracleConfig.getPort())
                        .option(ConnectionFactoryOptions.DATABASE, OracleConfig.getDatabase())
                        .option(ConnectionFactoryOptions.USER, OracleConfig.getUser())
                        .option(ConnectionFactoryOptions.PASSWORD, OracleConfig.getPassword())
                        .build());

    }

    public Long createTable(String sql) {
        return Flux.usingWhen(
                        // resourceSupplier
                        connectionFactory.create(),
                        // resourceClosure
                        connection -> {
                            Statement statement = connection.createStatement(sql);
                            return statement.execute();
                        },
                        // asyncCleanup
                        Connection::close)
                .flatMap(Result::getRowsUpdated)
                .onErrorReturn(error -> {
                    // Check if the error is ORA-00955 for creating a table that already
                    // exists. If so, then ignore it.
                    if (error instanceof R2dbcException r2dbcException) {
                        return r2dbcException.getErrorCode() == 955;
                    }
                    return false;
                }, 0L)
                .blockLast();
    }


    public Mono<String> querySingle(String sql) {
        Flux<String> queryFlux = Flux.usingWhen(
                        // resourceSupplier
                        connectionFactory.create(),
                        // resourceClosure
                        connection -> {
                            Statement statement = connection.createStatement(sql);
                            return statement.execute();
                        },
                        // asyncCleanup
                        Connection::close
                )
                .flatMap(result -> result.map(row -> row.get(0, String.class)));

        return Mono.from(queryFlux);

    }

    public <T> Flux<T> findAll(String sql, Function<? super Readable, ? extends T> mappingFunction) {
        return Flux.usingWhen(
                        // resourceSupplier
                        connectionFactory.create(),
                        // resourceClosure
                        connection -> {
                            Statement statement = connection.createStatement(sql);
                            return statement.execute();
                        },
                        // asyncCleanup
                        Connection::close
                )
                .flatMap(result -> result.map((row, meta) -> mappingFunction.apply(row)));
    }

    public Long insert(String sql, Consumer<Statement> bindFunction) {
        return Flux.usingWhen(
                        // resourceSupplier
                        connectionFactory.create(),
                        // resourceClosure
                        connection -> {
                            Statement statement = connection.createStatement(sql);
                            bindFunction.accept(statement);
                            return statement.execute();
                        },
                        // asyncCleanup
                        Connection::close
                )
                .flatMap(Result::getRowsUpdated)
                .blockLast();
    }
}
