
    import javax.security.cert.X509Certificate ;

    public class SpecTests {

    void testToolProviderCalls() {
        // Instance creation + method call
        ToolProvider provider = new ToolProvider();
        JavaCompiler compiler1 = provider.getSystemJavaCompiler();
        DocumentationTool docTool1 = provider.getSystemDocumentationTool();
        ClassLoader loader1 = provider.getSystemToolClassLoader();

        // Direct instantiation + method call  
        JavaCompiler compiler2 = new ToolProvider().getSystemJavaCompiler();
        DocumentationTool docTool2 = new ToolProvider().getSystemDocumentationTool();
        ClassLoader loader2 = new ToolProvider().getSystemToolClassLoader();

        // Variable-based calls
        ToolProvider tp = getToolProvider();
        JavaCompiler compiler3 = tp.getSystemJavaCompiler();
    }

    void testModifierCalls() {
        // Instance-style Modifier calls
        Modifier mod = new Modifier();
        boolean isPublic = mod.isPublic(123);
        boolean isStatic = mod.isStatic(456);
        boolean isPrivate = mod.isPrivate(789);

        // Method call on new instance
        boolean isFinal = new Modifier().isFinal(999);
        String toString = new Modifier().toString(111);
    }

    void testConstantBootstrapsCalls() {
        // Instance-style ConstantBootstraps calls
        ConstantBootstraps bootstraps = new ConstantBootstraps();
        Object primitive = bootstraps.primitiveClass(lookup, "int", int.class);
        Object enumConstant = bootstraps.enumConstant(lookup, "VALUE", Enum.class);

        // Direct instantiation calls
        Object primitive2 = new ConstantBootstraps().primitiveClass(lookup, "int", int.class);
    }

    void testEdgeCases() {
        // Chained calls (should only convert the matching method)
        String result = new ToolProvider().getSystemJavaCompiler().toString();

        // Nested calls
        process(new ToolProvider().getSystemJavaCompiler());

        // Already static calls (should be ignored)
        JavaCompiler staticCompiler = ToolProvider.getSystemJavaCompiler();
    }

    private ToolProvider getToolProvider() {
        return new ToolProvider();
    }

    private void process(Object obj) {
    }

    public void foo() {
        Runtime rt = Runtime.getRuntime();
        rt.traceInstructions(true);
        rt.traceMethodCalls(false);
        System.out.println("done");
    }

    public void example(LogRecord rec) {
        int t = rec.getThreadID();
        rec.setThreadID(42);
    }

    // override in a subclass:
    public static class MyLR extends LogRecord {

        @Override
        public int getThreadID() {
            return super.getThreadID();
        }

        @Override
        public void setThreadID(int id) {
            super.setThreadID(id);
        }
    }

    //Expected output
    // public void example(LogRecord rec) {
    //     long t = rec.getLongThreadID();
    //     rec.setLongThreadID(42);
    // }
    // // override in a subclass:
    // public static class MyLR extends LogRecord {
    //     @Override
    //     public long getLongThreadID() { return super.getLongThreadID(); }
    //     @Override
    //     public void setLongThreadID(int id) { super.setLongThreadID(id); }
    // }

    public class CertTest {

        javax.security.cert.X509Certificate cert;

        public void set(javax.security.cert.X509Certificate c) {
            this.cert = c;
        }
    }

    //Expected
    // import java.security.cert.X509Certificate;
    // public class CertTest {
    //     java.security.cert.X509Certificate cert;

    //     public void set(java.security.cert.X509Certificate c) {
    //         this.cert = c;
    //     }
    // }


}
