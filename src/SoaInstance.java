/**
 * Created with IntelliJ IDEA.
 * User: ucanian
 * Date: 6/4/13
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import oracle.soa.management.facade.ComponentInstance;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SoaInstance {
    private String instance_id;
    private String txid;
    private String course_id;
    private String course_provider;
    private String state;
    private String time;
    private String BPEL;
    private String duration;
    private List events=new ArrayList();
    private static org.apache.log4j.Logger logger;
    public SoaInstance(ComponentInstance instance,boolean show){
        logger=Logger.getLogger("driver");
        List courseMgmtLabels= Arrays.asList("ReceiveCourse",
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
        List regEnrollLabels= Arrays.asList("ReceiveUCE",
                "InvokeSMSLookupService",
                "InvokeSMS",
                "ReceiveSMS",
                "InvokeGB",
                "ReceiveGB",
                "InvokeCC",
                "Invoke_CCBPMS",
                "ReceiveCC",
                "Invoke_XLCCNG",
                "Receive_XLCCNG",
                "InvokeGBTx",
                "InvokeCCNGTx",
                "InvokeXLCCNGTx",
                "InvokeSMSTx",
                "InvokePortal");
        try {
            String xml= String.valueOf(instance.getAuditTrail());
            this.instance_id=instance.getCompositeInstanceId();
            this.time=instance.getCreationDate().toString();
            this.BPEL=instance.getComponentName();
            List bpellabels=new ArrayList();
            if (this.BPEL.equals("CourseMgmt")){
                bpellabels=courseMgmtLabels;
            }
            else if(this.BPEL.equals("RegEnroll")){
                bpellabels=regEnrollLabels;
            }
            this.state=instance.getNormalizedStateAsString();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize ();

            NodeList events = doc.getElementsByTagName("event");
            Date last=null;
            Date cur=null;
            Date start=null;
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
                    if(bpellabels.contains(label) || s==events.getLength()-1){
                        Node details=item.getLastChild();
                        if(last==null){
                            //2013-05-28T06:38:15.306-04:00
                            //need to remove the colon for < JDK7
                            String orig=item.getAttributes().getNamedItem("date").getTextContent();
                            String fixed=orig.substring(0,orig.length()-3)+"00";
                            last=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fixed);
                            start=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fixed);
                        }
                        String orig=item.getAttributes().getNamedItem("date").getTextContent();
                        String fixed=orig.substring(0,orig.length()-3)+"00";
                        cur=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(fixed);
                        diff=cur.getTime()-last.getTime();
                        last=cur;
                        this.duration=String.valueOf(cur.getTime()-start.getTime());
                        if(details.getNodeName().equals("details")){
                            Document tmp = docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            addEvent(new AuditEvent(label, eventn, prettyPrintWithXMLSerializer(tmp), diff,show));
                        }
                        else{
                            addEvent(new AuditEvent(label,eventn,details.getTextContent(),diff,show));
                        }
                        if(label.equals("ReceiveCourse")){
                            Document tmp=docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            tmp.getDocumentElement().normalize();
                            this.txid=tmp.getElementsByTagName("transactionId").item(0).getTextContent();
                            this.course_id=tmp.getElementsByTagName("extCourseId").item(0).getTextContent();
                        }
                        else if(label.equals("InvokeCatalogService")){
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
                                NodeList recipnode = tmp.getElementsByTagName("n3:recipients");
                                if(recipnode.item(0)!=null){
                                    NodeList recips=recipnode.item(0).getChildNodes();
                                    for(int i=0;i<recips.getLength();i++){
                                        if(recips.item(i).getTextContent().equals("coursecompass")){
                                            Node courseProviderId = recips.item(i).getAttributes().getNamedItem("courseProviderId");
                                            if(courseProviderId!=null){
                                                this.course_provider=courseProviderId.getTextContent();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if(label.equals("ReceiveUCE")){
                            Document tmp=docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            tmp.getDocumentElement().normalize();
                            if(tmp.getElementsByTagName("enroll:transactionId").item(0)!=null){
                                this.txid=tmp.getElementsByTagName("enroll:transactionId").item(0).getTextContent();
                            }
                            else if(tmp.getElementsByTagName("user:transactionId").item(0)!=null){
                                this.txid=tmp.getElementsByTagName("user:transactionId").item(0).getTextContent();
                            }
                            if(tmp.getElementsByTagName("enroll:extCourseId").item(0)!=null){
                                this.course_id=tmp.getElementsByTagName("enroll:extCourseId").item(0).getTextContent();
                            }
                            NodeList recipnode = tmp.getElementsByTagName("sys:recipients");
                            if(recipnode.item(0)!=null){
                                NodeList recips=recipnode.item(0).getChildNodes();
                                for(int i=0;i<recips.getLength();i++){
                                    if(recips.item(i).getTextContent().equals("coursecompass")){
                                        Node courseProviderId = recips.item(i).getAttributes().getNamedItem("courseProviderId");
                                        if(courseProviderId!=null){
                                            this.course_provider=courseProviderId.getTextContent();
                                        }
                                    }
                                }
                            }
                        }
                        else if (label.equals("InvokeSMSLookupService")){
                            Document tmp=docBuilder.parse(new InputSource(new StringReader(details.getTextContent())));
                            tmp.getDocumentElement().normalize();
                            NodeList recipnode = tmp.getElementsByTagName("n3:recipients");
                            if(recipnode.item(0)!=null){
                                NodeList recips=recipnode.item(0).getChildNodes();
                                for(int i=0;i<recips.getLength();i++){
                                    if(recips.item(i).getTextContent().equals("coursecompass")){
                                        Node courseProviderId = recips.item(i).getAttributes().getNamedItem("courseProviderId");
                                        if(courseProviderId!=null){
                                            this.course_provider=courseProviderId.getTextContent();
                                        }
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
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        catch (Throwable t) {
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
                "<td>"+this.duration+"</td>"+
                "</tr>";
    }
    private void addEvent(AuditEvent event){
        this.events.add(event);
    }

    static String prettyPrintWithXMLSerializer(Document document) {
        try{
            StringWriter stringWriter = new StringWriter();
            OutputFormat format = new OutputFormat(Method.XML, "UTF-8", true);
            format.setIndent(2);
            XMLSerializer serializer = new XMLSerializer(stringWriter,format);
            serializer.serialize(document);
            return stringWriter.toString();
        }
        catch(Exception e){
            return "error";
        }
    }
}
