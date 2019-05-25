package SuRT;

public class Position {
	private int x;
	private int y;
	private boolean isTarget;
	
	public Position(int x, int y, boolean isTarget) {
		this.x = x;
		this.y = y;
		this.isTarget = isTarget;
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
}
