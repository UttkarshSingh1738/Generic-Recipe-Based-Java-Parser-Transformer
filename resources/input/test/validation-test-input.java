public class ValidationTestInput {
    
    // This will create a pattern variable that IS used - should pass validation
    void goodExample(Object obj) {
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("Length: " + s.length());
        }
    }
    
    // This will create a pattern variable that is NOT used - should trigger validation error and rollback
    void badExample1(Object obj) {
        if (obj instanceof Integer) {
            Integer i = (Integer) obj; // This creates pattern variable but...
            System.out.println("Found an integer"); // ...pattern variable is never used
        }
    }
    
    // Another case that should trigger validation error
    void badExample2(Object obj) {
        if (obj instanceof Double) {
            Double d = (Double) obj; // Pattern variable created but not used
            System.out.println("Processing number");
        }
    }
    
    // This should also fail validation
    void badExample3(Object obj) {
        if (obj instanceof String) {
            String str = (String) obj; // Pattern variable created but not used
            System.out.println("Processing text");
        }
    }
}
