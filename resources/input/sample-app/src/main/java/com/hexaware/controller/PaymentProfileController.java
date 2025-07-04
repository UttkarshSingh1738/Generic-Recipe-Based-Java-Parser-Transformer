package com.hexaware.controller;

import org.springframework.ejb.access.EjbAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.hexaware.factory.DigitalPaymentProfileSvcFactory;
import com.hexaware.factory.JPAStoredProcedure;
import com.hexaware.factory.JPAStoredProcedureFactory;
import com.hexaware.factory.PaymentProfileSvcFactory;
import com.hexaware.model.Constants;
import com.hexaware.model.User;
import com.hexaware.service.DigitalPaymentProfileSvc;
import com.hexaware.service.PaymentProfileSvc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RequestMapping(value = "/paymentProfileController")
@Api(tags = "payment-profile-v1")
public class PaymentProfileController {

    private final static PaymentProfileSvc paymentProfileSvc = PaymentProfileSvcFactory.getService(PaymentProfileSvc.class, PaymentProfileSvcFactory.PaymentProfileSvc);

//    @Autowired
//    @Qualifier(PaymentProfileSvcFactory.PaymentProfileSvc)
//    private PaymentProfileSvc paymentProfileSvc;


    @ApiOperation(value = "/user", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK")})
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String user(@Validated User user, Model model) {
        DigitalPaymentProfileSvc digitalPaymentProfileSvc = DigitalPaymentProfileSvcFactory
                .getService(DigitalPaymentProfileSvc.class, DigitalPaymentProfileSvcFactory.DigitalPaymentProfileSvc);

//        DigitalPaymentProfileSvc digitalPaymentProfileSvc = SpringContext.getBean(DigitalPaymentProfileSvcFactory.DigitalPaymentProfileSvc);

        System.out.println("User Page Requested");
        model.addAttribute("userName", user.getUserName());

        String testValue = digitalPaymentProfileSvc.getUserName();
        String merchantValue = paymentProfileSvc.getUserName();

        try {
            JPAStoredProcedure sp = JPAStoredProcedureFactory.getInstance(
                    Constants.ams).getStoredProcedure(
                    new StringBuffer(Constants.CORE).append(
                                    Constants.SP_PROC_NAME)
                            .toString());
//            JPAStoredProcedure sp = JPAStoredProcedureFactory.getInstance().getStoredProcedure(
//                    new StringBuffer(Constants.CORE).append(
//                                    Constants.SP_PROC_NAME)
//                            .toString());

//            JPAStoredProcedure sp = JPAStoredProcedureFactory.getStoredProcedure(
//                    new StringBuffer(Constants.CORE).append(
//                                    Constants.SP_PROC_NAME)
//                            .toString());

        } catch (EjbAccessException | ArrayIndexOutOfBoundsException e) {
//        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
        try {
            JPAStoredProcedure sp = JPAStoredProcedureFactory.getInstance(
                    Constants.ams).getStoredProcedure(
                    new StringBuffer(Constants.CORE).append(
                                    Constants.SP_PROC_NAME)
                            .toString());
        } catch (EjbAccessException e) {
            throw new RuntimeException(e);
        }
        return "user";
    }
}
