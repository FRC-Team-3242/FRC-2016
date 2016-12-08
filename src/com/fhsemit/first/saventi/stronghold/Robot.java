
package com.fhsemit.first.saventi.stronghold;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * UNSTABLE max 79.388
 */
public class Robot extends IterativeRobot {
	final boolean simpleVision = true;
    final String reachAuto = "reach";
    final String crossHalfAuto = "only cross";
    final String onlyShootAuto = "only shoot";
    final String crossShootAuto = "cross and shoot";
    String autoSelected;
    SendableChooser chooser;
    int autoState;
    Timer autoTimer;
    
    final int targetX = 640;
    final int targetY = 360;
    final double xScalar = 1/1280;
    final double yScalar = 1/720;
    final int targetTolerance = 10;
    
    Joystick mainController;
    Joystick auxController;
    Toggle overrideToggle;
    Toggle arcadeDriveMode;//false is tank drive mode
    
    RobotDrive drive;
    Encoder driveEncoder;
    final double turnScalar = 0.5;
    
    Shooter shooter;
    AuxArm auxArm;

    RPIVision vision;
    
    //use to reset encoders?
    //InternalButton testButton;
    //testButton = new InternalButton();
    //SmartDashboard.putData("test button", testButton);
    //testButton.get();
    
    //Fix ratioToScore aspect ratio and area to represent new stuff
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Only Shoot Auto", onlyShootAuto);
        chooser.addObject("Only Reach Auto", reachAuto);
        chooser.addObject("cross half speed auto", crossHalfAuto);
        chooser.addObject("Cross and Shoot Auto", crossShootAuto);
        SmartDashboard.putData("Auto choices", chooser);
        autoTimer = new Timer();
        autoTimer.start();
        
        mainController = new Joystick(1);
        auxController = new Joystick(2);
        overrideToggle = new Toggle(false);
        arcadeDriveMode = new Toggle(false);
        
        drive = new RobotDrive(new CANTalon(2),new CANTalon(3));
        driveEncoder = new Encoder(2,3,false,EncodingType.k2X);
        driveEncoder.setSamplesToAverage(10);
		driveEncoder.setDistancePerPulse(0.025);
        
        shooter = new Shooter(new CANTalon(7), new Relay(0,Relay.Direction.kBoth),
        		new DoubleSolenoid(3,2), new AnalogInput(0));
        //auxArm = new AuxArm(new CANTalon(5),new CANTalon(6));

