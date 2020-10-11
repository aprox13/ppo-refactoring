package ru.akirakozov.sd.refactoring.dao;


import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.utils.DBSupport;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;


@RunWith(MockitoJUnitRunner.class)
public class ProductsDaoTest {
    private static final String DB_FILE = "ProductDaoTest.db";
    private final DBSupport dbSupport = new DBSupport(DB_FILE);

    private Product[] products;
    private ProductsDao productsDao;

    @Before
    public void init() {
        dbSupport.executeScript("create.sql");
        dbSupport.executeScript("clear_products.sql");

        productsDao = new JdbcProductsDao(DB_FILE);

        products = IntStream.range(0, 10)
                .mapToObj(i -> new Product(i * 10L, "product# " + i))
                .toArray(Product[]::new);
    }

    @Test
    public void returnEmptyResultOnGet_Ok() {
        assertThat(productsDao.getAll(), hasSize(0));
    }

    @Test
    public void returnNonEmpty_Ok() {
        Arrays.stream(products).forEach(productsDao::add);

        assertThat(productsDao.getAll(), contains(products));
    }

    @Test
    public void correctlyAdd_Ok() {
        assertThat(productsDao.getAll(), emptyIterable());

        Product product = new Product();
        productsDao.add(product);

        assertThat(productsDao.getAll(), contains(product));
    }

    @Test(expected = NullPointerException.class)
    public void raiseNPEOnNullAdd_Fail() {
        productsDao.add(null);
    }

    private <T> void singleOperationSpec(
            List<Product> products,
            Function<Stream<Product>, T> expectedExtractor,
            Function<ProductsDao, T> operation
    ) {
        T expected = expectedExtractor.apply(products.stream());
        if (!products.isEmpty()) {
            products.forEach(productsDao::add);
        }

        assertThat(operation.apply(productsDao), Is.is(expected));
    }

    @Test
    public void whenEmptyReturnZeroOnMin_Ok() {
        singleOperationSpec(
                Collections.emptyList(),
                productStream -> productStream.min(Comparator.comparing(Product::getPrice)).orElse(null),
                ProductsDao::min
        );
    }

    @Test
    public void whenEmptyReturnZeroOnMax_Ok() {
        singleOperationSpec(
                Collections.emptyList(),
                productStream -> productStream.max(Comparator.comparing(Product::getPrice)).orElse(null),
                ProductsDao::max
        );
    }

    @Test
    public void correctlyReturnMin_Ok() {
        singleOperationSpec(
                Arrays.asList(products),
                productStream -> productStream.min(Comparator.comparing(Product::getPrice)).orElse(null),
                ProductsDao::min
        );
    }

    @Test
    public void correctlyReturnOnMax_Ok() {
        singleOperationSpec(
                Arrays.asList(products),
                productStream -> productStream.max(Comparator.comparing(Product::getPrice)).orElse(null),
                ProductsDao::max
        );
    }

    @Test
    public void whenEmptyReturnZeroOnCount_Ok() {
        singleOperationSpec(
                Collections.emptyList(),
                Stream::count,
                ProductsDao::count
        );
    }

    @Test
    public void whenEmptyReturnZeroOnSum_Ok() {
        singleOperationSpec(
                Collections.emptyList(),
                productStream -> productStream.mapToLong(Product::getPrice).sum(),
                ProductsDao::sum
        );
    }

    @Test
    public void correctlyReturnCount_Ok() {
        singleOperationSpec(
                Arrays.asList(products),
                Stream::count,
                ProductsDao::count
        );
    }

    @Test
    public void correctlyReturnOnSum_Ok() {
        singleOperationSpec(
                Arrays.asList(products),
                productStream -> productStream.mapToLong(Product::getPrice).sum(),
                ProductsDao::sum
        );
    }
}
