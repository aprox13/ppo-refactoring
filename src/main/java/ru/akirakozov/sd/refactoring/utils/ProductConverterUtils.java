package ru.akirakozov.sd.refactoring.utils;

import ru.akirakozov.sd.refactoring.model.DBNamings;
import ru.akirakozov.sd.refactoring.model.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductConverterUtils {

    public static Product fromResultSet(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return new Product(resultSet.getLong(DBNamings.PRICE_FIELD), resultSet.getString(DBNamings.NAME_FIELD));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't parse Product", e);
        }
    }

    public static List<Product> listFromResultSet(ResultSet resultSet) {
        List<Product> res = new ArrayList<>();
        Product tmp;
        while ((tmp = fromResultSet(resultSet)) != null) {
            res.add(tmp);
        }
        return res;
    }
}
