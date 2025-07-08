package sample;

@Deprecated
@MyAnno(val="old")
class OldClass {
    void method() {
        int a = 1;
        int b = 2;
        System.out.println(a + b);
        System.err.println("oops");
    }
}
