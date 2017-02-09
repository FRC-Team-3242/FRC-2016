package com.fhsemit.first.saventi.stronghold;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class RPIVision {
	
	private NetworkTable table;
	private boolean found;
	private double x;
	private double y;

    private int aimState;
    private RobotDrive drive;
    private final int targetX = 640;
    private final int targetY = 360;
    private final double horizontalScalar = 1/1280;
    private final double verticalScalar = 1/720;
    private final double targetTolerance = 0.1;
	
	public RPIVision(RobotDrive drive){
		this.drive = drive;
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
	
	public void autoAim(boolean startAim){
    	switch(aimState){
    	case 0:
    		if(startAim){
    			aimState++;
    		}
    		break;
    	case 1:
    		double turnVal = orthogonalAim(x, targetX, horizontalScalar);
    		if(turnVal == 0){
    			aimState++;
    		}else{
        		drive.arcadeDrive(0, turnVal);
    		}
    		break;
    	case 2:
    		if(dualAim() == 0){
    			aimState = 0;
    		}
    		break;
    	}
    }
    
    public double dualAim(){
		double turn = orthogonalAim(x, targetX, horizontalScalar);
		double move = orthogonalAim(y, targetY, verticalScalar);
		drive.arcadeDrive(move, turn);
		return move + turn;
    }
    
    public double orthogonalAim(double actualVal, double targetVal, double scalar){
    	double error = actualVal - targetVal;
    	error *= scalar;
    	if(Math.abs(error) < targetTolerance){
    		return 0;
    	}
    	return error;
    }
	
	
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
}
