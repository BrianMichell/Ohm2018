package org.usfirst.frc.team1817.robot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PWMSpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;

public class Intake {

	private final int STOWED_THRESH = 20;
	private final int EXTENDED_THRESH = 100;

	private boolean inThread = false;

	private SpeedControllerGroup intake, wrist;
	private Encoder wristEncoder;

	public Intake(PWMSpeedController intake, PWMSpeedController wrist, Encoder wristEncoder) {
		this.intake = new SpeedControllerGroup(intake);
		this.wrist = new SpeedControllerGroup(wrist);
		this.wristEncoder = wristEncoder;
	}

	/**
	 * Gets the position of the intake
	 * 
	 * @return true if the intake is in its travel position
	 */
	public boolean stowed() {
		return (wristEncoder.get() <= STOWED_THRESH);
	}

	/**
	 * Gets the position of the intake
	 * 
	 * @return true if the intake is ready to injest a cube
	 */
	public boolean extended() {
		return (wristEncoder.get() >= EXTENDED_THRESH);
	}

	/**
	 * Intakes a cube not dependent on if it's stowed or not
	 * 
	 * @param speed
	 *            Speed at which the motor spins
	 */
	public void injest(double speed) {
		if (stowed())
			speed *= -1;
		intake.set(speed);
	}

	/**
	 * Eject a cube not dependent on if it's stowed or not
	 * 
	 * @param speed
	 *            Speed at which the motor spins
	 */
	public void expel(double speed) {
		if (stowed())
			speed *= -1;
		intake.set(speed);
	}

	/**
	 * Puts the intake in travel position
	 */
	public void stow() {
		if (inThread)
			return;
		PIDController pid = new PIDController(0, 0, 0, wristEncoder, wrist);
		pid.setAbsoluteTolerance(15.0);
		pid.setOutputRange(-0.5, 0.5);
		pid.setSetpoint(STOWED_THRESH);
		pid.enable();
		new Thread(() -> {
			inThread = true;
			while (!pid.onTarget() && Math.abs(wristEncoder.getRate()) > 1) {
				// do nothing
			}
			pid.free();
			inThread = false;
		}).start();
	}

	/**
	 * Extends the intake to prepare for injesting a cube
	 */
	public void extend() {
		if (inThread)
			return;
		PIDController pid = new PIDController(0, 0, 0, wristEncoder, wrist);
		pid.setAbsoluteTolerance(15.0);
		pid.setOutputRange(-0.5, 0.5);
		pid.setSetpoint(EXTENDED_THRESH);
		pid.enable();
		new Thread(() -> {
			inThread = true;
			while (!pid.onTarget() && Math.abs(wristEncoder.getRate()) > 1) {
				// do nothing
			}
			pid.free();
			inThread = false;
		}).start();
	}

	/**
	 * Moves the intake into travel position 
	 * @param speed Speed at which the motor spins
	 */
	public void manualStow(double speed) {
		killAutoMovement();
		wrist.set(speed);
	}

	/**
	 * Moves the intake to prepare to injest a cube
	 * @param speed Speed at which the motor spins
	 */
	public void manualExtend(double speed) {
		killAutoMovement();
		wrist.set(-speed);
	}

	private void killAutoMovement() {
		inThread = false;
	}

	/**
	 * Is the intake subsystem automatically doing something
	 * @return True if it is, False otherwise
	 */
	public boolean inAction() {
		return inThread;
	}
}
