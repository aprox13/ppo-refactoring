package ru.akirakozov.sd.refactoring.dao;

import ru.akirakozov.sd.refactoring.model.Product;
import java.util.List;

public interface ProductsDao {
    void add(Product product);

    List<Product> getAll();

    long sum();
    long count();

    Product min();
    Product max();

    void createTableIfNotExists();
    void clearAll();
}
