import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import oracle.soa.management.facade.ComponentInstance;
import oracle.soa.management.facade.CompositeInstance;
import oracle.soa.management.util.ComponentInstanceFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ucanian
 * Date: 6/5/13
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrowserServlet extends HttpServlet {
    private static org.apache.log4j.Logger logger;
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out=response.getWriter();
        Driver d=new Driver();
        out.println(d.htmlContent());
    }
    public void init() throws ServletException{

    }
    public void destroy(){

    }
}
