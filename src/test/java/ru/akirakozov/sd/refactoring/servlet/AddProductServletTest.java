package ru.akirakozov.sd.refactoring.servlet;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.dao.JdbcProductsDao;
import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.utils.DBSupport;
import ru.akirakozov.sd.refactoring.common.HttpServletProviders;
import ru.akirakozov.sd.refactoring.common.SuccessfulHtmlMatcher;
import ru.akirakozov.sd.refactoring.model.Product;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static ru.akirakozov.sd.refactoring.common.TestUtils.mapOf;

@RunWith(MockitoJUnitRunner.class)
public class AddProductServletTest {

    private static final String DB_FILE = "test.db";
    private final DBSupport dbSupport = new DBSupport(DB_FILE);
    private ProductsDao productsDao;

    private static final String NAME_PARAM = "name";
    private static final String PRICE_PARAM = "price";

    private final HttpServlet servlet = new AddProductServlet();
    private Writer writer;
    private HttpServletResponse response;

    @Before
    public void init() {
        dbSupport.executeScript("create.sql");
        dbSupport.executeScript("clear_products.sql");

        writer = new StringWriter();
        response = HttpServletProviders.provideResponse(writer);
        productsDao = new JdbcProductsDao(DB_FILE);
    }

    @After
    public void close() throws IOException {
        writer.close();
    }

    private Map<String, String> productToParams(Product product) {
        return mapOf(NAME_PARAM, String.valueOf(product.getName()), PRICE_PARAM, product.getPrice().toString());
    }

    @Test
    public void correctlyAdd_Ok() throws ServletException, IOException {
        assertThat(productsDao.count(), is(0L));

        Product product = new Product(1000L, "name");
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(productToParams(product));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("OK\n"));

        assertThat(productsDao.getAll(), contains(product));
    }

    @Test(expected = NumberFormatException.class)
    public void failOnMissedPrice_Fail() throws ServletException, IOException {
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap(NAME_PARAM, "name"));

        servlet.service(request, response);
    }

    @Test
    public void addWithMissedName_Ok() throws ServletException, IOException {
        assertThat(productsDao.count(), is(0L));

        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap(PRICE_PARAM, "1000"));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("OK\n"));

        assertThat(productsDao.getAll(), contains(new Product(1000L, "null")));
    }

    @Test
    public void doesnotFailOnDuplication_Fail() throws ServletException, IOException {
        Product product = new Product(10L, "duplication");
        productsDao.add(product);

        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(productToParams(product));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("OK\n"));

        assertThat(productsDao.getAll(), contains(product, product));
    }
}
