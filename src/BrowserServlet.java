

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

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
        Map params=request.getParameterMap();
        boolean show=false;
        String env="ppe";
        String min=null;
        String max=null;
        String state=null;
        if(params.containsKey("show")){
            String[] vals=(String[])params.get("show");
            if(vals[0].equals("true")){
                show=true;
            }
        }
        if(params.containsKey("env")){
            String[] vals=(String[])params.get("env");
            if(vals[0].equals("cert") || vals[0].equals("ppe") || vals[0].equals("prod")){
                env=vals[0];
            }
        }
        if(params.containsKey("state")){
            String[] vals=(String[])params.get("state");
            state=vals[0];
        }
        if(params.containsKey("minRelativeToNowInMinutes")){
            String[] vals=(String[])params.get("minRelativeToNowInMinutes");
            min=vals[0];
        }
        if(params.containsKey("maxRelativeToNowInMinutes")){
            String[] vals=(String[])params.get("maxRelativeToNowInMinutes");
            max=vals[0];
        }
        Driver d=new Driver(show);
        d.htmlContent(out,env,min,max,state);
    }
    public void init() throws ServletException{

    }
    public void destroy(){

    }
}
