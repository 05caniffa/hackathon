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
    boolean show=false;
    public Driver(boolean show){
        this.show=show;
    }
    private static org.apache.log4j.Logger logger;
    public void htmlContent(String env){
        htmlContent(new PrintWriter(System.out,true),env);
    }
    public void htmlContent(PrintWriter out,String env){
        String jsonText="";
        String filename=env+".json";
        //String ret="";
        logger= Logger.getLogger("driver");
        try{
            Properties props = new Properties();
            props.load(Driver.class.getResourceAsStream("log4j.properties"));
            PropertyConfigurator.configure(props);
        }
        catch(Exception e){
            String error="Error in reading log4j.properties";
            logger.error(error,e);
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
        }
        before(out);
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
                        if (l.get(0).getComponentName().equals("CourseMgmt") || l.get(0).getComponentName().equals("RegEnroll")){
                            instance_list.add(new SoaInstance(l.get(0),show));
                            out.println(instance_list.get(instance_list.size()-1).toHtml());
                        }
                    }
                }
            }
            catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        after(out);
    }
    public static String css(){
        return "<style>\n"+
                "#hor-minimalist-b\n" +
                "    {\n" +
                "    font-family: \"Lucida Sans Unicode\", \"Lucida Grande\", Sans-Serif;\n" +
                "    font-size: 12px;\n" +
                "    background: #fff;\n" +
                "    margin: 45px;\n" +
                "    width: 480px;\n" +
                "    border-collapse: collapse;\n" +
                "    text-align: left;\n" +
                "    }\n" +
                "    #hor-minimalist-b th\n" +
                "    {\n" +
                "    font-size: 14px;\n" +
                "    font-weight: normal;\n" +
                "    color: #039;\n" +
                "    padding: 6px 22px;\n" +
                "    border-bottom: 2px solid #6678b1;\n" +
                "    }\n" +
                "    #hor-minimalist-b td\n" +
                "    {\n" +
                "    border-bottom: 1px solid #ccc;\n" +
                "    color: #669;\n" +
                "    padding: 6px 22px;\n" +
                "    min-width: 70px;\n" +
                "    white-space: nowrap;\n" +
                "    }\n" +
                "    #hor-minimalist-b tbody tr:hover td\n" +
                "    {\n" +
                "    color: #009;\n" +
                "    }\n" +
                "    #hor-minimalist-b textarea\n" +
                "    {\n" +
                "    min-width: 100%;\n" +
                "    min-height: 200px;\n" +
                "    resize:none;\n" +
                "    border:none;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+1) .tablesorter-filter {\n" +
                "    width: 80px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+2) .tablesorter-filter {\n" +
                "    width: 200px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+3) .tablesorter-filter {\n" +
                "    width: 80px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+4) .tablesorter-filter {\n" +
                "    width: 80px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+5) .tablesorter-filter {\n" +
                "    width: 40px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+6) .tablesorter-filter {\n" +
                "    width: 120px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+7) .tablesorter-filter {\n" +
                "    width: 180px;\n" +
                "    }\n" +
                "    .tablesorter .tablesorter-filter-row td:nth-child(8n+8) .tablesorter-filter {\n" +
                "    width: 50px;\n" +
                "    }\n"+
                "</style>\n";
    }
    public static String head(){
        return "<title>Soa Browser</title>\n" +
                "<script type=\"text/javascript\" src=\"http://code.jquery.com/jquery-2.0.2.min.js\"></script>\n" +
                "<script type=\"text/javascript\" src=\"http://mottie.github.io/tablesorter/js/jquery.tablesorter.js\"></script>\n" +
                "<script type=\"text/javascript\" src=\"http://mottie.github.io/tablesorter/js/jquery.tablesorter.widgets.js\"></script>\n"+
                css()+
                js();
    }
    public static String js(){
        return "<script type=\"text/javascript\">\n" +
                "\n" +
                "    $(document).ready(function() {\n" +
                "    $('table')\n" +
                "    .bind('filterEnd', function () {\n" +
                "    var f = $.tablesorter.getFilters( $(this) );\n" +
                "    var empty=true\n" +
                "    for (var i = 0; i < f.length; i++) {\n" +
                "    if(f[i] != \"\"){\n" +
                "    empty=false;\n" +
                "    }\n" +
                "    }\n" +
                "    if(empty){\n" +
                "    console.log(\"empty\");\n" +
                "    $(\"[data-level]\").hide();\n" +
                "    $(\"[data-level='0']\").show();\n" +
                "    }\n" +
                "    else{\n" +
                "    console.log(\"not empty\");\n" +
                "    $(\"[data-level='1']\").hide();\n" +
                "    $(\"[data-level='2']\").hide();\n" +
                "    }\n" +
                "    });\n" +
                "\n" +
                "\n" +
                "    $(\"table\").tablesorter({\n" +
                "    cssChildRow: 'nosort',\n" +
                "    widgets: [\"zebra\", \"filter\"],\n" +
                "    widgetOptions : {\n" +
                "    // include child row content while filtering, if true\n" +
                "    filter_childRows  : false,\n" +
                "    // search from beginning\n" +
                "    filter_startsWith : true,\n" +
                "    filter_columnFilters : true,\n" +
                "    }\n" +
                "    });\n" +
                "\n" +
                "    function getChildren($row) {\n" +
                "    var children = [], level = $row.attr('data-level');\n" +
                "    while($row.next().attr('data-level') > level) {\n" +
                "\n" +
                "    if($row.next().attr('data-level') == parseInt(level)+1){\n" +
                "    children.push($row.next());\n" +
                "    }\n" +
                "    $row = $row.next();\n" +
                "    }\n" +
                "    return children;\n" +
                "    }\n" +
                "\n" +
                "    $('.parent').on('click', function() {\n" +
                "\n" +
                "    var children = getChildren($(this));\n" +
                "    $.each(children, function() {\n" +
                "    if($(this).is(\":visible\")){\n" +
                "    var subchilds=getChildren($(this))\n" +
                "    $.each(subchilds, function() {\n" +
                "    $(this).hide()\n" +
                "    })\n" +
                "    }\n" +
                "    $(this).toggle();\n" +
                "    })\n" +
                "    });\n" +
                "    $(\"[data-level]\").hide();\n" +
                "    $(\"[data-level='0']\").show();\n" +
                "    })\n" +
                "</script>";
    }
    public static String thead(){
        return  "<thead>" +
                "<tr>"+
                "<th>"+"instance id"+"</th>"+
                "<th>"+"txid"+"</th>"+
                "<th>"+"BPEL"+"</th>"+
                "<th>"+"course id"+"</th>"+
                "<th>"+"course provider"+"</th>"+
                "<th>"+"state"+"</th>"+
                "<th>"+"time"+"</th>"+
                "<th>"+"duration"+"</th>"+
                "</tr>" +
                "</thead>";
    }
    public static String wrapper(String data){
        return "<html>"+
                head()+
                "<body>"+
                "<table id=\"hor-minimalist-b\">"+
                thead()+
                data+
                "</table>"+
                "</body>"+
                "</html>";
    }
    public static void before(PrintWriter out){
        out.println("<html>"+
                head()+
                "<body>"+
                "<table id=\"hor-minimalist-b\">"+
                thead());
    }
    public static void after(PrintWriter out){
        out.println("</table>"+
                "</body>"+
                "</html>");
    }
    public static void main(String args[]){
        Driver d=new Driver(true);
        d.htmlContent("cert");
    }
}
