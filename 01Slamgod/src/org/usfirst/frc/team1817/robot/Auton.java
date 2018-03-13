package org.usfirst.frc.team1817.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/*
 * Left and right auto modes should be set up with the far corner slightly 
 * farther than the edge of the switch
 * Middle should be about centered on the middle driver station
 */
public class Auton {

	private final int ROBOT_LENGTH = 38;
	private final int DISTANCE_TO_SWITCH_FRONT = 144 - ROBOT_LENGTH;
	private final int DISTANCE_TO_SWITCH_MID = 170 - ROBOT_LENGTH;
	private final int DISTANCE_TO_SWITCH_END = 196 - ROBOT_LENGTH;
	private final int SWITCH_LENGTH = 154;
	private final int LEFT_TURN = -90;
	private final int RIGHT_TURN = 90;
	private final int HYPOTONUSE = 200;
	private final double ENCODER_PRECISION = 1.0;
	private final double GYRO_PRECISION = 0.5;

	private DifferentialDrive chassis;
	private Hand intake;

	private PIDController withGyro;
	private PIDController withEncoder;

	private Encoder encoderL;
	private Encoder encoderR;

	private ADXRS450_Gyro gyro;

	private Jaguar dummyMotor = new Jaguar(11);

	private boolean killswitch = false;

	public Auton(ADXRS450_Gyro gyro, Encoder encoderL, Encoder encoderR, DifferentialDrive chassis, Hand intake) {
		dummyMotor.setInverted(true);
		withGyro = new PIDController(0.08, 0.0165, 0.0845, gyro, dummyMotor);
		withEncoder = new PIDController(0.08, 0.0185, 0.3, encoderL, dummyMotor);
		this.encoderL = encoderL;
		this.encoderR = encoderR;
		this.gyro = gyro;
		this.chassis = chassis;
		this.intake = intake;
	}

	public void lineCross() {
		killswitch = false;
		driveTo(140);

	}

	/**
	 * Places one cube on our switch
	 * 
	 * @param ourSwitch
	 * @param botSide
	 */
	public void placeOneCube(char ourSwitch, char botSide) {
		killswitch = false;
		if (ourSwitch == 'l') {
			if (botSide == 'l')
				placeLeftFromLeft();
			else if (botSide == 'm')
				placeLeftFromMid();
			else
				placeLeftFromRight();
		} else {
			if (botSide == 'l')
				placeRightFromLeft();
			else if (botSide == 'm')
				placeRightFromMid();
			else
				placeRightFromRight();
		}
	}

