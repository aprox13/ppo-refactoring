package ru.akirakozov.sd.refactoring.common;

import org.junit.Assert;

import java.sql.*;
import java.util.function.Function;

public class DBSupport {
    private static final String TEST_DB_URL = "jdbc:sqlite:test.db";

    public static <T> T doWithConnection(Function<Connection, T> processor) {
        try (Connection c = DriverManager.getConnection(TEST_DB_URL)) {
            return processor.apply(c);
        } catch (SQLException throwables) {
            Assert.fail(String.format("Unestablished connection to %s, because %s", TEST_DB_URL, throwables.getMessage()));
            return null;
        }
    }

    public static <T> T processScript(String file, Function<ResultSet, T> processor) {
        String script = TestUtils.getResourceAsString(file);
        return processSql(script, processor);
    }

    public static void executeScript(String file) {
        processScript(file, x -> x);
    }

    public static <T> T processSql(String sql, Function<ResultSet, T> processor) {
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
