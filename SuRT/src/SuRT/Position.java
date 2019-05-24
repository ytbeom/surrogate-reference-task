package SuRT;

public class Position {
	private int x;
	private int y;
	private boolean isTarget;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
		isTarget = false;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean getIsTarget() {
		return isTarget;
	}
	
	public void setIsTarget() {
		isTarget = true;
	}
}
