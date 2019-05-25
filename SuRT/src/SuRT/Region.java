package SuRT;

import java.util.ArrayList;

public class Region {
	private boolean isTargetRegion;
	private int numDistractor;
	private ArrayList<Position> positionSet;
	
	public Region (boolean isHighlighted, boolean isTargetRegion, int numDistractor) {

		this.isTargetRegion = isTargetRegion;
		this.numDistractor = numDistractor;
		this.positionSet = new ArrayList<Position>();
	}
	
	public boolean getIsTargetRegion() {
		return isTargetRegion;
	}
	
	public void setIsTargetRegion(boolean isTargetRegion) {
		this.isTargetRegion = isTargetRegion;
	}
	
	public int getNumDistractor() {
		return numDistractor;
	}
	
	public void increaseNumDistractor() {
		numDistractor++;
	}
	
	public ArrayList<Position> getPositionSet() {
		return positionSet;
	}
	
	public Position getPosition(int index) {
		return positionSet.get(index);
	}
	
	public void setPosition(int x, int y, boolean isTarget) {
		positionSet.add(new Position(x, y, isTarget));
	}
	
	public void resetPositionSet() {
		for (int i=positionSet.size()-1; i>=0; i--) 
			positionSet.remove(i);
	}
	
	public boolean isOverlapped(int x, int y, int distractorRadius, int targetRadius, boolean targetSelected) {
		boolean isOverlapped = false;
		for(int i=0; i<positionSet.size(); i++) {
			Position position = positionSet.get(i);
			if (!targetSelected && !position.getIsTarget()) {
				if (Math.sqrt((position.getX()-x)*(position.getX()-x)+(position.getY()-y)*(position.getY()-y)) <= (double)(2*distractorRadius)) {
					isOverlapped = true;
					break;
				}
			}
			else {
				if (Math.sqrt((position.getX()-x)*(position.getX()-x)+(position.getY()-y)*(position.getY()-y)) <= (double)(distractorRadius+targetRadius)) {
					isOverlapped = true;
					break;
				}
			}
		}
		
		
		return isOverlapped;
	}
}