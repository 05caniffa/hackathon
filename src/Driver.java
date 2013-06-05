import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import oracle.soa.management.facade.*;
import oracle.soa.management.facade.bpel.*;
import oracle.soa.management.util.*;

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
        PropertyConfigurator.configure("log4j.properties");
        logger.info("Before try");
        String path="before try";
        try{
            File blah=new File(filename);
            path=blah.getAbsolutePath();
            logger.info(path);
            byte[] buffer = new byte[(int) new File(filename).length()];
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(filename));
            f.read(buffer);
            jsonText=new String(buffer);
            logger.info(jsonText);
        }
        catch (Exception e1){
            String error="Error in reading input";
            logger.error(error,e1);
            return path;
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
                        String xml= String.valueOf(l.get(0).getAuditTrail());
                        logger.info(xml);
                        instance_list.add(new SoaInstance(xml));
                        ret+=String.valueOf(instance_list.get(instance_list.size()-1));
                    }

                }
            }
            catch(Exception e){
                e.printStackTrace();
                return e.getMessage();
            }
        }
        return ret;
    }
    public static void main(String args[]){
        Driver d=new Driver();
        System.out.println(d.htmlContent());
    }
}
