package SuRT;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class SuRT extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private int numDistractor;
	private int distractorSize;
	private int targetSize;
	private int numRegion;
	private float circleLineWidth;
	
	private ArrayList<Region> regionArray;
	private int confirmedRegion;
	private boolean isStartedNow;
	private boolean positionSetCompleted;
	
	private Image img;
	private Graphics img_g;
	
	private String participantName;
	private int numTask;
	private int countTask;	
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private long startTime;
	private long endTime;
	private BufferedWriter bufferedWriter;
	
	public SuRT(String inputFileName) {
		super("Life Enhancing Technology Lab. - Surrogate Reference Task");
		
		this.setUndecorated(true);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addKeyListener(new MainKeyListener());
		this.addMouseListener(new MainMouseListener());
		this.setFocusable(true);
		
		SettingDialog dialog = new SettingDialog(this);
		dialog.setVisible(true);
		countTask = 0;

		try {
			File inputFile = new File(inputFileName);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
			
			String outputFileName = participantName+".csv";
			File outputFile = new File(outputFileName);
			if(outputFile.exists() == false) 
				outputFile.createNewFile();
			bufferedWriter = new BufferedWriter(new FileWriter(outputFile, true));
			
			// Column Header 저장
			String line = bufferedReader.readLine();
			String columnHeaderArray[] = line.split(",");
			bufferedWriter.write("Experiment start time" + ",");
			for (int i=0; i<columnHeaderArray.length; i++)
				bufferedWriter.write(columnHeaderArray[i] + ",");
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			// Parameter Value 저장
			line = bufferedReader.readLine();
			String array[] = line.split(",");
			bufferedWriter.write(format.format(System.currentTimeMillis()) + ",");
			for (int i=0; i<array.length; i++)
				bufferedWriter.write(array[i] + ",");
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			// Parameter 세팅
			numDistractor = Integer.parseInt(array[0]);
			distractorSize = Integer.parseInt(array[1]);
			targetSize = Integer.parseInt(array[2]);
			numRegion = Integer.parseInt(array[3]);
			circleLineWidth = Float.parseFloat(array[4]);
			
			// 결과 column Header 저장
			bufferedWriter.newLine();
			bufferedWriter.write("experiment set start time" + ",");
			bufferedWriter.write("response time" + ",");
			bufferedWriter.write("success" + ",");
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			bufferedReader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		MakePositionSet();
	}
	
	class MainKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == 37) {
				if (confirmedRegion != -1)
					confirmedRegion = (confirmedRegion == 0)? 0 : confirmedRegion-1;
				else
					confirmedRegion = (numRegion-1)/2;
				repaint();
			}
			else if (e.getKeyCode() == 39) {
				if (confirmedRegion != -1)
					confirmedRegion = (confirmedRegion == numRegion-1)? numRegion-1 : confirmedRegion+1;
				else
					confirmedRegion = numRegion/2;
				repaint();
			}
			else if (e.getKeyCode() == 38) {
				if (confirmedRegion != -1) {
					endTime = System.currentTimeMillis();
					SaveSuRTResult();
					MakePositionSet();
				}
			}
			else if (e.getKeyCode() == 27 || e.getKeyCode() == 8) {
				try {
					bufferedWriter.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.exit(0);
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}
	
	class MainMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			for (int i=0; i<numRegion; i++) {
				if (e.getX() >= regionArray.get(i).getLeftX() && e.getX() <= regionArray.get(i).getRightX()) {
					confirmedRegion = i;
					break;
				}
			}
			endTime = System.currentTimeMillis();
			SaveSuRTResult();
			MakePositionSet();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class SettingDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		
		private int width = 500;
		private int height = 150;
		private JLabel participantNameLabel = new JLabel("Participant Name: ", JLabel.CENTER);
		private JTextField participantNameTextField = new JTextField(10);
		private JLabel numTaskLabel = new JLabel("# of Trial: ", JLabel.CENTER);
		private JTextField numTaskTextField = new JTextField(10);
		private JButton okButton = new JButton("OK");
		private URL lineImageURL = SettingDialog.class.getClassLoader().getResource("Line.png");
		private ImageIcon lineImageIcon = new ImageIcon(lineImageURL);
		private Image lineImage = lineImageIcon.getImage().getScaledInstance(width-40, 15, java.awt.Image.SCALE_SMOOTH);
		private JLabel lineImageBox = new JLabel(new ImageIcon(lineImage));
		private URL logoImageURL = SettingDialog.class.getClassLoader().getResource("Logo.png");
		private ImageIcon logoImageIcon = new ImageIcon(logoImageURL);
		private Image logoImage = logoImageIcon.getImage().getScaledInstance(width/2, width/2*logoImageIcon.getIconHeight()/logoImageIcon.getIconWidth(), java.awt.Image.SCALE_SMOOTH);
		private JLabel logoImageBox = new JLabel(new ImageIcon(logoImage));
		
		public SettingDialog(JFrame frame) {
			super(frame, "SuRT Setting Dialog", true);
			setLayout(new FlowLayout());
			setSize(width, height);
			setLocation((SuRT.super.getWidth()-width)/2, (SuRT.super.getHeight()-height)/2);
			this.setFocusable(true);
			
			add(participantNameLabel, BorderLayout.CENTER);
			add(participantNameTextField);
			add(numTaskLabel, BorderLayout.CENTER);
			add(numTaskTextField);
			add(okButton);
			add(lineImageBox);
			add(logoImageBox);
			
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SaveDialogResult();
				}
			});
			
			KeyListener enterListener = new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == 10)
						SaveDialogResult();
				}
				
				@Override
				public void keyTyped(KeyEvent e) {}

				@Override
				public void keyReleased(KeyEvent e) {}
			};
			
			this.addKeyListener(enterListener);
			participantNameTextField.addKeyListener(enterListener);
			numTaskTextField.addKeyListener(enterListener);
		}
		
		public void SaveDialogResult() {
			participantName = participantNameTextField.getText();
			if (participantName.equals(""))
				participantName = "NONAME";
			
			String numTaskString = numTaskTextField.getText();
			if (numTaskString.equals(""))
				numTask = -1;
			else
				numTask = Integer.parseInt(numTaskString);
			
			this.setVisible(false);
		}
	}
	
	public void paint(Graphics g) {
		img = createImage(super.getWidth(), super.getHeight());
		img_g = img.getGraphics();
		Graphics2D g2 = (Graphics2D)img_g;
		
		if (positionSetCompleted) {
			int regionWidth = super.getWidth()/numRegion;
			int regionHeight = super.getHeight();
			for (int i=0; i<numRegion; i++) {
				if (i==confirmedRegion) {
					g2.setColor(new Color(160, 160, 160));
				}
				else {
					g2.setColor(Color.WHITE);
				}
				g2.fillRect(regionArray.get(i).getLeftX(), 0, regionArray.get(i).getRightX(), regionHeight);

				Region region = regionArray.get(i);
				g2.setStroke(new BasicStroke(circleLineWidth));
				g2.setColor(Color.BLACK);
				for (int j=0; j<region.getNumDistractor(); j++) {
					if(!region.getPosition(j).getIsTarget()) {
						g2.drawOval(region.getPosition(j).getX()-distractorSize, region.getPosition(j).getY()-distractorSize, distractorSize*2, distractorSize*2);
					}
					else {
						g2.drawOval(region.getPosition(j).getX()-targetSize, region.getPosition(j).getY()-targetSize, targetSize*2, targetSize*2);
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
	
	public void MakePositionSet() {
		isStartedNow = true;
		confirmedRegion = -1;
		positionSetCompleted = false;
		
		regionArray = new ArrayList<Region>();
		int positionPerRegion = numDistractor/numRegion;
		int remainedNumPosition = numDistractor%numRegion;
		
		Random random = new Random();
		for (int i=0; i<numRegion; i++) {
			int leftX = i == 0 ? 0 : regionArray.get(i-1).getRightX() + 1;
			int rightX = super.getWidth()*(i+1)/numRegion;
			Region region = new Region(false, false, positionPerRegion, leftX, rightX);
			regionArray.add(region);				
		}
		
		for (int i=0; i<remainedNumPosition; i++) {
			regionArray.get(random.nextInt(numRegion)).increaseNumDistractor();
		}
		
		int targetRegion = random.nextInt(numRegion);
		int targetIndex = random.nextInt(regionArray.get(targetRegion).getNumDistractor());
		regionArray.get(targetRegion).setIsTargetRegion(true);
		
		int targetRadius = targetSize + (int)(circleLineWidth/2);
		int distractorRadius = distractorSize + (int)(circleLineWidth/2);
		
		for (int i=0; i<numRegion; i++) {
			boolean timeOver = false;
			boolean overlapped = true;
			boolean targetSelected = false;
			long loopStartTime = System.currentTimeMillis();
			long loopEndTime = System.currentTimeMillis();
			Region region = regionArray.get(i);
			int regionWidth = super.getWidth()/numRegion;
			int regionHeight = super.getHeight();
			for (int j=0; j<region.getNumDistractor(); j++) {
				targetSelected = ((i == targetRegion) && (j == targetIndex))? true : false;
				int tempX = 0;
				int tempY = 0;
				do {
					if (!targetSelected) {
						tempX = random.nextInt(regionWidth-2*distractorRadius)+regionWidth*i+distractorRadius;
						tempY = random.nextInt(regionHeight-2*distractorRadius)+distractorRadius;
					}
					else {
						tempX = random.nextInt(regionWidth-2*targetRadius)+regionWidth*i+targetRadius;
						tempY = random.nextInt(regionHeight-2*targetRadius)+targetRadius;
					}
					overlapped = region.isOverlapped(tempX, tempY, distractorRadius, targetRadius, targetSelected);
					loopEndTime = System.currentTimeMillis();
					timeOver = ((loopEndTime - loopStartTime) > 1)? true : false;
				} while (overlapped & !timeOver);
				if (!overlapped) {
					region.setPosition(tempX, tempY, targetSelected);
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

		positionSetCompleted = true;
		repaint();
	}
	
	public void SaveSuRTResult() {
		long responseTime = endTime - startTime;
		boolean success = regionArray.get(confirmedRegion).getIsTargetRegion();
		
		try {
			bufferedWriter.write(format.format(startTime) + "," + String.valueOf(responseTime*0.001) + "," + String.valueOf(success));
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		countTask++;
		if (countTask == numTask) {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
	
	public static void main (String[] args) {
		@SuppressWarnings("unused")
		SuRT surt = new SuRT("SuRTsetting.csv");
	}
}