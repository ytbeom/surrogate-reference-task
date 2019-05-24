package SuRT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;


public class SuRT extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private int numDistractor;
	private int distractorSize;
	private int targetSize;
	private int numRegion;
	private float circleLineWidth;
	private ArrayList<Region> regionArray;
	private long startTime;
	private long endTime;
	private int confirmedRegion;
	private boolean isStartedNow;
	private Image img;
	private Graphics img_g;
	private boolean positionSetCompleted;
	
	public SuRT(String filename) {
		super("Life Enhancing Technology Lab. - Surrogate Reference Task");
		try {
			File file = new File(filename);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String line = "";
			
			if((line = bufferedReader.readLine()) != null) {
				String array[] = line.split(",");
				numDistractor = Integer.parseInt(array[0]);
				distractorSize = Integer.parseInt(array[1]);
				targetSize = Integer.parseInt(array[2]);
				numRegion = Integer.parseInt(array[3]);
				circleLineWidth = Float.parseFloat(array[4]);
			}
			bufferedReader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.setUndecorated(false);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addKeyListener(new MyKeyListener());
		this.setFocusable(true);
		MakePositionSet();
	}
	
	public void paint(Graphics g) {
		img = createImage(super.getWidth(), super.getHeight());
		img_g = img.getGraphics();
		Graphics2D g2 = (Graphics2D)img_g;
		
		if (positionSetCompleted) {
			int regionWidth = super.getWidth()/numRegion;
			int regionHeight = super.getHeight();
			for (int i=0; i<numRegion; i++) {
				// Draw background
				if (i==confirmedRegion) {
					g2.setColor(new Color(160, 160, 160));
				}
				else {
					g2.setColor(Color.BLACK);
				}
				g2.fillRect(regionWidth*i, 0, regionWidth, regionHeight);
				// Draw distractor & target
				Region tempRegion = regionArray.get(i);
				g2.setStroke(new BasicStroke(circleLineWidth));
				g2.setColor(new Color(192, 192, 192));
				for (int j=0; j<tempRegion.getNumDistractor(); j++) {
					if(!tempRegion.getPosition(j).getIsTarget()) {
						g2.drawOval(tempRegion.getPosition(j).getX()-distractorSize, tempRegion.getPosition(j).getY()-distractorSize, distractorSize*2, distractorSize*2);
					}
					else {
						g2.drawOval(tempRegion.getPosition(j).getX()-targetSize, tempRegion.getPosition(j).getY()-targetSize, targetSize*2, targetSize*2);
					}
				}
			}
			if (isStartedNow) {
				startTime = System.currentTimeMillis();
				isStartedNow = false;
			}
		}
		g.drawImage(img, 0, 0, null);
	}
	
	class MyKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 37) {
				confirmedRegion = (confirmedRegion == 0)? 0 : confirmedRegion-1;
			}
			else if (e.getKeyCode() == 39) {
				confirmedRegion = (confirmedRegion == numRegion-1)? numRegion-1 : confirmedRegion+1;
			}
			else if (e.getKeyCode() == 38) {
				endTime = System.currentTimeMillis();
				SaveSuRTResult();
				MakePositionSet();
			}
			repaint();
		}
		
		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}
	
	public void MakePositionSet() {
		isStartedNow = true;
		confirmedRegion = 0;
		Random random = new Random();
		positionSetCompleted = false;
		
		regionArray = new ArrayList<Region>();
		int positionPerRegion = numDistractor/numRegion;
		int remainedNumPosition = numDistractor%numRegion;
		
		for (int i=0; i<numRegion; i++) {
			Region region = new Region(false, false, positionPerRegion);
			regionArray.add(region);
		}
		
		for (int i=0; i<remainedNumPosition; i++) {
			regionArray.get(random.nextInt(numRegion)).increaseNumDistractor();
		}
		
		for (int i=0; i<numRegion; i++) {
			boolean timeOver = false;
			boolean overlapped = true;
			long loopStartTime = System.currentTimeMillis();
			long loopEndTime = System.currentTimeMillis();
			int regionWidth = super.getWidth()/numRegion;
			int regionHeight = super.getHeight();
			for (int j=0; j<regionArray.get(i).getNumDistractor(); j++) {
				int tempX = 0;
				int tempY = 0;
				do {
					tempX = random.nextInt(regionWidth-2*targetSize)+regionWidth*i+targetSize;
					tempY = random.nextInt(regionHeight-2*targetSize)+targetSize;
					loopEndTime = System.currentTimeMillis();
					timeOver = ((loopEndTime - loopStartTime) > 1)? true : false;
					overlapped = IsOverlapped(tempX, tempY, i);
				} while (overlapped & !timeOver);
				if (!overlapped) {
					regionArray.get(i).setPosition(tempX, tempY);
					timeOver = false;
				}
				else {
					break;
				}
			}
			if (timeOver) {
				regionArray.get(i).resetPositionSet();
				i--;
				continue;
			}
		}
		
		int targetRegion = random.nextInt(numRegion);
		int targetIndex = random.nextInt(regionArray.get(targetRegion).getNumDistractor());
		regionArray.get(targetRegion).setIsTargetRegion(true);
		regionArray.get(targetRegion).getPositionSet().get(targetIndex).setIsTarget();
		positionSetCompleted = true;
		repaint();
	}
	
	public void SaveSuRTResult() {
		long responseTime = endTime - startTime;
		boolean success = regionArray.get(confirmedRegion).getIsTargetRegion();
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Date time = new Date();
		//String dateString = format.format(time);
		//String filename = "Result_"+dateString+".txt";
		String filename = "test.csv";
		try {
			File file = new File(filename);
			if(file.exists() == false) 
				file.createNewFile();
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
			bufferedWriter.write("Response Time: "+String.valueOf(responseTime*0.001));
			bufferedWriter.write(",");
			bufferedWriter.write(String.valueOf(success));
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			bufferedWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean IsOverlapped(int x, int y, int regionIndex) {
		boolean isOverlapped = false;
		
		isOverlapped = regionArray.get(regionIndex).isOverlapped(x, y, (double)(2*targetSize));
		if (!isOverlapped && regionIndex != 0)
			isOverlapped = regionArray.get(regionIndex-1).isOverlapped(x, y, (double)(2*targetSize));
		
		return isOverlapped;
	}
	
	public static void main (String[] args) {
		@SuppressWarnings("unused")
		SuRT surt = new SuRT("SuRTsetting.csv");
	}
}