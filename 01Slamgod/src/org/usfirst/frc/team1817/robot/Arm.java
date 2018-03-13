package org.usfirst.frc.team1817.robot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedControllerGroup;

public class Arm {

	private final int STOWED_THRESH = 50;

	// if inThread is true deny extra autonomous movement actions until the current
	// one has released the thread
	private boolean inThread = false;

	private SpeedControllerGroup elbowMotors;
	private SpeedControllerGroup winchMotors;
	private Encoder elbowEncoder;
	//private Encoder winchEncoder;

	//public Arm(SpeedControllerGroup shoulder, SpeedControllerGroup winch, Encoder shoulderEncoder, Encoder winchEncoder) {
	public Arm(SpeedControllerGroup shoulder, SpeedControllerGroup winch, Encoder shoulderEncoder) {
		elbowMotors = shoulder;
		this.elbowEncoder = shoulderEncoder;
		this.winchMotors = winch;
		//this.winchEncoder = winchEncoder;
	}

	/**
	 * @return true if the lift is lowered enough
	 */
	public boolean stowed() {
		return elbowEncoder.get() <= STOWED_THRESH;
	}

	/**
	 * Pulls the lift into its smallest configuration.
	 */
	/*
	public void retract() {
		if (inThread)
			return;
		//TODO Tune or remove
		PIDController pid = new PIDController(0, 0, 0, winchEncoder, winchMotors);
		pid.setAbsoluteTolerance(15);
		pid.setOutputRange(-0.5, 0.5);
		pid.setSetpoint(STOWED_THRESH);
		pid.enable();
		new Thread(() -> {
			inThread = true;
			while (!pid.onTarget() && Math.abs(winchEncoder.getRate()) > 1 && inThread) {
				// do nothing
			}
			pid.free();
			inThread = false;
		}).start();
	}
	*/
	/**
	 * Extends the lift to its full range
	 */
	/*
	public void extend() {
		if (inThread)
			return;
		//TODO Tune or remove
		PIDController pid = new PIDController(0, 0, 0, winchEncoder, winchMotors);
		pid.setAbsoluteTolerance(15.0);
		pid.setOutputRange(-0.5, 0.5);
		pid.setSetpoint(360);
		pid.enable();
		new Thread(() -> {
			inThread = true;
			while (!pid.onTarget() && Math.abs(winchEncoder.getRate()) > 1 && inThread) {
				// do nothing
			}
			pid.free();
			inThread = false;
		}).start();
	}
	*/
	/**
	 * Sets the lift to a desired angle. This is run on a PID loop that WILL NOT run
	 * forever. Once it reaches a set tolerance it will terminate. Another function
	 * may be needed to compensate for sag.
	 * 
	 * @param angle
	 *            The angle at which the intake should be positioned
	 */

	//TODO This is not going to work as is. Need to calculate the angle, not use a raw angle
	/*
	public void setAngle(double angle) {
		if (inThread)
			return;
		//TODO Tune or remove
		PIDController pid = new PIDController(0, 0, 0, elbowEncoder, elbowMotors);
		pid.setAbsoluteTolerance(15);
		pid.setOutputRange(-0.5, 0.5);
		pid.setSetpoint(angle);
		pid.enable();
		new Thread(() -> {
			inThread = true;
			while (!pid.onTarget() && Math.abs(elbowEncoder.getRate()) > 1 && inThread) {
				// do nothing
			}
			pid.free();
			inThread = false;
		}).start();
	}
	*/
	/**
	 * Manual change of the height of the lift
	 * 
	 * @param value
	 *            Speed at which the motors run
	 */
	public void manualExtend(double value) {
		killAutoMovement();
		winchMotors.set(value);
	}

	/**
	 * Manual change of the angle for the lift
	 * 
	 * @param value
	 *            Speed at which the motors run
	 */
	public void manualAngle(double value) {
		killAutoMovement();
		elbowMotors.set(value);
	}

	private void killAutoMovement() {
		inThread = false;
	}

	/**
	 * Is the lift subsystem doing something automatically
	 * 
	 * @return True if it is, False otherwise
	 */
	public boolean inAction() {
		return inThread;
	}

	//TODO Set this up if used. Remove it otherwise
	@SuppressWarnings("unused")
	private double getAngle() {
		return 0.6 * (elbowEncoder.get() / 5.0);
	}
}
