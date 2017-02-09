package com.fhsemit.first.saventi.stronghold;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Encoder;

public class Climber {
	CANTalon leftArm;
	CANTalon rightArm;
	private DoubleSolenoid leaner;
	public Encoder encoder;
	private final double maxDist = 79.35;//79.388
	private final double minDist = -3;
	
	public Climber(CANTalon left, CANTalon right, DoubleSolenoid leaner, Encoder encoder){
		if(right != null){
			right.setInverted(true);
		}
		leftArm = left;
		rightArm = right;
		this.leaner = leaner;
		configEncoder(encoder);
	}
	
	/**
	 * 
	 * @param encoder use 2x or 4x encoding
	 */
	private void configEncoder(Encoder encoder){
		encoder.setSamplesToAverage(5);
		encoder.setDistancePerPulse(0.025);//per CIM revolution (assuming x2)
		this.encoder = encoder;
	}
	
	public void climbMotors(double speed){
		leftArm.set(speed);
		rightArm.set(speed);
	}
	
	/**
	 * Sets the speed of both of the climber arms
	 * @param speed from -1 to 1, where 1 is down
	 */
	public void climb(double speed){
		double dist = encoder.getDistance();
		if(speed < 0 && dist >= maxDist){//if going up and past limit
			speed = 0;
		}
		if(speed > 0 && dist <= minDist){//if going down and past limit
			speed = 0;
		}
		climbMotors(speed);
		
	}
	
	/**
	 * assumes +1 is down
	 * @param pov
	 */
	public void climbPov(int pov){
		if(pov == 0){
			climb(-0.8);
		}else if(pov == 180){
			climb(0.8);
		}else{
			climb(0);
		}
	}
	
	public void climbOverride(boolean down, boolean up){
		if(down){
			climbMotors(1);
		}else if(up){
			climbMotors(-1);
		}//no extra else, because it would override the default method
	}
	
	@Deprecated
	public void lean(int degree){
		Value val = leaner.get();
		if(degree == 180){//down
			val = Value.kForward;
		}
		if(degree == 0){//up
			val = Value.kReverse;
		}
		leaner.set(val);
	}
	
	
	public void lean(boolean forwards, boolean backwards){
		Value val = leaner.get();
		if(forwards){
			val = Value.kForward;
		}
		if(backwards){
			val = Value.kReverse;
		}
		leaner.set(val);
	}
}
