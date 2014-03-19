package ist.meic.pa;

import java.lang.reflect.*;
public class Inspector {
	
	public Inspector(){}
	
	public void inspect(Object o){
		String className = o.getClass().getName();
		String fields ="";
		String methodsString = "";
		//get all fields and values of the fields
		for(Field f : o.getClass().getDeclaredFields()){
			if(!f.isAccessible())
				f.setAccessible(true);
			fields += f.getType().getName() + " " + f.getName() + "="+ "\n"; // falta o valor
		}
		
		for(Method m :  o.getClass().getDeclaredMethods()){
			if(!m.isAccessible())
				m.setAccessible(true);
			methodsString += m.getName() + "\n";
		}
	}

}
