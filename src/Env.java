/**
 * Created with IntelliJ IDEA.
 * User: ucanian
 * Date: 6/5/13
 * Time: 7:48 AM
 * To change this template use File | Settings | File Templates.
 */
import java.util.*;
import java.io.*;
import javax.naming.Context;
import oracle.soa.management.facade.*;
import oracle.soa.management.facade.bpel.*;
import oracle.soa.management.util.*;
import com.google.gson.*;
import org.jasypt.util.text.BasicTextEncryptor;
import org.apache.log4j.*;


public class Env {
    private Locator locator=null;
    private Hashtable props;
    private List<CompositeInstance> instances=null;
    private static org.apache.log4j.Logger logger;
    private String error="";
    public Env(EnvParams ep) throws Exception{
        super();
        logger= Logger.getLogger("driver");
        locator(ep);
        if(locator==null)
            throw new Exception(this.error);
        CompositeInstanceFilter filter=make_filter(ep);
        try{
            instances=locator.getCompositeInstances(filter);

            logger.info(" Number of instances after filtering: " + instances.size());
        }
        catch(Exception e){
            this.error="Error in getting composite instances";
            logger.error(this.error,e);
            e.printStackTrace();
        }
    }

    private void locator(EnvParams ep){
        try{
            BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
            //textEncryptor.setPassword(System.getenv("_WLS_PASS_KEY"));
            textEncryptor.setPassword("password");
            String pass=textEncryptor.decrypt(ep.pass);
            props=new Hashtable();
            props.put(javax.naming.Context.PROVIDER_URL,ep.host);
            props.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,"weblogic.jndi.WLInitialContextFactory");
            props.put(javax.naming.Context.SECURITY_PRINCIPAL,ep.user);
            props.put(javax.naming.Context.SECURITY_CREDENTIALS,pass);
            props.put("dedicated.connection","true");
            locator = LocatorFactory.createLocator(props);
        }
        catch(IllegalArgumentException e){
            this.error="Exception: Password cannot be empty see README";
            logger.error(this.error,e);
            e.printStackTrace();
        }
        catch(Exception e){
            this.error="Error in creating locator";
            logger.error(this.error,e);
            e.printStackTrace();
        }
    }


    private CompositeInstanceFilter make_filter(EnvParams ep){
        CompositeInstanceFilter filter= new CompositeInstanceFilter();
        if(!(ep.compositeName==null)){
            filter.setCompositeName(ep.compositeName);
        }
        if(!(ep.minAbsolute==null)){
            filter.setMinCreationDate(ep.minAbsolute);
            if(ep.minRelativeToNowInMinutes!=0){
                logger.warn("minRelativeToNowInDays being ignored since an absoulte min date was provided");
            }
        }
        else{
            if(ep.minRelativeToNowInMinutes!=0){
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE,ep.minRelativeToNowInMinutes);
                filter.setMinCreationDate(cal.getTime());
            }
        }
        if(!(ep.maxAbsolute==null)){
            filter.setMaxCreationDate(ep.maxAbsolute);
            if(ep.maxRelativeToNowInMinutes!=0){
                logger.warn("maxRelativeToNowInDays being ignored since an absoulte max date was provided");
            }
        }
        else{
            if(ep.maxRelativeToNowInMinutes!=0){
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE,ep.maxRelativeToNowInMinutes);
                filter.setMaxCreationDate(cal.getTime());
            }
        }
        if(!(ep.state==null)){
            filter.setState(Integer.parseInt(ep.state));
        }
        //filter.setState(ep.state);
        logger.info("Getting instances for " + ep.name);
        return filter;
    }
    public List<CompositeInstance> getFilteredInstances(){
        return this.instances;
    }
}
