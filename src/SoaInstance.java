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
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
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

public class SoaInstance {
    private String instance_id;
    private String txid;
    private String course_id;
    private String course_provider;
    private String state;
    private String time;
    private List events=new ArrayList();
    private List courseMgmtLabels= Arrays.asList("ReceiveCourse","InvokeCatalogService","InvokeLookupService","InvokeCC","ReceiveCC");
    public SoaInstance(String instance_id,String txid, String course_id, String course_provider, String state, String time){
        this.instance_id=instance_id;
        this.course_id=course_id;
        this.course_provider=course_provider;
        this.state=state;
        this.time=time;
    }
    private void addEvent(AuditEvent event){
        this.events.add(event);
    }
    public SoaInstance(String xml){
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize ();

            NodeList events = doc.getElementsByTagName("event");

            for(int s=0; s<events.getLength() ; s++){
                Node item = events.item(s);
                Node labelnode=item.getAttributes().getNamedItem("label");
                Node statenode=item.getAttributes().getNamedItem("state");
                String eventn= item.getAttributes().getNamedItem("n").getTextContent();
                if(statenode!=null){
                    this.state=statenode.getTextContent();
                }
                if(labelnode!=null){
                    String label = item.getAttributes().getNamedItem("label").getTextContent();
                    if(courseMgmtLabels.contains(label)){
                        Node details=item.getLastChild();
                        if(details.getNodeName().equals("details")){
                            addEvent(new AuditEvent(label,eventn,prettyFormat(details.getTextContent(),4)));
                        }
                        else{
                            addEvent(new AuditEvent(label,eventn,details.getTextContent()));
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
                " | "+this.course_id+
                " | "+this.course_provider+
                " | "+this.state+
                " | "+this.time+" |\n";
        for(Object s:this.events){
            ret+=s+"\n";
        }
        return ret;
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
    public static void main(String args[]){
        SoaInstance s=new SoaInstance("audit.xml");
        System.out.println(s);
    }
}
