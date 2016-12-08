package com.fhsemit.first.saventi.stronghold;

import edu.wpi.first.wpilibj.CANTalon;

public class AuxArm {
	//5 and 6
	private CANTalon bicep;
	private CANTalon foreArm;
	private final double speedLim = 0.4;
	
	public AuxArm(CANTalon bicep,CANTalon fore){
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
