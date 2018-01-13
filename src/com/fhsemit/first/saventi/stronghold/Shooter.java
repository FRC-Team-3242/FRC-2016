package com.fhsemit.first.saventi.stronghold;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Relay.Direction;
import edu.wpi.first.wpilibj.Timer;


public class Shooter {
	private WPI_TalonSRX shooterWheel;
	private Relay flipper;
	AnalogInput flipPot;
	private DoubleSolenoid pusher;
	private Encoder encoder;
	private PIDController pid;
	
	//shooter state machine
	private Timer shootTimer;
	private int shootState;
	private final double shootTime = 1.5;
	
	//pusher state machine
	private Timer pushTimer;
	private int pushState;
	private Toggle reverseToggle;
	//Pusher timings in seconds
	private final double pushTime = 1.5;
	private final double retractTime = 0.8;
	
	//flipper extents
	private final double extendMax = 3.76;//TODO MAX: 4.175 - 4.26
	private final double retractMax = 0.19;//0.002	
	
	public Shooter(WPI_TalonSRX wheel, Relay flip, DoubleSolenoid push, AnalogInput flipPot){
		this.shooterWheel = wheel;
		this.flipper = flip;
		this.flipPot = flipPot;
		this.pusher = push;
		pushTimer = new Timer();
		pushTimer.start();
		pushState = 0;
		shootTimer = new Timer();
		shootTimer.start();
		shootState = 0;
		reverseToggle = new Toggle(false);
	}
	
	public void resetState(){
		shootState = 0;
		pushState = 0;
		reverseToggle = new Toggle(false);
	}
	
	//TODO config pid and encoder
	/**
	 * 
	 * @param encoder use 1x or 2x encoding
	 * @deprecated
	 */
	public void configEncoder(Encoder encoder){
		encoder.setSamplesToAverage(10);
		encoder.setDistancePerPulse(0.025);//per CIM revolution (assuming x2)
		encoder.setPIDSourceType(PIDSourceType.kRate);
		this.encoder = encoder;
		pid = new PIDController(0.9,0,0.1,encoder,shooterWheel);
	}
	
	/**
	 * directly sets the speed of the shooter wheel
	 * also updates the status of the flipper solenoid
	 * @param speed from -1 to 1, where 1 is reversing
	 */
	public void spinWheel(double speed){
		if(speed >= 0.1){
			shooterWheel.set(speed*0.6);
		}else{
			shooterWheel.set(speed);
		}
	}
	
	
	public void spinWheelButton(boolean forward, boolean reverse){
		if(reverse){
			reverseToggle.toggle();
		}
		if(forward){
			shooterWheel.set(-1);
		}else if(reverseToggle.getState()){
			shooterWheel.set(0.6);
		}else{
			shooterWheel.set(0);
		}
	}
	
	public void spinWheelTimer(boolean forward, boolean reverse){
		switch(shootState){
		case 0:
			spinWheelButton(forward, reverse);
			if(forward){
				shootState = 1;
			}
			break;
		case 1://waiting for button to be pressed
			shooterWheel.set(-1);
			if(pushState != 0){
				shootState = 2;
				shootTimer.reset();
			}
			break;
		case 2://keep spinning a little after shoot button is pressed
			shooterWheel.set(-1);
			if(shootTimer.get() >= shootTime){
				shootState = 0;
			}
			break;
		}
	}
	
	public void spinWheelVolts(double volts){
		shooterWheel.set(Robot.voltToPercent(volts));
	}
	
	/**
	 * tilts the shooter housing to grab or shoot a boulder, or to shift the 
	 * center of mass for driving
	 * Reverse is up, forward is down
	 * 
	 * @param up towards shooting position
	 * @param down towards grabbing position
	 */
	public void setFlipper(boolean up, boolean down){
		
		if(up && this.flipPot.getAverageVoltage() >  retractMax){//Contracting
			flipper.set(Relay.Value.kForward);
		}else if(down && this.flipPot.getAverageVoltage() < extendMax){//Contracting
			flipper.set(Relay.Value.kReverse);
		}else{
			flipper.set(Relay.Value.kOff);
		}
	}
	/**
	 * must be called after setFlipper()
	 * @param down
	 * @param up
	 */
	public void setFlipperOverride(boolean down, boolean up){
		
		if(up){//definite extending
			flipper.set(Relay.Value.kReverse);
		}else if(down){//definite contracting
			flipper.set(Relay.Value.kForward);
		}
	}
	
	public void setFlipperPOV(int povDirection){
		setFlipper(povDirection == 180, povDirection == 0);
	}
	
	public void setFlipperAxis(double axis){
		setFlipper(axis < -0.15  , axis > 0.15);
	}
	
	@Deprecated
	public void setFlipperAlt(boolean up, boolean down){
		if(up){
			flipper.setDirection(Direction.kForward);
			flipper.set(Relay.Value.kForward);
		}else if(down){
			flipper.setDirection(Direction.kReverse);
			flipper.set(Relay.Value.kReverse);
		}else{
			flipper.set(Relay.Value.kOff);
		}
	}
	
	
	//Reverse opens, forward closes
	public void setPush(boolean down, boolean push){
		if(down){
			pusher.set(DoubleSolenoid.Value.kForward);
			//pusher.set(DoubleSolenoid.Value.kForward);
		}else if(push){
			push(true);
		}else{
			pusher.set(DoubleSolenoid.Value.kOff);
		}
		push(false);
	}
	
	/**
	 * starts the flipper solenoid, must be followed by the update function
	 * @param up whether to flip upwards or downwards, assuming "forward" is up
	 */
	public void push(boolean start){
		switch(pushState){
		case 0:
			pusher.set(Value.kOff);
			if(start){
				pusher.set(Value.kReverse);
				pushState++;
				pushTimer.reset();
			}
			break;
		case 1:
			pusher.set(Value.kReverse);
			if(pushTimer.get() >= pushTime){
				pusher.set(Value.kForward);
				pushState++;
				pushTimer.reset();
			}
			break;
		case 2:
			pusher.set(Value.kForward);
			if(pushTimer.get() >= retractTime){
				pusher.set(Value.kOff);
				pushState = 0;
				pushTimer.reset();
			}
			break;
		}
		/*
		if(pusher.get()){
			if(pushTimer.get() >= pushTime){
				pusher.set(false);
			}
		}else if(start){
			pushTimer.reset();
			pusher.set(true);
		}
		*/
	}
	
}
