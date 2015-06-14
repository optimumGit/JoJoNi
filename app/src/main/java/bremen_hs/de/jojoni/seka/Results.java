package seka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author  Johann Luziv
 * @since   20150608
 * @version 0.1
 */

public class Results {

	public Results() {
		// TODO Auto-generated constructor stub
	}
	
	public float results(List<Cards> hand){
		ArrayList<Float> results = new ArrayList<Float>();
		results.add(this.highestCard(hand));
		results.add(this.pair(hand));
		results.add(this.allSameType(hand));
		results.add(this.allSameCount(hand));
		results.add(this.twoAs(hand));
		
		Collections.sort(results);
		//return high score
		return results.get(results.size() - 1);
	}
	
	/**
	 * get result of highest card
	 * 
	 * @param List<Cards> hand
	 * @return float result
	 */
	private float highestCard(List<Cards> hand){
		float result = 0.0f;
		List<Integer> list = new ArrayList<Integer>();
		
		for(int i = 0; i < hand.size(); i++){
			list.add(hand.get(i).getCardCount());
		}

		Collections.sort(list);
		
		if(list.get(2) >= 0 && list.get(2) <= 3){
			result += list.get(2) + 6;
		}else if(list.get(2) >= 4 && list.get(2) <= 7){
			result += 10;
		}else if(list.get(2) == 8){
			result += 11;	
		}
		
		return result;
	}

	/**
	 * 
	 * 
	 * @param List<Cards> hand
	 * @return float result
	 */
	private float pair(List<Cards> hand){
		float result = 0.0f;
		if(hand.get(0).getCardTyp() == hand.get(1).getCardTyp()){
			if(hand.get(0).getCardCount() >= 0 && hand.get(0).getCardCount() <= 3){
				result += hand.get(0).getCardCount() + 6;
			}else if(hand.get(0).getCardCount() >= 4 && hand.get(0).getCardCount() <= 7){
				result += 10;
			}else if(hand.get(0).getCardCount() == 8){
				result += 11;	
			}
			if(hand.get(1).getCardCount() >= 0 && hand.get(1).getCardCount() <= 3){
				result += hand.get(1).getCardCount() + 6;
			}else if(hand.get(1).getCardCount() >= 4 && hand.get(1).getCardCount() <= 7){
				result += 10;
			}else if(hand.get(1).getCardCount() == 8){
				result += 11;		
			}
		}
		else if(hand.get(0).getCardTyp() == hand.get(2).getCardTyp()){
			if(hand.get(0).getCardCount() >= 0 && hand.get(0).getCardCount() <= 3){
				result += hand.get(0).getCardCount() + 6;
			}else if(hand.get(0).getCardCount() >= 4 && hand.get(0).getCardCount() <= 7){
				result += 10;
			}else if(hand.get(0).getCardCount() == 8){
				result += 11;	
			}if(hand.get(2).getCardCount() >= 0 && hand.get(2).getCardCount() <= 3){
				result += hand.get(2).getCardCount() + 6;
			}else if(hand.get(2).getCardCount() >= 4 && hand.get(2).getCardCount() <= 7){
				result += 10;
			}else if(hand.get(2).getCardCount() == 8){
				result += 11;		
			}
		}
		else if(hand.get(1).getCardTyp() == hand.get(2).getCardTyp()){
			if(hand.get(1).getCardCount() >= 0 && hand.get(1).getCardCount() <= 3){
				result += hand.get(1).getCardCount() + 6;
			}else if(hand.get(1).getCardCount() >= 4 && hand.get(1).getCardCount() <= 7){
				result += 10;
			}else if(hand.get(1).getCardCount() == 8){
				result += 11;	
			}	
			if(hand.get(2).getCardCount() >= 0 && hand.get(2).getCardCount() <= 3){
				result += hand.get(2).getCardCount() + 6;
			}else if(hand.get(2).getCardCount() >= 4 && hand.get(2).getCardCount() <= 7){
				result += 10;
			}else if(hand.get(2).getCardCount() == 8){
				result += 11;		
			}
		}
		return result;
	}
	/**
	 * 
	 * 
	 * @return float result
	 */
	private float allSameType(List<Cards> hand){
		float result = 0.0f;
		
		if(hand.get(0).getCardTyp() == hand.get(1).getCardTyp() && hand.get(0).getCardTyp() == hand.get(2).getCardTyp() 
				&& hand.get(1).getCardTyp() == hand.get(2).getCardTyp()){
			
			for(int i = 0; i < hand.size(); i++){
				if(hand.get(i).getCardCount() >= 0 && hand.get(i).getCardCount() <= 3){
					result += hand.get(i).getCardCount() + 6;
				}
				if(hand.get(i).getCardCount() >= 4 && hand.get(i).getCardCount() <= 7){
					result += 10;
				}
				if(hand.get(i).getCardCount() == 8){
					result += 11;
				}
			}
			return result;
		}else{
			return result;
		}
	}
	/**
	 *
	 * 
	 * @return float
	 */
	private float allSameCount(List<Cards> hand){
		float as;
		float six;
		if(hand.get(0).getCardCount() == hand.get(1).getCardCount() && hand.get(1).getCardCount() == hand.get(2).getCardCount()){
			as  = threeAs(hand);
			six = threeSix(hand);
			if(as == 33){
				return 33.0f;
			}else if(six == 33.5){
				return 33.5f;
			}
			return 30.5f;
		}else{
			return 0.0f;
		}
	}
	/**
	 * 
	 * 
	 * @return float
	 */
	private float twoAs(List<Cards> hand){
		if(hand.get(0).getCardCount() == 8 && hand.get(1).getCardCount() == 8){
			return 22.0f;
		}
		if(hand.get(0).getCardCount() == 8 && hand.get(2).getCardCount() == 8){
			return 22.0f;
		}
		if(hand.get(1).getCardCount() == 8 && hand.get(2).getCardCount() == 8){
			return 22.0f;
		}
		return 0.0f;
	}

	/**
	 * Check special type of "allSameCount", if all three cards are As return a other result.
	 * Three As are 33 points.
	 * 
	 * @return float
	 */
	private float threeAs(List<Cards> hand){
		if(hand.get(0).getCardCount() == 8 && hand.get(1).getCardCount() == 8 && hand.get(2).getCardCount() == 8){
			return 33.0f;
		}else{
			return 0.0f;
		}
	}
	/**
	 * Check special type of "allSameCount", if all three cards are Six return a other result.
	 * 
	 * Three As are 33,5 points.
	 * 
	 * @return float
	 */
	private float threeSix(List<Cards> hand){
		if(hand.get(0).getCardCount() == 0 && hand.get(1).getCardCount() == 0 && hand.get(2).getCardCount() == 0){
			return 33.5f;
		}else{
			return 0.0f;
		}
	}


}
