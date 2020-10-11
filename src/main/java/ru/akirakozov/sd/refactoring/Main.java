package ru.akirakozov.sd.refactoring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.dao.JdbcProductsDao;
import ru.akirakozov.sd.refactoring.dao.ProductsDao;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

/**
 * @author akirakozov
 */
public class Main {

    public static final String DB_FILE = "test.db";
    public static final int PORT = 8081;

    public static void main(String[] args) throws Exception {
        ProductsDao productsDao = new JdbcProductsDao(DB_FILE);
        productsDao.createTableIfNotExists();

        Server server = new Server(PORT);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(productsDao)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(productsDao)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(productsDao)),"/query");

        server.start();
        server.join();
    }
}
