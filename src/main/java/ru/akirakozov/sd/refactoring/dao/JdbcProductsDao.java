package ru.akirakozov.sd.refactoring.dao;

import ru.akirakozov.sd.refactoring.model.DBNamings;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.utils.DBSupport;
import ru.akirakozov.sd.refactoring.utils.ProductConverterUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class JdbcProductsDao implements ProductsDao {
    private final DBSupport dbSupport;

    public JdbcProductsDao(String databaseFile) {
        this.dbSupport = new DBSupport(databaseFile);
    }

    @Override
    public void add(Product product) {
        if (product == null) {
            throw new NullPointerException("Inserting null product");
        }

        String sql = String.format(
                "INSERT INTO %s (%s, %s) VALUES (\"%s\", \"%s\")",
                DBNamings.PRODUCT_TABLE, DBNamings.NAME_FIELD, DBNamings.PRICE_FIELD,
                product.getName(), product.getPrice()
        );

        dbSupport.processSql(sql, Function.identity());
    }

    @Override
    public List<Product> getAll() {
        return dbSupport.processSql(
                String.format("SELECT * FROM %s", DBNamings.PRODUCT_TABLE),
                ProductConverterUtils::listFromResultSet
        );
    }

    @Override
    public long sum() {
        return dbSupport.processSql(
                String.format("SELECT SUM(%s) FROM %s", DBNamings.PRICE_FIELD, DBNamings.PRODUCT_TABLE),
                this::singlePositiveValueFromRs
        );
    }

    @Override
    public long count() {
        return dbSupport.processSql(
                String.format("SELECT COUNT(*) FROM %s", DBNamings.PRODUCT_TABLE),
                this::singlePositiveValueFromRs
        );
    }

    @Override
    public Product min() {
        return dbSupport.processSql(
                String.format(
                        "SELECT %s, %s FROM %s ORDER BY %s LIMIT 1",
                        DBNamings.PRICE_FIELD, DBNamings.NAME_FIELD, DBNamings.PRODUCT_TABLE, DBNamings.PRICE_FIELD
                        ),
                ProductConverterUtils::fromResultSet
        );
    }

    @Override
    public Product max() {
        return dbSupport.processSql(
                String.format(
                        "SELECT %s, %s FROM %s ORDER BY %s DESC LIMIT 1",
                        DBNamings.PRICE_FIELD, DBNamings.NAME_FIELD, DBNamings.PRODUCT_TABLE, DBNamings.PRICE_FIELD
                ),
                ProductConverterUtils::fromResultSet
        );
    }

    @Override
    public void createTableIfNotExists() {
        dbSupport.executeScript("create.sql");
    }

    @Override
    public void clearAll() {
        dbSupport.executeScript("clear_products.sql");
    }


    private long singlePositiveValueFromRs(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Expected single positive value in result", e);
        }
    }
}