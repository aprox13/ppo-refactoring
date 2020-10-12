package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.model.QueryCommand;
import ru.akirakozov.sd.refactoring.response.HtmlBuilder;
import ru.akirakozov.sd.refactoring.response.ResponseEnricher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {

    private final ProductsDao productsDao;
    private final Map<QueryCommand, String> titles;
    private final Map<QueryCommand, Consumer<HtmlBuilder>> enrichers;

    public QueryServlet(ProductsDao productsDao) {
        this.productsDao = productsDao;
        titles = new HashMap<>();
        enrichers = new HashMap<>();

        titles.put(QueryCommand.MAX, "<h1>Product with max price: </h1>");
        titles.put(QueryCommand.MIN, "<h1>Product with min price: </h1>");
        titles.put(QueryCommand.SUM, "Summary price: ");
        titles.put(QueryCommand.COUNT, "Number of products: ");

        enrichers.put(QueryCommand.MAX, this::enrichMax);
        enrichers.put(QueryCommand.MIN, this::enrichMin);
        enrichers.put(QueryCommand.COUNT, this::enrichCount);
        enrichers.put(QueryCommand.SUM, this::enrichSum);
    }

    private void enrichMax(HtmlBuilder enricher) {
        Optional.ofNullable(productsDao.max()).map(Product::toHtml).ifPresent(enricher::addLine);
    }

    private void enrichMin(HtmlBuilder enricher) {
        Optional.ofNullable(productsDao.min()).map(Product::toHtml).ifPresent(enricher::addLine);
    }

    private void enrichSum(HtmlBuilder enricher) {
        enricher.addLine(String.valueOf(productsDao.sum()));
    }

    private void enrichCount(HtmlBuilder enricher) {
        enricher.addLine(String.valueOf(productsDao.count()));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String commandRaw = request.getParameter("command");

        QueryCommand command = Arrays.stream(QueryCommand.values())
                .filter(q -> q != QueryCommand.UNKNOWN)
                .filter(q -> q.name().toLowerCase().equals(commandRaw))
                .findFirst()
                .orElse(QueryCommand.UNKNOWN);

        HtmlBuilder body = HtmlBuilder.newBuilder();

        if (command == QueryCommand.UNKNOWN) {
            body.addLine("Unknown command: " + commandRaw);
        } else {
            body.wrapToHtmlTag()
                    .addLine(titles.get(command))
                    .accept(enrichers.get(command));
        }


        ResponseEnricher.newResponseEnricher()
                .withBody(body)
                .withCode(ResponseEnricher.STATUS_CODE_200)
                .withContentType(ResponseEnricher.HTML_CONTENT_TYPE)
                .enrich(response);
    }

}
