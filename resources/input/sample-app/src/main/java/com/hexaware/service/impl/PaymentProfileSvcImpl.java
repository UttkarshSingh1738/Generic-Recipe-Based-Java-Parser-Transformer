package com.hexaware.service.impl;

import com.hexaware.factory.PaymentQuickPayPaymentRespondSvcFactory;
import com.hexaware.service.PaymentProfileSvc;
import com.hexaware.service.PaymentQuickPayPaymentRespondSvc;

//@Component("PaymentProfileSvc")
public class PaymentProfileSvcImpl implements PaymentProfileSvc {

    private final PaymentQuickPayPaymentRespondSvc svc;

//    @Autowired
//    @Qualifier("PaymentQuickPayPaymentRespondSvc")
//    private PaymentQuickPayPaymentRespondSvc svc;

    public PaymentProfileSvcImpl() {
        svc = PaymentQuickPayPaymentRespondSvcFactory
                .getService(PaymentQuickPayPaymentRespondSvc.class, "PaymentQuickPayPaymentRespondSvc");
    }

//    public PaymentProfileSvcImpl() {
//        svc = PaymentQuickPayPaymentRespondSvcFactory
//                .getService(PaymentQuickPayPaymentRespondSvc.class, "PaymentQuickPayPaymentRespondSvc");
//    }

    @Override
    public String getUserName() {
        return svc.getUserName();
    }
}
