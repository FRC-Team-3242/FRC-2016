package com.fhsemit.first.saventi.stronghold;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class RPIVision {
	
	private NetworkTable table;
	private boolean found;
	private double x;
	private double y;
	
	public RPIVision(){
		table = NetworkTable.getTable("rpi");
		found = false;
		x = -1;
		y = -1;
	}
	
	public boolean update(){
		found = table.getBoolean("found", false);
    	if(found){
        	x = table.getNumber("visionX", -1);
    		y = table.getNumber("visionY", -1);
    	}
    	return found;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
}
