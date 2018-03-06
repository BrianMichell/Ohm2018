/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

//Talking about soy nuts:
//"I bet if you heat these up enough they turn into popcorn"

//"I don't know how hourses work..."
package org.usfirst.frc.team1817.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {

	// AUTON CHOOSERS
	private SendableChooser<String> station = new SendableChooser<>();
	private final String stationMiddle = "Middle";
	private final String stationLeft = "Left";
	private final String stationRight = "Right";
	private SendableChooser<String> auto = new SendableChooser<>();
	private final String autoCross = "Cross";
	private final String autoCube = "One Cube";
	private final String auto2CubeAndExchange = "2 Cubes and Exchange";
	// CONSTANTS
	private final double DISTANCE_PER_PULSE = Math.PI * 6.25 / 250.0;
	private double LIMIT = 0.07; // A limited change in the joystick input per iteration.
	@SuppressWarnings("unused")
	private final float BOAT = 8047; // IT'S ALWAYS A WATER GAME!!!

	// ROBOT OBJECTS
	public SpeedControllerGroup leftShark;
	public SpeedControllerGroup right;
	public DifferentialDrive chassis;
	private DoubleSolenoid leftShift;
	private DoubleSolenoid rightShift;
	private RaiderJoystick driver;
	private RaiderJoystick manipulator;
	// ROBOT PARTS
	private Lift lift;
	private Intake intake;
	private Auton a;
	// SENSORS
	private PowerDistributionPanel pdp;
	public Encoder encoderL;
	public Encoder encoderR;
	public ADXRS450_Gyro gyro;

	// GLOBAL VARIABLES
	private boolean lowGear = true;
	private boolean quadratic = true;

	private double limitedForward = 0.0;
	private double limitedTurn = 0.0;

	DriverStation ds;

	@Override
	public void robotInit() {
		// SmartDashboard buttons
		station.addDefault("Middle", stationMiddle);
		station.addObject("Left", stationLeft);
		station.addObject("Right", stationRight);
		auto.addDefault("1 Cube", autoCube);
		auto.addObject("Line Cross", autoCross);
		auto.addObject("2 Cube+Exchange", auto2CubeAndExchange);
		SmartDashboard.putData("Aliance Station", station);
		SmartDashboard.putData("Auton", auto);
		// Left side motor controllers
		leftShark = new SpeedControllerGroup(new Talon(1), new Talon(2), new Talon(0));
		leftShark.setInverted(true);
		// Right side motor controllers
		right = new SpeedControllerGroup(new Talon(4), new Talon(5), new Talon(3));
		right.setInverted(true);
		// Drive train
		chassis = new DifferentialDrive(leftShark, right);
		chassis.setDeadband(0.2);
		// Shifters
		leftShift = new DoubleSolenoid(0, 7);
		rightShift = new DoubleSolenoid(1, 6);
		// Left encoder
		encoderL = new Encoder(0, 1);
		encoderL.setDistancePerPulse(DISTANCE_PER_PULSE);
		encoderL.setReverseDirection(true);
		// Right encoder
		encoderR = new Encoder(2, 3);
		encoderR.setDistancePerPulse(DISTANCE_PER_PULSE);
		// Driver joystick
		driver = new RaiderJoystick(0);
		manipulator = new RaiderJoystick(1);
		// Other random sensing devices
		pdp = new PowerDistributionPanel();
		gyro = new ADXRS450_Gyro();
		gyro.calibrate();
		// Robot parts decelerations
		a = new Auton(gyro, encoderL, encoderR, chassis, lift, intake);
		lift = new Lift(new Victor(12), new Victor(13), new Encoder(5, 6), new Encoder(7, 8));
		intake = new Intake(new Victor(14), new Victor(15), new Encoder(9, 10));

		new Thread(() -> {
			boolean pressed;
			boolean change;
			boolean quad;
			while (true) {
				pressed = false;
				change = false;
				quad = false;
				while (driver.getRightBumper()) {
					pressed = true;
				}
				if (pressed) {
					updateGearM();
				}
				while (driver.getLeftBumper()) {
					change = true;
				}
				if (change) {
					if (LIMIT == 1.0) {
						LIMIT = 0.1;
					} else if (LIMIT == 0.1) {
						LIMIT = 0.09;
					} else if (LIMIT == 0.09) {
						LIMIT = 0.08;
					} else if (LIMIT == 0.08) {
						LIMIT = 0.07;
					} else if (LIMIT == 0.07) {
						LIMIT = 0.06;
					} else if (LIMIT == 0.06) {
						LIMIT = 0.05;
					} else if (LIMIT == 0.05) {
						LIMIT = 0.04;
					} else {
						LIMIT = 1.0;
					}
				}
				while (driver.getAButton()) {
					quad = true;
				}
				if (quad) {
					quadratic = !quadratic;
				}
			}
		}).start();
	}

	@Override
	public void autonomousInit() {
		// char switchLocation = ds.getGameSpecificMessage().charAt(0);
		// char scaleLocation = ds.getGameSpecificMessage().charAt(1);
		char botLocation;
		switch (station.getSelected()) {
		case stationMiddle:
			botLocation = 'm';
			break;
		case stationLeft:
			botLocation = 'l';
			break;
		default:
			botLocation = 'r';
			break;
		}
		char switchLocation = 'l';
		
		Thread autoExecutor;
		
		switch (auto.getSelected()) {
		case autoCube:
			autoExecutor = new Thread(() -> {
				a.placeOneCube(switchLocation, botLocation);
			});
			break;
		case autoCross:
			autoExecutor = new Thread(() -> {
				a.lineCross();
			});
			break;
		default:
			autoExecutor = new Thread(() -> {
				a.twoCubeAndExchange(switchLocation, botLocation);
			});
		}
		autoExecutor.start();
	}

	@Override
	public void autonomousPeriodic() {
	}

	@Override
	public void teleopInit() {
		a.kill();
	}

	@Override
	public void teleopPeriodic() {
		// Autonomous driver functions
		if (driver.getDUp())
			lift.extend();
		if (driver.getDDown())
			lift.retract();
		if (driver.getDRight())
			intake.extend();
		if (driver.getDLeft())
			intake.stow();
		if (driver.getLeftTriggerPressed())
			intake.injest(driver.getLeftTrigger());
		if (driver.getRightTriggerPressed())
			intake.expel(driver.getRightTrigger());
		if (driver.getYButton())
			lift.setAngle(20);
		if (driver.getBButton())
			lift.setAngle(90);
		// Manual functions
		if (manipulator.getLeftYMoved())
			lift.manualExtend(manipulator.getLeftY());

		if (manipulator.getRightXMoved())
			lift.manualAngle(manipulator.getRightX());

		if (manipulator.getLeftTriggerPressed())
			intake.manualStow(manipulator.getLeftTrigger());
		if (manipulator.getRightTriggerPressed())
			intake.manualExtend(manipulator.getRightTrigger());

		// Handle driving
		double forward = driver.getLeftY();
		double turn = driver.getRightX();

		// Limited forward/backward
		double changeForward = forward - limitedForward;
		if (changeForward > LIMIT) {
			changeForward = LIMIT;
		} else if (changeForward < -LIMIT) {
			changeForward = -LIMIT;
		}
		limitedForward += changeForward;

		// Limited turning
		double changeTurn = turn - limitedTurn;
		if (changeTurn > LIMIT) {
			changeTurn = LIMIT;
		} else if (changeTurn < -LIMIT) {
			changeTurn = -LIMIT;
		}
		limitedTurn += changeTurn;

		chassis.arcadeDrive(calculateForward(limitedForward), limitedTurn);
		// Reset sensors
		if (driver.getRightBumper()) {
			encoderL.reset();
			encoderR.reset();
			gyro.reset();
		}
		displayData();
	}

	@Override
	public void testPeriodic() {
	}

	private void updateGearM() {
		lowGear = !lowGear;
		shift();
	}

	@SuppressWarnings("unused")
	private void updateGearA() {
		if (encoderL.getRate() == 0 && encoderR.getRate() == 0) {
			lowGear = true;
			shift();
		}
	}

	@SuppressWarnings("unused")
	private void driveStraight(double speed) {

		double r = gyro.getRate() * 0.005;
		if (speed == 0)
			return;
		if (r < 0) {
			chassis.tankDrive(speed, speed - r);
		} else {
			chassis.tankDrive(speed - r, speed);
		}

		/*
		 * double r = encoderR.getDistance() / 12.0; double l = encoderL.getDistance() /
		 * 12.0;
		 * 
		 * double delta = r - l; if (l < r) { chassis.tankDrive(speed, speed - delta); }
		 * else { chassis.tankDrive(speed + delta, speed); }
		 */
	}

	private double calculateForward(double input) {
		if (quadratic) { // Quadratic scaled driving
			if (input == 0) {
				return 0;
			} else if (input > 0) {
				return (Math.pow(5, limitedForward - 1));
			} else {
				return (Math.pow(5, (limitedForward * -1) - 1)) * -1;
			}
		}
		return input;
	}

	private void shift() {
		if (lowGear) {
			leftShift.set(DoubleSolenoid.Value.kForward);
			rightShift.set(DoubleSolenoid.Value.kForward);
		} else {
			leftShift.set(DoubleSolenoid.Value.kReverse);
			rightShift.set(DoubleSolenoid.Value.kReverse);
		}
	}

	private void displayData() {
		SmartDashboard.putNumber("Limited Forward", limitedForward);
		SmartDashboard.putNumber("Limited Turn", limitedTurn);
		SmartDashboard.putNumber("Limit", LIMIT);
		SmartDashboard.putNumber("Speed", encoderL.getRate() / 12.0);
		SmartDashboard.putNumber("Current Amperage", pdp.getTotalCurrent());
		SmartDashboard.putNumber("Current Energy", pdp.getTotalEnergy());
		SmartDashboard.putNumber("Current Power", pdp.getTotalPower());
		SmartDashboard.putNumber("Current Voltage", pdp.getVoltage());
		SmartDashboard.putNumber("Encoder Left", encoderL.get());
		SmartDashboard.putNumber("Encoder Right", encoderR.get());

		SmartDashboard.putBoolean("Quadratic ramping mode", quadratic);
	}

}
