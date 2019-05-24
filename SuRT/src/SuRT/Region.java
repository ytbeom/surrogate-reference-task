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
	
	public void setPosition(int x, int y) {
		positionSet.add(new Position(x, y));
	}
	
	public void resetPositionSet() {
		for (int i=positionSet.size()-1; i>=0; i--) 
			positionSet.remove(i);
	}
	
	public boolean isOverlapped(int x, int y, double threshold) {
		boolean isOverlapped = false;
		
		for(int i=0; i<positionSet.size(); i++) {
			if (Math.sqrt((positionSet.get(i).getX()-x)*(positionSet.get(i).getX()-x)+(positionSet.get(i).getY()-y)*(positionSet.get(i).getY()-y)) < threshold) {
				isOverlapped = true;
				break;
			}
		}
		return isOverlapped;
	}
}