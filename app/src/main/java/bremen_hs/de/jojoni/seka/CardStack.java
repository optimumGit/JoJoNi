package bremen_hs.de.jojoni.seka;

/**
 * @author  Johann Luziv
 * @since   20150513
 * @version 0.1
 */


import java.util.ArrayList;
import java.util.List;

public class CardStack {
	
	private List<Cards> stack = null;
	
	public CardStack(){
		stack = new ArrayList<Cards>();
		this.newStack();
	}
	/**
	 * Get a List with Cards
	 * 
	 * @return ArrayList<Cards>
	 */
	public List<Cards> getStack(){
		return this.stack;
	}
 	
	/**
	 * Creat new stack
	 *  
	 */
	private void newStack(){
		 for(int x = 0; x <= 3; x++){
			 for(int y = 0; y <= 8; y++){
				 this.stack.add(new Cards(x,y));
			 }
		 }
	}
	
}