        vision = new RPIVision();
    }
    
    private void drive(double left, double right){
    	double ly = left;
    	double ry = right;
    	if((ry > 0 && ly < 0) || (ry < 0 && ly > 0)){//if going in opposite directions
    		ly *= turnScalar;
    		ry *= turnScalar;
    	}
    	drive.tankDrive(ly,ry);
    }
    
    /**
     * uses RPIVision to aim robot
     */
    private void aim(){
    	if(vision.update()){
    		double turn = vision.getX() - targetX;
    		double move = vision.getY() - targetY;
    		if(Math.abs(turn) < targetTolerance){
    			turn = 0;
    		}
    		if(Math.abs(move) < targetTolerance){
    			move = 0;
    		}
    		drive.arcadeDrive(move * yScalar, turn * xScalar);
    	}else{
    		drive.arcadeDrive(0, 0);
    	}
    }
    
    private void auxControl(){
    	if(auxController.getRawButton(1)){						//A
    		driveEncoder.reset();
    	}
    	if(auxController.getRawButton(4)){						//Y
    		//climber.encoder.reset();
    	}
    	if(auxController.getPOV() == 180){						//D
    		shooter.setPush(true, false);
    	}
    	if(auxController.getRawAxis(3) != 0 || auxController.getRawAxis(2) != 0){
    		//climber.climbMotors(0.5 * (auxController.getRawAxis(3) - auxController.getRawAxis(2)));	//RT - LT
    	}else{
	    	//climber.leftArm.set(0.5 * auxController.getRawAxis(1));	//LY
	    	//climber.rightArm.set(0.5 * auxController.getRawAxis(5));	//RY
    	}
    }
    
    private void printDashboard(){
    	SmartDashboard.putNumber("visionX", vision.getX());
    	SmartDashboard.putNumber("visionY", vision.getY());
    	
    	SmartDashboard.putNumber("driver encoder distance", driveEncoder.getDistance());
    	SmartDashboard.putNumber("flipper actuator average", shooter.flipPot.getAverageVoltage());
    	SmartDashboard.putNumber("flipper actuator raw", shooter.flipPot.getVoltage());
    	SmartDashboard.putBoolean("Override Mode", overrideToggle.getState());
    }
    
    public static double voltToPercent(double voltage){
    	return voltage/DriverStation.getInstance().getBatteryVoltage();
    }
    
    @Override
    public void teleopInit(){
    	shooter.resetState();
    }
    
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	SmartDashboard.putBoolean("vision found", vision.update());
    	
    	
    	//drive(mainController.getRawAxis(1), mainController.getRawAxis(5));
    	drive.tankDrive(mainController.getRawAxis(1), mainController.getRawAxis(5));		//LY, RY
        shooter.setFlipperAxis(mainController.getRawAxis(2) - mainController.getRawAxis(3));//LT - RT
        shooter.setFlipperOverride(mainController.getRawButton(7), mainController.getRawButton(8));//select, start
        shooter.setPush(mainController.getRawButton(5), mainController.getRawButton(6));	//LB, RB1
        shooter.spinWheelTimer(mainController.getRawButton(2), mainController.getRawButton(3));//B, X
        
        /*	
        if(auxController.getRawButton(5) && auxController.getRawButton(6)){ 				//LB and RB
        	overrideToggle.toggle();
        }
        if(overrideToggle.getState()){
        	auxControl();
        }
        */
        
        printDashboard();
    }
    
    @Override
    public void disabledInit(){
    	//overrideToggle.reset();
    }
    
    /**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    
    public void autonomousInit() {
    	autoSelected = (String) chooser.getSelected();
		//autoSelected = SmartDashboard.getString("Auto choices", reachAuto);
		System.out.println("Auto selected: " + autoSelected);
		switch(autoSelected) {
		case onlyShootAuto:
			autoState = 200;
			break;
    	case reachAuto:
    		autoState = 100;
            break;
    	case crossHalfAuto:
    	case crossShootAuto:
    	default:
    		autoState = 0;
            break;
    	}
		autoTimer.reset();
    	driveEncoder.reset();
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	printDashboard();
    	switch(autoState){
    	case 0://cross defense, and optionally shoot
    		drive.arcadeDrive(-0.6,0);
    		if(driveEncoder.getDistance() >= 60 || this.autoTimer.get() > 12){
    			autoState = 1;//start shooting
    			if(autoSelected.equals(crossHalfAuto)){
    				autoState = -1;
    			}
    			driveEncoder.reset();
    			autoTimer.reset();
    		}
    		break;
    		
		    	case 4://turn left slightly (aim to goal)
		    		drive.arcadeDrive(0,-0.2);
		    		if(driveEncoder.getDistance() >= 1.5){//TODO config
		    			autoState = 1;//start shooting
		    			autoTimer.reset();
		    		}
		    		break;
    		
    	case 1://spin up shooter
    		shooter.spinWheel(0.9);
    		if(autoTimer.get() >= 2){
    			shooter.push(true);//starts pushing
    			autoState = 2;
    			autoTimer.reset();
    		}
    		break;
    	case 2://shoot
    		shooter.push(false);//continues to push until internal timer goes off
    		shooter.spinWheel(1);
    		if(autoTimer.get() >= 1.4){
    			autoState = 3;//++
    			autoTimer.reset();
    		}
    		break;
    	case 3://stop shooting
    		shooter.spinWheel(0);
    		shooter.push(false);
    		if(autoTimer.get() >= 500){
    			autoState = -1;
    		}
    		break;
    	
    		
    	case 100://only reach a defense
    		drive.arcadeDrive(-0.75,0);
    		if(driveEncoder.getDistance() >= 29 || this.autoTimer.get() > 8){
    			autoState = -1;
    		}
    		break;
    	case 200://spin up wheel
    		shooter.spinWheelTimer(true, false);
    		if(autoTimer.get() > 3){
    			autoState = 201;
    			autoTimer.reset();
    		}
    		break;
    	case 201://start shooting and stop spinning wheel
    		shooter.push(true);
    		shooter.spinWheelTimer(false, false);
    		autoState = 202;
    		break;
    	case 202://continue/stop shooting and stop spinning wheel
    		shooter.push(false);
    		shooter.spinWheelTimer(false, false);
    		if(autoTimer.get() > 4){
    			autoState = -1;
    		}
    		break;
    	}
    }
}
