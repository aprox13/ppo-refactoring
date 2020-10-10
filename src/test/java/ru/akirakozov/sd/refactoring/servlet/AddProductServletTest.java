package ru.akirakozov.sd.refactoring.servlet;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.common.DBSupport;
import ru.akirakozov.sd.refactoring.common.HttpServletProviders;
import ru.akirakozov.sd.refactoring.common.ProductDbSupport;
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
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static ru.akirakozov.sd.refactoring.common.TestUtils.mapOf;

@RunWith(MockitoJUnitRunner.class)
public class AddProductServletTest {

    private static final String NAME_PARAM = "name";
    private static final String PRICE_PARAM = "price";

    private final HttpServlet servlet = new AddProductServlet();
    private Writer writer;
    private HttpServletResponse response;

    @Before
    public void init() {
        DBSupport.executeScript("create.sql");
        DBSupport.executeScript("clear_products.sql");

        writer = new StringWriter();
        response = HttpServletProviders.provideResponse(writer);
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
        assertThat(ProductDbSupport.getRows(), hasSize(0));

        Product product = new Product(1000L, "name");
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(productToParams(product));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("OK\n"));

        List<Product> rows = ProductDbSupport.getRows();
        assertThat(rows, contains(product));
    }

    @Test(expected = NumberFormatException.class)
    public void failOnMissedPrice_Fail() throws ServletException, IOException {
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap(NAME_PARAM, "name"));

        servlet.service(request, response);
    }

    @Test
    public void addWithMissedName_Ok() throws ServletException, IOException {
        assertThat(ProductDbSupport.getRows(), hasSize(0));

        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap(PRICE_PARAM, "1000"));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("OK\n"));

        List<Product> rows = ProductDbSupport.getRows();

        assertThat(rows, contains(new Product(1000L, "null")));
    }

    @Test
    public void doesnotFailOnDuplication_Fail() throws ServletException, IOException {
        Product product = new Product(10L, "duplication");
        ProductDbSupport.addProducts(product);

        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(productToParams(product));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("OK\n"));

        List<Product> rows = ProductDbSupport.getRows();

        assertThat(rows, contains(product, product));
    }
}
