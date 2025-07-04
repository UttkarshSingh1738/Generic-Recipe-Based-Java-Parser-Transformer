package com.hexaware.factory;

public class JPAStoredProcedureFactory {
    private JPAStoredProcedureFactory() {
    }

    public static JPAStoredProcedureFactory getInstance() {
        return new JPAStoredProcedureFactory();
    }

    public static JPAStoredProcedureFactory getInstance(String ams) {
        return new JPAStoredProcedureFactory();
    }

    public JPAStoredProcedure getStoredProcedure(String string) {
        return new JPAStoredProcedure();
    }
}
