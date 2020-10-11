package ru.akirakozov.sd.refactoring.utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DBSupport {
    private final String dbUrl;

    public DBSupport(String dbFile) {
        this.dbUrl = "jdbc:sqlite:" + dbFile;
    }

    private String getResourceAsString(String name) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(name);

        return new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public <T> T doWithConnection(Function<Connection, T> processor) {
        try (Connection c = DriverManager.getConnection(dbUrl)) {
            return processor.apply(c);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Couldn't connect to DB by %s", dbUrl), e);
        }
    }

    public void executeScript(String file) {
        processSql(getResourceAsString(file), Function.identity());
    }

    public <T> T processSql(String sql, Function<ResultSet, T> processor) {
        return doWithConnection(
                connection -> {
                    T result = null;
                    try {
                        Statement stmt = connection.createStatement();
                        stmt.execute(sql);

                        ResultSet rs = stmt.getResultSet();
                        result = processor.apply(rs);
                        stmt.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    return result;
                }
        );
    }


}
