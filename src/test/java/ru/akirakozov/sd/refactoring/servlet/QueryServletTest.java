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
import ru.akirakozov.sd.refactoring.model.QueryCommand;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static ru.akirakozov.sd.refactoring.common.TestUtils.mapOf;

@RunWith(MockitoJUnitRunner.class)
public class QueryServletTest {

    private static final String DB_FILE = "test.db";
    private final DBSupport dbSupport = new DBSupport(DB_FILE);
    private ProductsDao productsDao;

    private static final String HTML_TEMPLATE = "<html><body>\n%s</body></html>\n";
    private static final String COUNT_TEMPLATE = String.format(
            HTML_TEMPLATE,
            "Number of products: \n%s"
    );

    private static final String SUM_TEMPLATE = String.format(
            HTML_TEMPLATE,
            "Summary price: \n%s"
    );

    private static final Map<QueryCommand, String> SUM_COUNT_TEMPLATES = mapOf(
            QueryCommand.COUNT, COUNT_TEMPLATE,
            QueryCommand.SUM, SUM_TEMPLATE
    );

    private Writer writer;
    private HttpServletResponse response;
    private final HttpServlet servlet = new QueryServlet();
    private List<Product> products;


    @Before
    public void init() {
        dbSupport.executeScript("create.sql");
        dbSupport.executeScript("clear_products.sql");

        writer = new StringWriter();
        response = HttpServletProviders.provideResponse(writer);

        products = IntStream.range(1, 10)
                .mapToObj(i -> new Product(i * 10L, "product#" + i))
                .collect(Collectors.toList());

        productsDao = new JdbcProductsDao(DB_FILE);
    }

    @After
    public void close() throws IOException {
        writer.close();
    }

    private HttpServletRequest commandRequest(QueryCommand command) {
        return HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap("command", command.toString()));
    }


    private void minMaxSpec(List<Product> products, QueryCommand command, Function<Stream<Product>, Optional<Product>> function) throws ServletException, IOException {
        String answerTemplate = "<h1>Product with %s price: </h1>\n%s";
        String expectedProductHtml;

        if (!products.isEmpty()) {
            products.forEach(productsDao::add);
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            Product expectedProduct = function.apply(products.stream()).get();

            expectedProductHtml = String.format("%s\t%d</br>\n", expectedProduct.getName(), expectedProduct.getPrice());

        } else {
            expectedProductHtml = "";
        }

        String expectedHtml = String.format(
                HTML_TEMPLATE,
                String.format(
                        answerTemplate,
                        command, expectedProductHtml
                )
        );

        HttpServletRequest request = commandRequest(command);
        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is(expectedHtml));

    }

    private void sumCountSpec(
            List<Product> products,
            QueryCommand command,
            Function<Stream<Product>, Long> function
    ) throws ServletException, IOException {
        if (!products.isEmpty()) {
           products.forEach(productsDao::add);
        }

        String expectedAnswer = function.apply(products.stream()).toString();

        HttpServletRequest request = commandRequest(command);

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is(String.format(SUM_COUNT_TEMPLATES.get(command), expectedAnswer + "\n")));
    }

    @Test
    public void correctlyReturnMin_Ok() throws ServletException, IOException {
        minMaxSpec(products, QueryCommand.MIN, productStream -> productStream.min(Comparator.comparing(Product::getPrice)));
    }

    @Test
    public void correctlyReturnMax_Ok() throws ServletException, IOException {
        minMaxSpec(products, QueryCommand.MAX, productStream -> productStream.max(Comparator.comparing(Product::getPrice)));
    }

    @Test
    public void returnEmptyDataMinOnEmptyProduct_Ok() throws ServletException, IOException {
        minMaxSpec(Collections.emptyList(), QueryCommand.MIN, productStream -> productStream.min(Comparator.comparing(Product::getPrice)));
    }

    @Test
    public void returnEmptyDataMaxOnEmptyProduct_Ok() throws ServletException, IOException {
        minMaxSpec(Collections.emptyList(), QueryCommand.MAX, productStream -> productStream.max(Comparator.comparing(Product::getPrice)));
    }

    @Test
    public void correctlyReturnCountOnEmpty_Ok() throws ServletException, IOException {
        sumCountSpec(Collections.emptyList(), QueryCommand.COUNT, Stream::count);
    }

    @Test
    public void correctlyReturnCount_Ok() throws ServletException, IOException {
        sumCountSpec(products, QueryCommand.COUNT, Stream::count);
    }

    @Test
    public void correctlyReturnSumOnEmpty_Ok() throws ServletException, IOException {
        sumCountSpec(
                Collections.emptyList(),
                QueryCommand.SUM,
                productStream -> productStream.mapToLong(Product::getPrice).sum()
                );
    }

    @Test
    public void correctlyReturnSum_Ok() throws ServletException, IOException {
        sumCountSpec(
                products,
                QueryCommand.SUM,
                productStream -> productStream.mapToLong(Product::getPrice).sum()
        );
    }
}
