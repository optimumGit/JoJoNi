package bremen_hs.de.jojoni.seka;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * @author  Johann Luziv
 * @version 0.1
 * @since   20150513
 */
public class Main {
	//private int cardType;  /*0=karo; 1=herz; 2=kreuz; 3=pik;*/
	//private int cardCount; /*0=6; 1=7; 2=8; 3=9; 4=10; 5=bube; 6=dame; 7=dame; 8=As*/
	
	public static void main(String[] args) {
		Cards one   = new Cards(1, 7);
		Cards two   = new Cards(2, 1);
		Cards three = new Cards(0, 1);
				
		List<Cards> hand = new ArrayList<Cards>();
		hand.add(one);
		hand.add(two);
		hand.add(three);
		
		
		GameManager gm = new GameManager();
		float result = gm.testResult(hand, 4);
		System.out.println(result);
		
/*		int i = 0;
		while(i < player.size()){
			System.out.println(player.get(i).getName());
			for(int y = 0; y < player.get(i).getHand().size(); y++){
				System.out.println(player.get(i).getHand().get(y));
			}
			i++;
			System.out.println("-----------------");
		}
*/  	
		
		
	/*
		GameManager game = new GameManager();


		
		System.out.println("Zwei Spieler");
		Scanner sc = new Scanner(System.in);
    	System.out.print("Geben sie einen Name ein: ");
    	String name = sc.next();
       	game.creatNewPlayer(name);
   
       	System.out.print("Geben sie einen Name ein: ");
    	name = sc.next();
    	game.creatNewPlayer(name);
    
		System.out.println("neues spiel x druecken: ");
		String state = "";
		state = sc.next();
    	boolean gm;
    	if(state.equals("x")){
    		gm = true;
    	}else{
    		gm = false;
    	}
    	List<Player> plList = game.getPlayerList();
    	/*int i = 0;
		while(i < plList.size()){
			System.out.println(plList.get(i).getName());
			for(int y = 0; y < plList.get(i).getHand().size(); y++){
				System.out.println(plList.get(i).getHand().get(y));
			}
			i++;
			System.out.println("-----------------");
		}
    	while(gm){
    		game.dealCards(game.getPlayerList());
    		int i = 0;
    		while(i < plList.size()){
    			System.out.println(plList.get(i).getName());
    			for(int y = 0; y < plList.get(i).getHand().size(); y++){
    				System.out.println(plList.get(i).getHand().get(y));
    			}
				System.out.println("result: " + plList.get(i).getCardsResult(plList.get(i).getHand()));

    			i++;
    			
    		}
    		
    	}
    	sc.close();
	*/
	}
}
