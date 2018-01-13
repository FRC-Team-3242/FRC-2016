package com.fhsemit.first.saventi.stronghold;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class AuxArm {
	//5 and 6
	private WPI_TalonSRX bicep;
	private WPI_TalonSRX foreArm;
	private final double speedLim = 0.4;
	
	public AuxArm(WPI_TalonSRX bicep,WPI_TalonSRX fore){
		this.bicep = bicep;
		this.foreArm = fore;
	}
	
	public void set(double bicepSpeed, double forearmSpeed){
		bicep.set(bicepSpeed * speedLim);
		foreArm.set(forearmSpeed * speedLim);
	}
	
	public void setBicep(double speed){
		bicep.set(speed * speedLim);
	}
	
	public void setForearm(double speed){
		foreArm.set(speed * speedLim);
	}
}
