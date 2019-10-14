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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;


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
	
	private boolean isControllerUsed = false;
	private Controller targetController;
	private Identifier targetLeftRightComponentIdentifier;
	private float leftLowerBound;
	private float leftUpperBound;
	private float rightLowerBound;
	private float rightUpperBound;
	private Identifier targetConfirmComponentIdentifier;
	private float confirmLowerBound;
	private float confirmUpperBound;
	private ControllerListenerThread controllerListenerThread;
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private long startTime;
	private long endTime;
	private long pauseStartTime;
	private long pausedTime;
	
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	
	public SuRT(String inputFileName) {		
		super("Life Enhancing Technology Lab. - Surrogate Reference Task");
	
		this.setUndecorated(true);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setVisible(true);
		this.addKeyListener(new MainKeyListener());
		this.addMouseListener(new MainMouseListener());
		this.setFocusable(true);
		
		SettingDialog dialog = new SettingDialog(this);
		dialog.setVisible(true);
		countTask = 0;
		
		try {
			File inputFile = new File(inputFileName);
			bufferedReader = new BufferedReader(new FileReader(inputFile));
			String outputFileName = participantName+".csv";
			File outputFile = new File(outputFileName);
			if(outputFile.exists() == false) 
				outputFile.createNewFile();
			bufferedWriter = new BufferedWriter(new FileWriter(outputFile, true));
			
			// Save column headers
			String line = bufferedReader.readLine();
			String columnHeaderArray[] = line.split(",");
			bufferedWriter.write("Experiment Start Time" + ",");
			for (int i=0; i<columnHeaderArray.length; i++)
				bufferedWriter.write(columnHeaderArray[i] + ",");
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			// Save parameter values
			line = bufferedReader.readLine();
			String array[] = line.split(",");
			bufferedWriter.write(format.format(System.currentTimeMillis()) + ",");
			for (int i=0; i<array.length; i++)
				bufferedWriter.write(array[i] + ",");
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			// Load parameters
			numDistractor = Integer.parseInt(array[0]);
			distractorSize = Integer.parseInt(array[1]);
			targetSize = Integer.parseInt(array[2]);
			numRegion = Integer.parseInt(array[3]);
			circleLineWidth = Float.parseFloat(array[4]);
			if (isControllerUsed) {
				targetLeftRightComponentIdentifier = targetController.getComponents()[Integer.parseInt(array[5])].getIdentifier();
				leftLowerBound = Float.parseFloat(array[6]);
				leftUpperBound = Float.parseFloat(array[7]);
				rightLowerBound = Float.parseFloat(array[8]);
				rightUpperBound = Float.parseFloat(array[9]);
				targetConfirmComponentIdentifier = targetController.getComponents()[Integer.parseInt(array[10])].getIdentifier();
				confirmLowerBound = Float.parseFloat(array[11]);
				confirmUpperBound = Float.parseFloat(array[12]);
			}
			
			// Write additional column header
			bufferedWriter.newLine();
			bufferedWriter.write("Set Start Time" + ",");
			bufferedWriter.write("Response Time" + ",");
			bufferedWriter.write("Success" + ",");
			bufferedWriter.newLine(); 
			bufferedWriter.flush();
			
			bufferedReader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		pausedTime = 0;
		if (isControllerUsed) {
			controllerListenerThread = new ControllerListenerThread();
			controllerListenerThread.setStop(false);
			controllerListenerThread.start();
		}
		
		MakePositionSet();
	}
	
	public void OpenPauseDialog() {
		PauseDialog pauseDialog = new PauseDialog(this);
		pauseDialog.setVisible(true);
	}
	
	public void OpenQuitDialog() {
		QuitDialog quitDialog = new QuitDialog(this);
		quitDialog.setVisible(true);
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
				pauseStartTime = System.currentTimeMillis();
				OpenPauseDialog();
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
		
		private int width = 600;
		private int height = 230;
		
		private JPanel firstRowPanel = new JPanel();
		private JLabel participantNameLabel = new JLabel("Participant Name: ", JLabel.LEFT);
		private JTextField participantNameTextField = new JTextField();
		private JLabel numTaskLabel = new JLabel("# of Trial: ", JLabel.LEFT);
		private JTextField numTaskTextField = new JTextField();
		
		private JPanel secondRowPanel = new JPanel();
		private JLabel controllerInputLabel = new JLabel("Controller Input", JLabel.LEFT);
		private JCheckBox controllerCheckBox = new JCheckBox("", false);
		private JLabel emptyLabel = new JLabel("", JLabel.LEFT);
		
		private JPanel thirdRowPanel = new JPanel();
		private JComboBox<String> controllerCombo;
		private Controller[] controllers = {};
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
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			firstRowPanel.setLayout(new FlowLayout());
			firstRowPanel.setPreferredSize(new Dimension(600, 30));
			participantNameLabel.setPreferredSize(new Dimension(120, 20));
			firstRowPanel.add(participantNameLabel);
			participantNameTextField.setPreferredSize(new Dimension(160, 20));
			firstRowPanel.add(participantNameTextField);
			numTaskLabel.setPreferredSize(new Dimension(120, 20));
			firstRowPanel.add(numTaskLabel);
			numTaskTextField.setPreferredSize(new Dimension(160, 20));
			firstRowPanel.add(numTaskTextField);
			
			secondRowPanel.setLayout(new FlowLayout());
			secondRowPanel.setPreferredSize(new Dimension(600, 30));
			controllerInputLabel.setPreferredSize(new Dimension(120, 20));
			secondRowPanel.add(controllerInputLabel);
			controllerCheckBox.setPreferredSize(new Dimension(20, 20));
			secondRowPanel.add(controllerCheckBox);
			emptyLabel.setPreferredSize(new Dimension(425, 20));
			secondRowPanel.add(emptyLabel);
			
			String[] controllerName = {};
			controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
			controllerName = new String[controllers.length];		
			for (int i = 0; i<controllerName.length; i++) {
				controllerName[i] = controllers[i].getName() + " / " + controllers[i].getType();
			}
			controllerCombo = new JComboBox<String>(controllerName);
			controllerCombo.setEnabled(false);

			thirdRowPanel.setLayout(new FlowLayout());
			thirdRowPanel.setPreferredSize(new Dimension(600, 30));
			controllerCombo.setPreferredSize(new Dimension(510, 20));
			thirdRowPanel.add(controllerCombo);
			okButton.setPreferredSize(new Dimension(60, 20));
			thirdRowPanel.add(okButton);
			
			controllerCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == 1)
						controllerCombo.setEnabled(true);
					else
						controllerCombo.setEnabled(false);
				}
			});
			
			add(firstRowPanel, "North");
			add(secondRowPanel, "North");
			add(thirdRowPanel, "North");
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
			
			if (controllerCheckBox.isSelected()) {
				isControllerUsed = true;
				targetController = controllers[controllerCombo.getSelectedIndex()];		
			}
			
			dispose();
		}
	}
	
	class PauseDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		
		private int width = 250;
		private int height = 70;
		
		private JButton continueButton = new JButton("Continue");
		private JButton quitButton = new JButton("Quit");
		
		public PauseDialog(JFrame frame) {
			super(frame, "", true);
			setLayout(new FlowLayout());
			setSize(width, height);
			setLocation((SuRT.super.getWidth()-width)/2, (SuRT.super.getHeight()-height)/2);
			this.setFocusable(true);
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			continueButton.setPreferredSize(new Dimension(100, 20));
			quitButton.setPreferredSize(new Dimension(100, 20));
			add(continueButton);
			add(quitButton);
			
			continueButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pausedTime = System.currentTimeMillis() - pauseStartTime;
					dispose();
				}
			});
			
			quitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Quit();
				}
			});
			
			KeyListener escListener = new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == 27) {
						pausedTime = System.currentTimeMillis() - pauseStartTime;
						setVisible(false);
					}
				}
				
				@Override
				public void keyTyped(KeyEvent e) {}

				@Override
				public void keyReleased(KeyEvent e) {}
			};
			
			this.addKeyListener(escListener);
		}
	}
	
	class QuitDialog extends JDialog {
		private static final long serialVersionUID = 1L;
		
		private int width = 250;
		private int height = 70;
		
		private JButton newTrialButton = new JButton("New Trial");
		private JButton quitButton = new JButton("Quit");
		
		public QuitDialog(JFrame frame) {
			super(frame, "", true);
			setLayout(new FlowLayout());
			setSize(width, height);
			setLocation((SuRT.super.getWidth()-width)/2, (SuRT.super.getHeight()-height)/2);
			this.setFocusable(true);
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			newTrialButton.setPreferredSize(new Dimension(100, 20));
			quitButton.setPreferredSize(new Dimension(100, 20));
			add(newTrialButton);
			add(quitButton);
			
			newTrialButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
					@SuppressWarnings("unused")
					SuRT surt = new SuRT("SuRTsetting.csv");
				}
			});
			
			quitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Quit();
				}
			});
		}
	}
	
	public void paint(Graphics g) {
		img = createImage(super.getWidth(), super.getHeight());
		img_g = img.getGraphics();
		Graphics2D g2 = (Graphics2D)img_g;
		
		if (positionSetCompleted) {
			int regionHeight = super.getHeight();
			for (int i=0; i<numRegion; i++) {
				Region region = regionArray.get(i);
				if (i==confirmedRegion) {
					g2.setColor(new Color(160, 160, 160));
				}
				else {
					g2.setColor(Color.WHITE);
				}
				g2.fillRect(region.getLeftX(), 0, region.getRightX()-region.getLeftX()+1, regionHeight);
				

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
		long responseTime = endTime - startTime - pausedTime;
		boolean success = regionArray.get(confirmedRegion).getIsTargetRegion();
		
		try {
			bufferedWriter.write(format.format(startTime) + "," + String.valueOf(responseTime*0.001) + "," + String.valueOf(success));
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pausedTime = 0;
		
		countTask++;
		if (countTask == numTask) {
			OpenQuitDialog();
		}
	}
	
	public void Quit() {
		// Close BufferedWriter
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Stop controllerListener
		if (isControllerUsed)
			controllerListenerThread.setStop(true);
		
		System.exit(0);
	}
	
	class ControllerListenerThread extends Thread {
		private boolean stop;
		
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		
		public void run() {
			while (!stop) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {}
				targetController.poll();
				
				float leftRightPolledData = targetController.getComponent(targetLeftRightComponentIdentifier).getPollData();
				float confirmPolledData = targetController.getComponent(targetConfirmComponentIdentifier).getPollData();
				if (leftRightPolledData >= leftLowerBound && leftRightPolledData <= leftUpperBound) {
					if (confirmedRegion != -1)
						confirmedRegion = (confirmedRegion == 0)? 0 : confirmedRegion-1;
					else
						confirmedRegion = (numRegion-1)/2;
					repaint();
				}
				else if (leftRightPolledData >= rightLowerBound && leftRightPolledData <= rightUpperBound) {
					if (confirmedRegion != -1)
						confirmedRegion = (confirmedRegion == numRegion-1)? numRegion-1 : confirmedRegion+1;
					else
						confirmedRegion = numRegion/2;
					repaint();
				}
				else if (confirmPolledData >= confirmLowerBound && confirmPolledData <= confirmUpperBound) {
					if (confirmedRegion != -1) {
						endTime = System.currentTimeMillis();
						SaveSuRTResult();
						MakePositionSet();
					}
				}
			}
		}
	}
	
	public static void main (String[] args) {
		@SuppressWarnings("unused")
		SuRT surt = new SuRT("SuRTsetting.csv");
	}
}