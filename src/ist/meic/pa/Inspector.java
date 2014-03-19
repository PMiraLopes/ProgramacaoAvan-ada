package ist.meic.pa;
import java.lang.reflect.*;

public class Inspector {
	
	
	public Inspector(){}
	
	public void inspect(Object object) throws ClassNotFoundException{
		System.out.println(Class.forName(object.toString()));
	}
	
}
