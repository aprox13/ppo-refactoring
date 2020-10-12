package ru.akirakozov.sd.refactoring.servlet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ru.akirakozov.sd.refactoring.dao.JdbcProductsDao;
import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.common.HttpServletProviders;
import ru.akirakozov.sd.refactoring.common.SuccessfulHtmlMatcher;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.model.QueryCommand;
import ru.akirakozov.sd.refactoring.response.HtmlBuilder;

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
import static org.hamcrest.core.IsNot.not;
import static ru.akirakozov.sd.refactoring.common.TestUtils.mapOf;

@RunWith(MockitoJUnitRunner.class)
public class QueryServletTest {

    private static final String DB_FILE = "QueryServletTest.db";
    private ProductsDao productsDao;

    private static final Map<QueryCommand, String> SUM_COUNT_TEMPLATES = mapOf(
            QueryCommand.COUNT, "Number of products: ",
            QueryCommand.SUM, "Summary price: "
    );

    private Writer writer;
    private HttpServletResponse response;
    private HttpServlet servlet;
    private List<Product> products;


    @Before
    public void init() {
        writer = new StringWriter();
        response = HttpServletProviders.provideResponse(writer);

        products = IntStream.range(1, 10)
                .mapToObj(i -> new Product(i * 10L, "product#" + i))
                .collect(Collectors.toList());

        productsDao = new JdbcProductsDao(DB_FILE);
        servlet = new QueryServlet(productsDao);

        productsDao.createTableIfNotExists();
        productsDao.clearAll();
    }

    @After
    public void close() throws IOException {
        writer.close();
    }

    private HttpServletRequest commandRequest(QueryCommand command) {
        return HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap("command", command.toString()));
    }


    private void minMaxSpec(List<Product> products, QueryCommand command, Function<Stream<Product>, Optional<Product>> function) throws ServletException, IOException {
        HtmlBuilder expected = HtmlBuilder.newBuilder()
                .wrapToHtmlTag()
                .addLine(String.format("<h1>Product with %s price: </h1>", command.name().toLowerCase()));

        if (!products.isEmpty()) {
            products.forEach(productsDao::add);

            function.apply(products.stream()).map(Product::toHtml).ifPresent(expected::addLine);
        }

        HttpServletRequest request = commandRequest(command);
        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is(expected.build()));

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

        HtmlBuilder htmlBuilder = HtmlBuilder.newBuilder()
                .wrapToHtmlTag()
                .addLine(SUM_COUNT_TEMPLATES.get(command))
                .addLine(expectedAnswer);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is(htmlBuilder.build()));
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

    @Test
    public void correctlyReturnUnknownCommand_Ok() throws ServletException, IOException {
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.singletonMap("command", "cmd"));

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("Unknown command: cmd\n"));
    }

    @Test
    public void correctlyReturnUnknownCommandOnNull_Ok() throws ServletException, IOException {
        HttpServletRequest request = HttpServletProviders.provideGetRequestWithParams(Collections.emptyMap());

        servlet.service(request, response);

        assertThat(response, SuccessfulHtmlMatcher.isSuccessfulHtml());
        assertThat(writer.toString(), is("Unknown command: null\n"));
    }

    private void commandSupported(QueryCommand queryCommand) {
        Writer printWriter = new StringWriter();
        HttpServletResponse resp = HttpServletProviders.provideResponse(printWriter);
        HttpServletRequest r =
                HttpServletProviders.provideGetRequestWithParams(
                        Collections.singletonMap("command", queryCommand.name().toLowerCase()
                        )
                );

        try {
            servlet.service(r, resp);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        String wrong = "Unknown command: " + queryCommand.name().toLowerCase() + "\n";

        assertThat(printWriter.toString(), not(is(wrong)));
    }

    @Test
    public void allCommandSupported_Ok() {
        Arrays.stream(QueryCommand.values())
                .filter(q -> q != QueryCommand.UNKNOWN)
                .forEach(this::commandSupported);
    }
}
