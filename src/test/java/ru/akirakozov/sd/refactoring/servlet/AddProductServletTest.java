package ru.akirakozov.sd.refactoring.servlet;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.common.DBSupport;
import ru.akirakozov.sd.refactoring.common.HttpServletProviders;
import ru.akirakozov.sd.refactoring.common.ProductDbSupport;
import ru.akirakozov.sd.refactoring.model.Product;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static ru.akirakozov.sd.refactoring.common.TestUtils.mapOf;

@RunWith(MockitoJUnitRunner.class)
public class AddProductServletTest {

    private static final String NAME_PARAM = "name";
    private static final String PRICE_PARAM = "price";

    private final HttpServlet servlet = new AddProductServlet();

    @Before
    public void init() {
        DBSupport.executeScript("create.sql");
        DBSupport.executeScript("clear_products.sql");
    }

    @Test
    public void correctlyAdd_Ok() throws ServletException, IOException {
        assertThat(ProductDbSupport.getRows(), hasSize(0));

        StringWriter writer = new StringWriter();
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(mapOf(NAME_PARAM, "name", PRICE_PARAM, "1000"));
        HttpServletResponse response = HttpServletProviders.provideResponse(writer);

        servlet.service(request, response);

        //noinspection ConstantConditions
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
        Assert.assertEquals(response.getContentType(), "text/html");
        Assert.assertEquals(writer.toString(), "OK\n");

        List<Product> rows = ProductDbSupport.getRows();

        assertThat(rows, contains(new Product(1000L, "name")));
    }

    @Test(expected = NumberFormatException.class)
    public void failOnMissedPrice_Fail() throws ServletException, IOException {
        StringWriter writer = new StringWriter();
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap(NAME_PARAM, "name"));
        HttpServletResponse response = HttpServletProviders.provideResponse(writer);

        servlet.service(request, response);
    }

    @Test
    public void addWithMissedName_Ok() throws ServletException, IOException {
        assertThat(ProductDbSupport.getRows(), hasSize(0));

        StringWriter writer = new StringWriter();
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap(PRICE_PARAM, "1000"));
        HttpServletResponse response = HttpServletProviders.provideResponse(writer);

        servlet.service(request, response);

        //noinspection ConstantConditions
        Assert.assertEquals(response.getStatus(), HttpServletResponse.SC_OK);
        Assert.assertEquals(response.getContentType(), "text/html");
        Assert.assertEquals(writer.toString(), "OK\n");

        List<Product> rows = ProductDbSupport.getRows();

        assertThat(rows, contains(new Product(1000L, "null")));
    }
}
