package com.colak.r2dbctemplate;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Readable;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class R2dbcTemplate {

    ConnectionFactory connectionFactory;

    public void getConnectionFactory(String path) throws IOException {
        R2dbcConfig r2dbcConfig = new R2dbcConfig();
        r2dbcConfig.load(path);

        connectionFactory = ConnectionFactories
                .get(ConnectionFactoryOptions.builder()
                        .option(ConnectionFactoryOptions.DRIVER, "pool")
                        .option(ConnectionFactoryOptions.PROTOCOL, r2dbcConfig.getProtocol())
                        .option(ConnectionFactoryOptions.HOST, r2dbcConfig.getHost())
                        .option(ConnectionFactoryOptions.PORT, r2dbcConfig.getPort())
                        .option(ConnectionFactoryOptions.DATABASE, r2dbcConfig.getDatabase())
                        .option(ConnectionFactoryOptions.USER, r2dbcConfig.getUser())
                        .option(ConnectionFactoryOptions.PASSWORD, r2dbcConfig.getPassword())
                        .build());

    }

    public Long executeSql(String sql) {
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
