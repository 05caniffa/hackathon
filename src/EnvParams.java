import java.util.Date;

public class EnvParams{
    public String name;
    public String host;
    public Date minAbsolute;
    public Date maxAbsolute;
    public String state;
    public String compositeName;
    public String user;
    public String pass;
    //relative dates will be ignored if an 
    //absolute date is also supplied
    public int maxRelativeToNowInDays;
    public int minRelativeToNowInDays;
    public EnvParams(){
	//nothing
    }
    public String toString(){
	String out="";
	out+="name = "+name+"\n";
	out+="host = "+host+"\n";
	out+="minAbsolute = ";
	if(minAbsolute==null){
	    
	}
	else{
	    out+=minAbsolute;
	}
	out+="\n";
	out+="maxAbsolute = ";
	if(maxAbsolute==null){

	}
	else{
	    out+=maxAbsolute;
	}
	out+="\n";
	out+="minRelativeToNowInDays = "+String.valueOf(minRelativeToNowInDays)+"\n";
	out+="maxRelativeToNowInDays = "+String.valueOf(maxRelativeToNowInDays)+"\n";
	out+="state = "+state+"\n";
	out+="compositeName = "+compositeName+"\n";
	out+="user = "+user+"\n";
	out+="pass = "+pass+"\n";
	return out;
    }
}