	/**
	 * Place a cube on the left side of the switch with the bot coming from the left
	 */
	private void placeLeftFromLeft() {
		killswitch = false;
		driveTo(DISTANCE_TO_SWITCH_MID);
		turnTo(RIGHT_TURN);
		chassis.arcadeDrive(0.5, 0);
		Timer.delay(0.5);
		//Shoot box out face opposing wall again
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);

	}

	/**
	 * Place a cube on the left side of the switch with the bot coming from the
	 * middle
	 */
	private void placeLeftFromMid() {
		killswitch = false;
		turnTo(LEFT_TURN + 45);
		driveTo(HYPOTONUSE);
		turnTo(45);
		//shoot cube
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);

	}

	/**
	 * Place a cube on the left side of the switch with the bot coming from the
	 * right
	 */
	private void placeLeftFromRight() {
		killswitch = false;
		driveTo(DISTANCE_TO_SWITCH_END + 12);
		turnTo(LEFT_TURN);
		driveTo(SWITCH_LENGTH - 14);
		turnTo(LEFT_TURN);
		chassis.arcadeDrive(0.5, 0);
		Timer.delay(0.5);
		//shoot cube
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);
	}

	/**
	 * Place a cube on the right side of the switch with the bot coming from the
	 * left
	 */
	private void placeRightFromLeft() {
		killswitch = false;
		driveTo(DISTANCE_TO_SWITCH_END + 12);
		turnTo(RIGHT_TURN);
		driveTo(SWITCH_LENGTH - 14);
		turnTo(RIGHT_TURN);
		chassis.arcadeDrive(0.5, 0);
		Timer.delay(0.5);
		// shoot cube
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);
	}

	/**
	 * Place a cube on the right side of the switch with the bot coming from the
	 * right
	 */
	private void placeRightFromMid() {
		killswitch = false;
		turnTo(45);
		driveTo(HYPOTONUSE);
		turnTo(-45);
		chassis.arcadeDrive(0.5, 0);
		Timer.delay(0.5);
		//Shoot cube
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);
	}

	/**
	 * Place a cube on the right side of the switch with the bot coming from the
	 * right
	 */
	private void placeRightFromRight() {
		killswitch = false;
		driveTo(DISTANCE_TO_SWITCH_MID);
		turnTo(LEFT_TURN);
		chassis.arcadeDrive(0.5, 0);
		Timer.delay(0.5);
		// Shoot box out 
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);
	}

	/**
	 * Place one cube on our side of the scale
	 * 
	 * @param ourScale
	 *            Which side our scale is on
	 * @param botSide
	 *            Where our bot is on the field
	 */
	public void oneCubeScale(char ourScale, char botSide) {

	}

	/**
	 * Place 2 cubes on the switch and then prep to go to the exchange
	 * 
	 * @param ourSwitch
	 *            Which side our switch is on
	 * @param botSide
	 *            Where our bot is on the field
	 */
	public void twoCubeAndExchange(char ourSwitch, char botSide) {

		switch (ourSwitch) {
		case 'l':
			switch (botSide) {
			case 'l':
				placeLeftFromLeft();
				break;
			case 'm':
				placeLeftFromMid();
				break;
			case 'r':
				placeLeftFromRight();
				break;
			}
			getCubeFromLeft();
			placeLeftFromPile();
			getCubeFromLeft();
			toExchangeFromLeft();
			break;
		case 'r':
			switch (botSide) {
			case 'l':
				placeRightFromLeft();
				break;
			case 'm':
				placeRightFromMid();
				break;
			case 'r':
				placeRightFromRight();
				break;
			}
			getCubeFromRight();
			placeRightFromPile();
			getCubeFromRight();
			toExchangeFromRight();
			break;
		}

	}

	/**
	 * Grabs a cube while the bot is at the left side of the switch
	 */
	private void getCubeFromLeft() {
		turnTo(RIGHT_TURN);
		driveTo(SWITCH_LENGTH / 2 - 12);
		while (intake.inAction() && !killswitch) {
			// Since it's still not "extended" spin the wheels opposite the way the code wants to
			intake.ingest(-1);
		}
		intake.ingest(1);
		Timer.delay(0.5);
		intake.ingest(0);
	}

	/**
	 * Grabs a cube while the bot is at the right side of the switch
	 */
	private void getCubeFromRight() {
		turnTo(LEFT_TURN);
		driveTo(SWITCH_LENGTH / 2 - 12);
		intake.extend();
		while (intake.inAction()) {
			// Since it's still not "extended" spin the wheels opposite the way the code wants to
			intake.ingest(-1);
		}
		intake.ingest(1);
		Timer.delay(0.5);
		intake.ingest(0);
	}

	/**
	 * Heads to the exchange from the left side of the cube pile
	 */
	private void toExchangeFromLeft() {
		turnTo(RIGHT_TURN);
		driveTo(DISTANCE_TO_SWITCH_FRONT - 6);
	}

	/**
	 * Heads to the exchange from the right side of the cube pile
	 */
	private void toExchangeFromRight() {
		turnTo(LEFT_TURN - 10);
		driveTo(DISTANCE_TO_SWITCH_FRONT);
	}

	/**
	 * Places a cube on the left side of the switch from the left side of the cube
	 * pile
	 */
	private void placeLeftFromPile() {
		turnTo(RIGHT_TURN + RIGHT_TURN);
		driveTo(SWITCH_LENGTH / 2 - 12);
		turnTo(RIGHT_TURN);
		// shoot cube
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);
	}

	/**
	 * Places a cube on the right side of the switch from the right side of the cube
	 * pile
	 */
	private void placeRightFromPile() {
		turnTo(LEFT_TURN + LEFT_TURN);
		driveTo(SWITCH_LENGTH / 2 - 12);
		turnTo(LEFT_TURN);
		// shoot cube
		intake.expel(1);
		Timer.delay(1);
		intake.expel(0);
	}

	private boolean encoderThresh() {

		if (killswitch)
			return true;
		return withEncoder.onTarget() && encoderL.getRate() == 0 && encoderR.getRate() == 0;
	}

	private void executeEncoderDrive() {
		while (encoderThresh() && !killswitch) {
			driveStraight();
		}
		withEncoder.disable();
	}

	private boolean gyroThresh() {
		if (killswitch)
			return true;
		return Math.abs(gyro.getRate()) < 0.001 && withGyro.onTarget();
	}

	private void executeGyroTurn() {
		while (!gyroThresh() && !killswitch) {
			chassis.arcadeDrive(0, withGyro.get());
		}
		withGyro.disable();
	}

	private void turnTo(double degrees) {
		gyro.reset();

		withGyro.setOutputRange(-0.6, 0.6);
		withGyro.setAbsoluteTolerance(GYRO_PRECISION);
		withGyro.setPercentTolerance(GYRO_PRECISION);
		withGyro.setSetpoint(degrees);

		withGyro.enable();

		executeGyroTurn();
	}

	private void driveTo(double inches) {
		resetEncoders();

		withEncoder.setOutputRange(-0.75, 0.75);
		withEncoder.setAbsoluteTolerance(ENCODER_PRECISION);
		withEncoder.setSetpoint(inches);

		withEncoder.enable();

		executeEncoderDrive();
	}

	private void resetEncoders() {
		encoderL.reset();
		encoderR.reset();
	}

	private void driveStraight() {
		double speed = withEncoder.get();
		chassis.tankDrive(speed, -1*speed);
		/*
		double r = encoderR.getDistance() / 12.0;
		double l = encoderL.getDistance() / 12.0;

		double delta = r - l;
		if (l < r) {
			chassis.tankDrive(speed, (speed - delta)*-1);
		} else {
			chassis.tankDrive(speed + delta, -1*speed);
		}
		*/
	}

	public void printStatus() {
		System.out.println(withGyro.isEnabled());
		System.out.println(withEncoder.isEnabled());
	}

	public void kill() {
		killswitch = true;
		withGyro.disable();
		withEncoder.disable();
	}

}
