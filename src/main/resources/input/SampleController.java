// BEFORE TRANSFORMATION: Should become @RestController
import org.springframework.stereotype.Controller;
// BEFORE TRANSFORMATION: Should become @Autowired
import javax.inject.Inject;
// BEFORE TRANSFORMATION: Should be removed
import org.springframework.stereotype.mvc.MvcController;
// BEFORE TRANSFORMATION: Should become @Qualifier, @Autowired
import org.springframework.web.client.InjectRestServiceClient;

// BEFORE TRANSFORMATION: Should be updated to jakarta.*
import javax.xml.XMLConstants;
import javax.xml.validation.Validator;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

// BEFORE TRANSFORMATION: Should change class usages and imports
import org.apache.logging.log4j.CustomLogManager;
import org.apache.http.impl.client.DefaultHttpClient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

// SHOULD NOT TRANSFORM: In void ambiguousHandler()
import com.example.Date;
import com.example.DefaultHttpClient;

@Controller
@MvcController
@Path("/sample")
public class SampleController {

    @Inject
    private final ProductService productService = null;

    @InjectRestServiceClient("logManager")
    private CustomLogManager customLogManager;

    private static final org.apache.logging.log4j.Logger logger = CustomLogManager.getLogger(SampleController.class);

    @PostConstruct
    public void init() {
        // Placeholder
    }
    @PreDestroy
    public void destroy() {
        // Placeholder
    }

    @GET
    @Produces("application/json")
    public String getHandler(@NotNull HttpServletRequest request) {
        DefaultHttpClient client = new DefaultHttpClient(); // Should become HttpClient
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Should become DateTimeFormatter
        Date date = new Date(); // Should become LocalDate
        Calendar cal = Calendar.getInstance(); // Should become ZonedDateTime

        logger.info("Handling GET at " + XMLConstants.XML_NS_PREFIX); // XMLConstants should update

        return "Handled GET";
    }

    @POST
    @Consumes("application/json")
    public void postHandler() {
        Validator validator = null; // javax.xml.validation.Validator to jakarta.xml.validation.Validator
    }

    public void ambiguousHandler() {
        com.example.Date myDate = new com.example.Date(); // <-- AMBIGUOUS: Should NOT become LocalDate.now()
        com.example.DefaultHttpClient myClient = new com.example.DefaultHttpClient(); // <-- AMBIGUOUS: Should NOT become HttpClient
        Date utilDate = new Date(); // Should become LocalDate.now() (java.util.Date)
        DefaultHttpClient utilClient = new DefaultHttpClient(); // Should become HttpClient (org.apache.http.impl.client.DefaultHttpClient)
    }
}
