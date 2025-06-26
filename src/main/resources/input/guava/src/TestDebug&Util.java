package input.guava.src;

class TestExample {
    String greeting(String name) {
        return Optional.ofNullable(name)
                       .orElse("world");
    }
}

public class FooUtil {
    public static void help() {
        int sum = 1 + 2;
        System.out.println("debug!");
    }
}

class Demo {
    void foo() {
        System.out.println("hello");
        System.err.println("oops");
    }
}
