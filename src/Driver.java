import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import oracle.soa.management.facade.*;
import oracle.soa.management.facade.bpel.*;
import oracle.soa.management.util.*;
import weblogic.xml.saaj.util.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: ucanian
 * Date: 6/5/13
 * Time: 7:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class Driver {
    public Driver(){}
    private static org.apache.log4j.Logger logger;
    public String htmlContent(){
        String jsonText="";
        String filename="cert.json";
        String ret="";
        logger= Logger.getLogger("driver");
        try{
            Properties props = new Properties();
            props.load(Driver.class.getResourceAsStream("log4j.properties"));
            PropertyConfigurator.configure(props);
        }
        catch(Exception e){
            String error="Error in reading log4j.properties";
            logger.error(error,e);
            return error;
        }
        InputStream in=null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{
            in=Driver.class.getResourceAsStream(filename);
            jsonText=new String(IOUtils.toByteArray(in));
            logger.info(jsonText);
        }
        catch (Exception e){
            String error="Error in reading input";
            logger.error(error,e);
            return error;
        }
        Gson gson=new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(jsonText).getAsJsonArray();
        for(int i=0;i<array.size();i++){
            EnvParams ep=gson.fromJson(array.get(i),EnvParams.class);
            try{
                Env e=new Env(ep);
                List<CompositeInstance> instances=e.getFilteredInstances();
                ComponentInstanceFilter instanceFilter = new ComponentInstanceFilter();
                List<SoaInstance> instance_list=new ArrayList<SoaInstance>();
                for(CompositeInstance instance:instances){
                    List<ComponentInstance> l=instance.getChildComponentInstances(instanceFilter);
                    if(l.size()>0){
                        //String xml= String.valueOf(l.get(0).getAuditTrail());
                        instance_list.add(new SoaInstance(l.get(0)));
                        //ret+=String.valueOf(instance_list.get(instance_list.size()-1));
                        ret+=instance_list.get(instance_list.size()-1).toHtml();
                    }

                }
            }
            catch(Exception e){
                //e.printStackTrace();
                return e.getMessage();
            }
        }
        return ret;
    }
    public static void main(String args[]){
        Driver d=new Driver();
        System.out.println(wrapper(d.htmlContent()));
    }
    public static String css(){
        return "table,tr,td\n" +
                "{\n" +
                "border: 1px solid black;\n" +
                "}";
    }
    public static String head(){
        return "<title>Soa Browser</title>\n" +
                "<script type=\"text/javascript\" src=\"jquery-2.0.2.min.js\"></script>\n" +
                "<style>\n" +
                css()+
                "</style>\n" +
                "      <script type=\"text/javascript\">\n" +
                "            $(document).ready(function() {\n" +
                "            \n" +
                "                function getChildren($row) {\n" +
                "                    var children = [], level = $row.attr('data-level');\n" +
                "                    while($row.next().attr('data-level') > level) {\n" +
                "      \n" +
                "      \t\t if($row.next().attr('data-level') == parseInt(level)+1){\n" +
                "                         children.push($row.next());\n" +
                "      \t\t}\n" +
                "                         $row = $row.next();\n" +
                "                    }            \n" +
                "                    return children;\n" +
                "                }        \n" +
                "            \n" +
                "                $('.parent').on('click', function() {\n" +
                "                \n" +
                "                    var children = getChildren($(this));\n" +
                "                    $.each(children, function() {\n" +
                "                        $(this).toggle();\n" +
                "                    })\n" +
                "                });\n" +
                "                 $(\"[data-level]\").hide();\n" +
                "                 $(\"[data-level='0']\").show();\n" +
                "                \n" +
                "            })</script>";
    }
    public static String wrapper(String body){
        return "<html>"+
                head()+
                "<body>"+
                "<table>"+
                body+
                "</table>"+
                "</body>"+
                "</html>";
    }
}
