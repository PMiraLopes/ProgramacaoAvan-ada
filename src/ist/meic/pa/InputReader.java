package ist.meic.pa;

import java.util.Scanner;

public class InputReader {
	
	public static void main(String[] args){
		Scanner reader = new Scanner(System.in);
		Inspector inspector = new Inspector();
		String input;
		
		while(true){
			input = reader.next();
			
			if(input.equals("q"))
				System.exit(0);
			try{
				inspector.inspect(input);
			}
			catch(ClassNotFoundException e){
				System.out.println(e.getMessage());
			}
		}
		
	}
}
