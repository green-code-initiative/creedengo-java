import java.util.List;

class AvoidHibernateLazyRelationAccessInLoopCheckSample {

    void badForEachLoop(List<Product> products) {
        for (Product product : products) {
            product.getOrders(); // Noncompliant
        }
    }

    void badForLoop(List<Product> products) {
        for (int i = 0; i < products.size(); i++) {
            products.get(i).getOrders(); // Noncompliant
        }
    }

    void badStreamForEach(List<Product> products) {
        products.forEach(product -> {
            product.getOrders(); // Noncompliant
        });
    }

    void badStreamMap(List<Product> products) {
        products.stream()
                .map(product -> product.getOrders().size()); // Noncompliant
    }

    void goodSimpleGetter(List<Product> products) {
        for (Product product : products) {
            product.getName();
        }
    }

    void goodIdGetter(List<Product> products) {
        for (Product product : products) {
            product.getId();
        }
    }

    class Product {
        List<Order> getOrders() {
            return null;
        }

        String getName() {
            return "";
        }

        Long getId() {
            return 1L;
        }
    }

    class Order {
    }
}