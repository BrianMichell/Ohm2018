package org.usfirst.frc.team1817.robot;

import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PWMSpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Hand implements Runnable {

	private final int STOWED_THRESH = 0;
	private final int EXTENDED_THRESH = -180;
	private final int SCORE_THRESH = -90; // Ideally, half way between
	private final double DEADBAND = 0.05;
	private final double REDUCED = 0.35;
	private final double MAX = 0.75;

	private int state = 0;
	private final int STOW = 1;
	private final int EXTEND = 2;
	private final int SCORE = 3;

	private boolean inThread = false;

	private SpeedControllerGroup intake, wrist;
	private Encoder wristEncoder;
	private Thread t;

	public Hand(SpeedControllerGroup intake, PWMSpeedController wrist, Encoder wristEncoder) {
		this.intake = intake;
		this.wrist = new SpeedControllerGroup(wrist);
		this.wristEncoder = wristEncoder;
		t = new Thread(this, "Hand");
		t.start();
	}

	public double normalize(double value, double max) {
		return Math.max(-max, Math.min(value, max));
	}

	public double deadBand(double value) {
		return Math.abs(value) > DEADBAND ? value : 0;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			double speed = 0.0;
			double rate = 50.0;

			SmartDashboard.putNumber("Hand State", state);
			switch (state) {
			case 0: // Nothing
				if (wristEncoder.getDistance() < -10)
					wristEncoder.reset();
				break;
			case STOW:
				speed = STOWED_THRESH - wristEncoder.getDistance();
				if (wristEncoder.getDistance() < -10)
					state = 0;
				break;
			case EXTEND:
				speed = EXTENDED_THRESH - wristEncoder.getDistance();
				break;
			case SCORE:
				speed = SCORE_THRESH - wristEncoder.getDistance();
				break;
			}

			SmartDashboard.putNumber("RAW Speed of wrist", speed);

			speed /= rate;
			//			if (wristEncoder.getDistance() < SCORE_THRESH) {
			speed = normalize(speed, MAX);
			//			} else {
			//				speed = normalize(speed, REDUCED);
			//			}
			speed = deadBand(speed);

			if (state != 0)
				wrist.set(speed);
			SmartDashboard.putNumber("Speed of wrist", speed);

			Timer.delay(0.005);
		}
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
	public void ingest(double speed) {
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
		speed*=-1;
		intake.set(speed);
	}

	/**
	 * Puts the intake in travel position
	 */
	public void stow() {
		state = STOW;
	}

	/**
	 * Extends the intake to prepare for injesting a cube
	 */
	public void extend() {
		state = EXTEND;
	}

	public void score() {
		state = SCORE;
	}

	/**
	 * Moves the intake into travel position
	 * 
	 * @param speed
	 *            Speed at which the motor spins
	 */
	public void manualStow(double speed) {
		if (state != 0 && Math.abs(speed) < DEADBAND)
			return;

		killAutoMovement();
		wrist.set(speed);
	}

	/**
	 * Moves the intake to prepare to ingest a cube
	 * 
	 * @param speed
	 *            Speed at which the motor spins
	 */
	public void manualExtend(double speed) {
		if (state != 0 && Math.abs(speed) < DEADBAND)
			return;

		killAutoMovement();
		wrist.set(-speed);
	}

	private void killAutoMovement() {
		state = 0;
	}

	/**
	 * Is the intake subsystem automatically doing something
	 * 
	 * @return True if it is, False otherwise
	 */
	public boolean inAction() {
		return inThread;
	}
}
