package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.response.HtmlBuilder;
import ru.akirakozov.sd.refactoring.response.ResponseEnricher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    private final ProductsDao productsDao;

    public GetProductsServlet(ProductsDao productsDao) {
        this.productsDao = productsDao;
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Product> products = productsDao.getAll();


        HtmlBuilder body = HtmlBuilder.newBuilder()
                .wrapToHtmlTag()
                .addLines(products.stream().map(Product::toHtml).collect(Collectors.toList()));

        ResponseEnricher.newResponseEnricher()
                .withContentType(ResponseEnricher.HTML_CONTENT_TYPE)
                .withCode(ResponseEnricher.STATUS_CODE_200)
                .withBody(body)
                .enrich(response);
    }
}
