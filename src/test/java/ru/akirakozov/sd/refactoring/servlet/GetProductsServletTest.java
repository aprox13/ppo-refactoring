package ru.akirakozov.sd.refactoring.servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.common.*;
import ru.akirakozov.sd.refactoring.model.Product;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class GetProductsServletTest {


    private final HttpServlet servlet = new GetProductsServlet();
    private static final String RESULT_TEMPLATE = "<html><body>\n%s</body></html>\n";
    private static final String EMPTY_RESPONSE = String.format(RESULT_TEMPLATE, "");

    private Writer writer;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void init() {
        DBSupport.executeScript("create.sql");
        DBSupport.executeScript("clear_products.sql");

        writer = new StringWriter();
        request = HttpServletProviders.provideGetRequestWithParams(Collections.emptyMap());
        response = HttpServletProviders.provideResponse(writer);
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

        ProductDbSupport.addProducts(products);

        servlet.service(request, response);

        String productsHtml = Arrays.stream(products)
                .map(p -> String.format("%s\t%d</br>\n", p.getName(), p.getPrice()))
                .collect(Collectors.joining());

        String expected = String.format(RESULT_TEMPLATE, productsHtml);

        assertThat(writer.toString(), is(expected));
        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
    }


}
