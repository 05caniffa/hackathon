import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: ucanian
 * Date: 6/4/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditEvent {
    String label;
    String position;
    String payload;
    long timediff;
    boolean show;
    public AuditEvent(String label,String position,String payload,long timediff){
        this(label,position,payload,timediff,false);
    }
    public AuditEvent(String label,String position,String payload,long timediff,boolean show){
        this.label=label;
        this.position=position;
        this.payload=payload;
        this.show=show;
        this.timediff=timediff;
    }
    public String toString(){
        String ret=this.position+": "+this.label+" | "+this.timediff;
        if(this.show){
            ret+="\n"+payload;
        }
        return ret;
    }
    public String parentRow(){
        return "<tr class=\"parent nosort\" data-level=\"1\">"+
                "<td/><td colspan=\"6\">"+this.label+"</td>"+
                "<td>"+this.timediff+"</td>"+
                "</tr>";
    }
    public String childRow(){
        return "<tr class=\"child nosort\" data-level=\"2\">"+
                "<td/><td colspan=\"7\"><pre>"+ StringEscapeUtils.escapeXml(this.payload)+"</pre></td>"+
                "</tr>";
    }
    public String toHtml(){
        if(this.show){
            return parentRow()+childRow();
        }
        else{
            return parentRow();
        }
    }
}

