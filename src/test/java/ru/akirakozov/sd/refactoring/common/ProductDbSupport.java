package ru.akirakozov.sd.refactoring.common;

import ru.akirakozov.sd.refactoring.model.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProductDbSupport {

    private static Product productFromResultSet(ResultSet resultSet) throws SQLException {
        return new Product(resultSet.getLong("price"), resultSet.getString("name"));
    }

    private static List<Product> fromResultSet(ResultSet rs) {
        try {
            List<Product> res = new ArrayList<>();
            while (rs.next()) {
                res.add(productFromResultSet(rs));
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public static List<Product> getRows() {
        return DBSupport.processSql("SELECT * FROM PRODUCT;", ProductDbSupport::fromResultSet);
    }
}
