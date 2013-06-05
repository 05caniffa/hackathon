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
    boolean show;
    public AuditEvent(String label,String position,String payload){
        this(label,position,payload,true);
    }
    public AuditEvent(String label,String position,String payload,boolean show){
        this.label=label;
        this.position=position;
        this.payload=payload;
        this.show=show;
    }
    public String toString(){
        String ret=this.position+": "+this.label;
        if(this.show){
            ret+="\n"+payload;
        }
        return ret;
    }
}

