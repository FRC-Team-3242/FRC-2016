package com.fhsemit.first.saventi.stronghold;

import edu.wpi.first.wpilibj.Timer;

/**
 * UNSTABLE
 */
public class Toggle {

    private boolean state;
    private Timer timer;
    final double minToggleTime = 0.5;
    
    public Toggle(boolean start){
    	state = start;
    	timer = new Timer();
    	timer.start();
    	
    }
    
    public void reset(){
    	state = false;
    }
    
    public void toggle(){
    	if(timer.get() > minToggleTime){
    		state = !state;
    		timer.reset();
    	}
    }
    
    public boolean getState(){
    	return state;
    }
}
