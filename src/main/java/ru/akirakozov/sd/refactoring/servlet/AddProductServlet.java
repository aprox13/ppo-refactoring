package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.model.Product;
import ru.akirakozov.sd.refactoring.utils.Html200ResponseEnricher;
import ru.akirakozov.sd.refactoring.utils.ProductConverterUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class AddProductServlet extends HttpServlet {

    private final ProductsDao productsDao;

    public AddProductServlet(ProductsDao productsDao) {
        this.productsDao = productsDao;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Product product = ProductConverterUtils.fromHttpRequest(request);
        productsDao.add(product);

        Html200ResponseEnricher.newResponseEnricher()
                .addLine("OK")
                .enrich(response);
    }
}
