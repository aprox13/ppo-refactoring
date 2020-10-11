package ru.akirakozov.sd.refactoring.model;

import java.util.Objects;

public class Product {
    private Long price;
    private String name;

    public Product(long price, String name) {
        this.price = price;
        this.name = name;
    }

    public Product() {
        price = 0L;
        name = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(price, product.price) &&
                Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, name);
    }

    @Override
    public String toString() {
        return "Product{" +
                "price=" + price +
                ", name='" + name + '\'' +
                '}';
    }

    public Long getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String toHtml() {
        return getName() + "\t" + getPrice() + "</br>";
    }
}
