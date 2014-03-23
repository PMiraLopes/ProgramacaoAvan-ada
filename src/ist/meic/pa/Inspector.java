package ist.meic.pa;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Inspector {

	private Scanner scanner;
	private String userInput;

	private Object objectInstance;
	private ArrayList<Object> objectHistory;
	private HashMap<String,Field> objectFields;
	private HashMap<String,Method> objectMethods;
	
	public Inspector(){}

	public void inspect(Object object) {

		scanner = new Scanner(System.in);
		objectHistory = new ArrayList<Object>();

		objectInstance = this.createInstance(object.getClass());
		objectFields = this.getFields(objectInstance.getClass());

		while(true) {

			this.printObjectFeatures(objectInstance, objectFields);

			/*** User interaction ***/
			System.err.print("\n> ");

			userInput = scanner.next();

			if(userInput.equals("q")) {
				return;

			} else if(userInput.equals("i")) {

				userInput = scanner.next();

				objectHistory.add(objectInstance);
				
				objectInstance = this.createInstance(objectFields.get(userInput).getType());
				objectFields = this.getFields(objectInstance.getClass());
				
			} else if(userInput.equals("m")) {
				
			} else if(userInput.equals("c")) {
				
			}
		}
	}

	/**
	 * Create an instance of class
	 * 
	 * @param clazz The class object
	 * @return An instance of class
	 */
	private Object createInstance(Class<?> clazz) {

		Object objectInstance = null;

		try {
			objectInstance = clazz.newInstance();
		} catch (InstantiationException e) {
			System.err.println(e.getMessage() + ": The specified class object cannot be instantiated");
		} catch (IllegalAccessException e) {
			System.err.println(e.getMessage() + ": No access to definition");
		}
		return objectInstance;
	}
	
	/**
	 * Get all the class fields and make them accessible
	 * 
	 * @param clazz The class object
	 * @return All class fields
	 */
	private HashMap<String,Field> getFields(Class<?> clazz) {

		HashMap<String,Field> fields = new HashMap<String,Field>();

		for(Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			for(Field f : c.getDeclaredFields()){
				if(!f.isAccessible())
					f.setAccessible(true);
				fields.put(f.getName(),f);
			}
		}
		return fields;
	}

	/**
	 * Prints object features, name, fields, methods
	 * 
	 * @param objectInstance The object instance
	 * @param hashMap The field hashmap
	 */
	private void printObjectFeatures(Object objectInstance, HashMap<String,Field> hashMap) {
		
		//Print object name and class instance
		if(objectInstance.toString().equals("")) {
			System.err.println("Instance of " + objectInstance.getClass());
		} else {
			System.err.println(objectInstance + " is an instance of " + objectInstance.getClass());
		}	
			
		System.err.println("-------------");
		
		//Print object fields
		for(Field field : hashMap.values()) {	
			try {
				String fieldsOutput = field.getType() + " " + field.getName() + " = " + field.get(objectInstance);
				
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
	}
	
	/**
	 * Modifies the value of the field named fieldName so that it becomes value
	 * 
	 * @param objectInstance The object instance
	 * @param fieldName The object field name to be re-valued
	 * @param value The new object field value
	 */
	private void fieldTypeModifier(Object objectInstance, String fieldName, String value) {
		
	}
}
