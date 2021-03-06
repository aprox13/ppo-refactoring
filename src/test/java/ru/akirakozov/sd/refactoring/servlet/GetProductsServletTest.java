package ru.akirakozov.sd.refactoring.servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.common.*;
import ru.akirakozov.sd.refactoring.dao.JdbcProductsDao;
import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.response.HtmlBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class GetProductsServletTest {

    private static final String DB_FILE = "GetProductsServletTest.db";
    private ProductsDao productsDao;

    private HttpServlet servlet;
    private static final String EMPTY_RESPONSE = HtmlBuilder.newBuilder().wrapToHtmlTag().build();

    private Writer writer;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void init() {
        writer = new StringWriter();
        request = HttpServletProviders.provideGetRequestWithParams(Collections.emptyMap());
        response = HttpServletProviders.provideResponse(writer);
        productsDao = new JdbcProductsDao(DB_FILE);
        servlet = new GetProductsServlet(productsDao);

        productsDao.createTableIfNotExists();
        productsDao.clearAll();
    }

    @After
    public void close() throws IOException {
        writer.close();
    }

    @Test
    public void correctlyReturnEmpty_Ok() throws ServletException, IOException {
        servlet.service(request, response);

        assertThat(writer.toString(), is(EMPTY_RESPONSE));
    }

    @Test
    public void correctlyReturnProducts_Ok() throws ServletException, IOException {
        Product[] products = IntStream.range(1, 10)
                .mapToObj(i -> new Product(i * 100L, String.format("product no.%d", i)))
                .toArray(Product[]::new);

        Arrays.stream(products).forEach(productsDao::add);

        servlet.service(request, response);

        HtmlBuilder expectedHtml = HtmlBuilder.newBuilder()
                .wrapToHtmlTag();

        Arrays.stream(products)
                .map(Product::toHtml)
                .forEach(expectedHtml::addLine);

        assertThat(writer.toString(), is(expectedHtml.build()));
        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
    }


}
