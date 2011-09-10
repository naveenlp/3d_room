package com.android.NeHeHoneycomb;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

public class NeHeHoneycombHome extends Activity  {
	//http://blog.radioactiveyak.com/2011/01/how-to-use-gyroscope-api-and-remain.html
    /** Called when the activity is first created. */
	/** The OpenGL View */
	private GLSurfaceView glSurface;
	private SensorManager mSensorManager;
	private Lesson06 renderer = new Lesson06(this);
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp = 0;
	float angle[] = new float[3];


	class Averager 
	{
		private LinkedList<float[]> values = new LinkedList<float[]>();
		int sampleSize = 5;
		float[] average = new float[3];
		
		public Averager(){
			average[0] = 0;
			average[1] = 0;
			average[2] = 0;
			for(int i=0;i<sampleSize;i++)
				values.add(average.clone());
		}
		
		public void addToQueue(float a[]){
			float[] currentReading = new float[3];
			currentReading = a.clone();
			values.add(currentReading);
			updateAverage(currentReading);
			
			
		}
		
		public float[] returnAverage()
		{
			return average.clone();
		}
		
		private void updateAverage(float[] newNumber)
		{
			float[] oldNumber = new float[3];
			oldNumber = values.poll();
			
			int size = values.size();
			
			average[0]+=(newNumber[0]-oldNumber[0])/sampleSize;
			average[1]+=(newNumber[1]-oldNumber[1])/sampleSize;
			average[2]+=(newNumber[2]-oldNumber[2])/sampleSize;
			
		}
		
	}

        	
	float accelerometerValues[] = new float[3];
	float geomagneticMatrix[] = new float[3];
	Averager accAverager = new Averager();
	Averager magAverager = new Averager();
	
	boolean sensorReady = false, accReady = false;
	float[] R = new float[16];
	float[] I = new float[16];
	private final SensorEventListener gyroListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) { 
			boolean conditionVariable = false;
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				for(int i=0;i<3;i++)
				{
					if(Math.abs(event.values[i]-accelerometerValues[i])>0.4)
					{
						conditionVariable = true;
					}
				}
				if(conditionVariable)
				{
					accAverager.addToQueue(event.values.clone());
					accelerometerValues = event.values.clone();
					accReady = true;
				}
				
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				for(int i=0;i<3;i++)
				{
					if(Math.abs(event.values[i]-geomagneticMatrix[i])>0.4)
					{
						conditionVariable = true;
					}
				}
				if(conditionVariable)
				{
					magAverager.addToQueue(event.values.clone());
					geomagneticMatrix = event.values.clone();
					sensorReady = true;
				}
				
				//geomagneticMatrix = event.values.clone();
				//sensorReady = true;
				break;
			default:
				break;
			}   

			if (sensorReady) {
				sensorReady = false;
				accReady = false;
				SensorManager.getRotationMatrix(R, I, accAverager.returnAverage(), magAverager.returnAverage());
				renderer.rotationMatrix = R.clone();
				renderer.isRotationMatrixReady = true;
			}
			/*if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
        		//    	    	if(timestamp!=0)
        		updateOrientation(event.values[0], event.values[1], event.values[2]);
        		//    	    	else{
        		//    	    		timestamp = event.timestamp;
        		//   	    		
        		//   	   	}
        	}
        	if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
        		updateOrientation(event.values[0], event.values[1], event.values[2]);
        	}*/
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
    
    float angleStep[] = new float[3];
    float tempStore = 0;
    private void updateOrientation(float heading, float pitch, float roll) {
    	  // Update the UI
    	//Log.d("GYRO","GYRO heading: "+heading+" pitch: "+pitch+" roll: "+roll);
    	
    	if(Math.abs(heading-angle[0])>0.001)
    	{
        	Log.d("GYRO","GYRO heading: "+heading);
    		angle[0] = heading;
    		renderer.anglerot[2] = angle[0];
    		angle[0] = heading;
    	}
    	if(Math.abs(pitch-angle[1])>0.001)
    	{
        	Log.d("GYRO","GYRO  pitch: "+pitch);
    		angle[1] = pitch;
    		if(pitch<0)
    		{
    			tempStore = 360+pitch;
    			renderer.anglerot[0] = tempStore;
    		} else
    			renderer.anglerot[0] = angle[1];
    		angle[1] = pitch;
    	}
    	/*if(Math.abs(roll-angle[2])>0.01)
    	{
        	Log.d("GYRO","GYRO roll: "+roll);
        	if(roll>0)
        	{
        		renderer.anglerot[2] = -1*angle[2];
        	}
        	if(roll<0)
        	{
        		//then it is going from 90 to 0
        		renderer.anglerot[2] = 180+angle[2];
        	}
        	angle[2] = roll;

    	}*/
        
   		
   	 
    	/* final float dT = (timestampcurrent - timestamp) * NS2S;
    	// if(Math.abs(heading*dT)>0.001)
    	 angle[0] += heading * dT;
    	 //if(Math.abs(pitch*dT)>0.001)
    	 angle[1] += pitch * dT;
    	 //if(Math.abs(roll*dT)>0.001)
    	 angle[2] += roll * dT;
    	 timestamp = timestampcurrent;
    	 
    	 
    	 for (int i=0;i<3;i++)
    	 {
    		 if(Math.abs(angle[i]-angleStep[i])>0.01)
    		 {
    			
    			 angleStep[i] = angle[i];
    			 Log.d("GYRO","GYRO heading: "+angleStep[0]+" pitch: "+angleStep[1]+" roll: "+angleStep[2]);
    			 renderer.anglerot[i] = (float) ((-1*angleStep[i]*360)/(2*Math.PI));
    		 }
    	 }*/
    	 /* if(Math.abs(Math.abs(angle[0])-Math.abs(tempangle))>5)
    	 {
    		 Log.d("GYRO","GYRO heading: "+angle[0]);
    		 tempangle = angle[0];
    	 }*/
    	
    }
     /**
	 * Initiate the OpenGL View and set our own
	 * Renderer (@see Lesson02.java)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Create an Instance with this Activity
		glSurface = new GLSurfaceView(this);
		//Set our own Renderer
		glSurface.setRenderer(renderer);
		//Set the GLSurface as View to this Activity
		setContentView(glSurface);
		
		angle[0] = 0;
		angle[1] = 0;
		angle[2] = 0;
		
		accelerometerValues[0] = 0;
		accelerometerValues[1] = 0;
		accelerometerValues[2] = 0;

		geomagneticMatrix[0] = 0;
		geomagneticMatrix[1] = 0;
		geomagneticMatrix[2] = 0;
		
		angleStep[0] = 0;
		angleStep[1] = 0;
		angleStep[2] = 0;
		
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		  // mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
		   mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		   mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
			

		//mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_UI);
        
	}

	/**
	 * Remember to resume the glSurface
	 */
	@Override
	protected void onResume() {
		super.onResume();
		glSurface.onResume();
		mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		   mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
			
		//mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
	   //mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
	    //mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
	    //mSensorManager.registerListener(gyroListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
		 //register this class as a listener for the orientation and accelerometer sensors
        //sm.registerListener(this,SensorManager.);
	}
	
	

	/**
	 * Also pause the glSurface
	 */
	@Override
	protected void onPause() {
		super.onPause();
		 mSensorManager.unregisterListener(gyroListener);
		glSurface.onPause();
	}
	
	 @Override
	    protected void onStop() {
	        // unregister listener
	       // sm.unregisterListener(this);
		 	mSensorManager.unregisterListener(gyroListener);
	        super.onStop();
	    }

	  
}