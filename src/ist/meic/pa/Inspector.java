package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Inspector {

	private Class<?> objectClass;
	private ArrayList<Class<?>> objectHistory;
	private HashMap<String,Field> objectFields;
	private Scanner scanner;
	private String userInput;

	public Inspector(){}

	public void inspect(Object object) {

		scanner = new Scanner(System.in);
		objectHistory = new ArrayList<Class<?>>();
		
		objectClass = object.getClass();
		objectFields = this.getFields(object.getClass());

		while(true) {

			//Print class name
			System.err.println(object + " is an instance of " + objectClass.getName());

			//Print class fields	
			for(Field field : objectFields.values()) {	
				try {
					String fieldsOutput = field.getType() + " " + field.getName() + " = " + field.get(object);

					if(field.getModifiers() == 0) {
						System.err.println(fieldsOutput);
					} else {
						System.err.println(Modifier.toString(field.getModifiers()) + " " + fieldsOutput);
					}
				} catch (IllegalArgumentException e) {
					System.err.println(e.getMessage());
				} catch (IllegalAccessException e) {
					System.err.println(e.getMessage());
				}
			}

			System.out.print("> ");

			userInput = scanner.next();

			if(userInput.equals("q")) {
				return;

			} else if(userInput.equals("i")) {

				userInput = scanner.next();
				
				objectHistory.add(objectClass);
				objectClass = objectFields.get(userInput).getType();
				objectFields = this.getFields(objectClass);	
			}
		}
	}

	/**
	 * 
	 * @param clxss
	 * @return a hashmap of all the clxss fields (local and inherited)
	 */
	private HashMap<String,Field> getFields(Class<?> clxss) {

		HashMap<String,Field> fields = new HashMap<String,Field>();

		for(Class<?> c = clxss; c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(!f.isAccessible())
					f.setAccessible(true);
				fields.put(f.getName(),f);
			}
		}

		return fields;
	}
}
