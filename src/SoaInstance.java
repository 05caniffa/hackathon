/**
 * Created with IntelliJ IDEA.
 * User: ucanian
 * Date: 6/4/13
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.File;

import oracle.soa.management.facade.ComponentInstance;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import weblogic.xml.saaj.mime4j.field.datetime.DateTime;

public class SoaInstance {
    private String instance_id;
    private String txid;
    private String course_id;
    private String course_provider;
    private String state;
    private String time;
    private String BPEL;
    private List events=new ArrayList();
    private static org.apache.log4j.Logger logger;
    private List courseMgmtLabels= Arrays.asList("ReceiveCourse",
            "InvokeCatalogService",
            "InvokeLookupService",
            "InvokeSMS",
            "ReceiveSMS",
            "InvokeGB",
            "ReceiveGB",
            "InvokeCC",
            "Invoke_CCNG_New",
            "ReceiveCC",
            "InvokeGBTx",
            "Invoke_CCNGTx_New",
            "InvokeSMSTx",
            "InvokePortalNotification");
    public SoaInstance(String instance_id,String txid, String course_id, String course_provider, String state, String time){
        this.instance_id=instance_id;
        this.course_id=course_id;
        this.course_provider=course_provider;
        this.state=state;
        this.time=time;
        logger=Logger.getLogger("driver");
    }
    public SoaInstance(ComponentInstance instance){
        logger=Logger.getLogger("driver");
        try {
            String xml= String.valueOf(instance.getAuditTrail());
            this.instance_id=instance.getCompositeInstanceId();
            this.time=instance.getCreationDate().toString();
            this.BPEL=instance.getComponentName();
            this.state=instance.getNormalizedStateAsString();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize ();

            NodeList events = doc.getElementsByTagName("event");
            Date last=null;
            Date cur=null;
            long diff=0;
            for(int s=0; s<events.getLength() ; s++){
                Node item = events.item(s);
                Node labelnode=item.getAttributes().getNamedItem("label");
                Node statenode=item.getAttributes().getNamedItem("state");
                String eventn= item.getAttributes().getNamedItem("n").getTextContent();
                if(statenode!=null){
                    //this.state=statenode.getTextContent();
                }
                if(labelnode!=null){
                    String label = labelnode.getTextContent();
                    if(courseMgmtLabels.contains(label) || s==events.getLength()-1){
                        Node details=item.getLastChild();
                        if(last==null){
                            //2013-05-28T06:38:15.306-04:00
                            //need to remove the colon for < JDK7
                            String orig=item.getAttributes().getNamedItem("date").getTextContent();
                            String fixed=orig.substring(0,orig.length()-3)+"00";
                            last=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fixed);
                        }
                        String orig=item.getAttributes().getNamedItem("date").getTextContent();
                        String fixed=orig.substring(0,orig.length()-3)+"00";
                        cur=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fixed);
                        diff=cur.getTime()-last.getTime();
                        last=cur;
                        if(details.getNodeName().equals("details")){
                            addEvent(new AuditEvent(label,eventn,prettyFormat(details.getTextContent(),4),diff));
                        }
                        else{
                            addEvent(new AuditEvent(label,eventn,details.getTextContent(),diff));
                        }
                        if(label.equals("ReceiveCourse")){
                            Document tmp=docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            tmp.getDocumentElement().normalize();
                            this.txid=tmp.getElementsByTagName("transactionId").item(0).getTextContent();
                            this.course_id=tmp.getElementsByTagName("extCourseId").item(0).getTextContent();
                        }
                        if(label.equals("InvokeCatalogService")){
                            Document tmp=docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            tmp.getDocumentElement().normalize();
                            this.course_provider=tmp.getElementsByTagName("courseProviderRef").item(0).getLastChild().getTextContent();
                        } 
                        else if(label.equals("InvokeLookupService")){
                            Document tmp=docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            tmp.getDocumentElement().normalize();
                            if(tmp.getElementsByTagName("InvokeLookupService_getSystemsByProductIds_InputVariable").getLength()>0){
                                ;
                            }
                            else{
                                NodeList recips=tmp.getElementsByTagName("n3:recipients").item(0).getChildNodes();
                                for(int i=0;i<recips.getLength();i++){
                                    if(recips.item(i).getTextContent().equals("coursecompass")){
                                        this.course_provider=recips.item(i).getAttributes().getNamedItem("courseProviderId").getTextContent();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage());
        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace();
        }catch (Throwable t) {
            t.printStackTrace ();
        }
    }
    public String toString(){
        String ret="| "+this.instance_id+
                " | "+this.txid+
                " | "+this.BPEL+
                " | "+this.course_id+
                " | "+this.course_provider+
                " | "+this.state+
                " | "+this.time+" |\n";
        for(Object s:this.events){
            ret+=s+"\n";
        }
        return ret;
    }

    public String toHtml(){
        String ret=parentRow();
        for(Object s:this.events){
            ret+=((AuditEvent)s).toHtml();
        }
        return ret;
    }
    public String parentRow(){
        return "<tr class=\"parent\" data-level=\"0\">"+
                "<td>"+this.instance_id+"</td>"+
                "<td>"+this.txid+"</td>"+
                "<td>"+this.BPEL+"</td>"+
                "<td>"+this.course_id+"</td>"+
                "<td>"+this.course_provider+"</td>"+
                "<td>"+this.state+"</td>"+
                "<td>"+this.time+"</td>"+
                "</tr>";
    }
    private void addEvent(AuditEvent event){
        this.events.add(event);
    }

    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }
}
