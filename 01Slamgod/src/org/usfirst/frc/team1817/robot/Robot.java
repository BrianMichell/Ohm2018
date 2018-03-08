/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

//Talking about soy nuts:
//"I bet if you heat these up enough they turn into popcorn"

//"I don't know how horses work..."
package org.usfirst.frc.team1817.robot;

//Programming pit punch-list
//TODO Update PWM and DIO values to reflect real ports (TOP PRIORITY)
//TODO Remove/Tune PID controllers on the arm and hand (TOP PRIORITY)
//TODO Get actual diameter of wheels (HIGH PRIORITY)
//TODO Clear faults from CAN system (LOW-MID PRIORITY)
//TODO Re-enable data display to dashboard (LOW-MID PRIORITY)
//TODO Implement automatic downshifting when the bot is stopped (LOW PRIORITY)

//Electrical pit punch-list
//TODO Run PWM and DIO wires (TOP PRIORITY)
//TODO Orient servo shifters (TOP PRIORITY)
//TODO Rewire CAN (LOW PRIORITY)

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {

	// AUTON CHOOSERS
	private SendableChooser<String> station = new SendableChooser<>();
	private final String MIDDLE_STATION = "Middle";
	private final String LEFT_STATION = "Left";
	private final String RIGHT_STATION = "Right";
	private SendableChooser<String> auto = new SendableChooser<>();
	private final String AUTO_CROSS = "Cross";
	private final String AUTO_CUBE = "One Cube";
	private final String AUTO_2_CUBE_AND_EXCHG = "2 Cubes and Exchange";
	// CONSTANTS
	private final double DISTANCE_PER_PULSE = Math.PI * 6.25 / 250.0; //TODO update diameter
	private final double LIMIT = 0.08; // A limited change in the joystick input per iteration.
	@SuppressWarnings("unused")
	private final float BOAT = 8047; // IT'S ALWAYS A WATER GAME!!!

	// ROBOT OBJECTS
	public SpeedControllerGroup leftShark;
	public SpeedControllerGroup right;
	public DifferentialDrive chassis;
	private Servo backShifter;
	private Servo frontShifter;
	private RaiderJoystick driver;
	private RaiderJoystick manipulator;
	// ROBOT PARTS
	private Arm arm;
	private Hand hand;
	private Auton a;
	// SENSORS
	private PowerDistributionPanel pdp;
	public Encoder encoderL;
	public Encoder encoderR;
	public ADXRS450_Gyro gyro;

	DriverStation ds;
	// GLOBAL VARIABLES
	private boolean lowGear = true;

	private double limitedForward = 0.0;
	private double limitedTurn = 0.0;

	@Override
	public void robotInit() {
		// SmartDashboard buttons
		station.addDefault("Middle", MIDDLE_STATION);
		station.addObject("Left", LEFT_STATION);
		station.addObject("Right", RIGHT_STATION);
		auto.addDefault("1 Cube", AUTO_CUBE);
		auto.addObject("Line Cross", AUTO_CROSS);
		auto.addObject("2 Cube+Exchange", AUTO_2_CUBE_AND_EXCHG);
		SmartDashboard.putData("Aliance Station", station);
		SmartDashboard.putData("Auton", auto);
		// Left side motor controllers
		//TODO Update PWM values
		leftShark = new SpeedControllerGroup(new VictorSP(1), new VictorSP(2), new VictorSP(0));
		leftShark.setInverted(true);
		// Right side motor controllers
		//TODO Update PWM values
		right = new SpeedControllerGroup(new VictorSP(4), new VictorSP(5), new VictorSP(3));
		right.setInverted(true);
		// Drive train
		chassis = new DifferentialDrive(leftShark, right);
		chassis.setDeadband(0.2);
		// Shifters
		//TODO Update PWM value
		frontShifter = new Servo(8);
		//TODO Update PWM value
		backShifter = new Servo(9);
		// Left encoder
		//TODO Update DIO value
		encoderL = new Encoder(0, 1);
		encoderL.setDistancePerPulse(DISTANCE_PER_PULSE);
		encoderL.setReverseDirection(true);
		// Right encoder
		//TODO Update DIO value
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
		a = new Auton(gyro, encoderL, encoderR, chassis, arm, hand);
		//TODO Update PWM and DIO values
		arm = new Arm(new VictorSP(12), new VictorSP(13), new Encoder(5, 6), new Encoder(7, 8));
		//TODO Update PWM and DIO values
		hand = new Hand(new VictorSP(14), new VictorSP(15), new Encoder(9, 10));

		//Thread to handle shifting
		new Thread(() -> {
			boolean pressed;
			while (true) {
				pressed = false;
				while (driver.getRightBumper()) {
					pressed = true;
				}
				if (pressed) {
					updateGear();
				} 
			}
		}).start();
	}

	@Override
	public void autonomousInit() {
		//Set bot to low gear
		lowGear=true;
		shift();
		char switchLocation = ds.getGameSpecificMessage().charAt(0);
		char scaleLocation = ds.getGameSpecificMessage().charAt(1);
		char botLocation;
		switch (station.getSelected()) {
		case MIDDLE_STATION:
			botLocation = 'm';
			break;
		case LEFT_STATION:
			botLocation = 'l';
			break;
		default:
			botLocation = 'r';
			break;
		}

		Thread autoExecutor;

		switch (auto.getSelected()) {
		case AUTO_CUBE:
			autoExecutor = new Thread(() -> {
				a.placeOneCube(switchLocation, botLocation);
			});
			break;
		case AUTO_CROSS:
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
		//TODO Make sure driver can use these controls
			//TODO Simplify controls to limit button presses
		if (driver.getDUp())
			arm.extend();
		if (driver.getDDown())
			arm.retract();
		if (driver.getDRight())
			hand.extend();
		if (driver.getDLeft())
			hand.stow();
		if (driver.getLeftTriggerPressed())
			hand.injest(driver.getLeftTrigger());
		if (driver.getRightTriggerPressed())
			hand.expel(driver.getRightTrigger());
		if (driver.getYButton())
			arm.setAngle(20);
		if (driver.getBButton())
			arm.setAngle(90);
			
		// Manual functions
		//TODO Make sure manipulator can use these controls
		if (manipulator.getLeftYMoved())
			arm.manualExtend(manipulator.getLeftY());
		if (manipulator.getRightXMoved())
			arm.manualAngle(manipulator.getRightX());
		if (manipulator.getLeftTriggerPressed())
			hand.manualStow(manipulator.getLeftTrigger());
		if (manipulator.getRightTriggerPressed())
			hand.manualExtend(manipulator.getRightTrigger());
			
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

		chassis.arcadeDrive(limitedForward, limitedTurn);
		displayData();
	}

	@Override
	public void testPeriodic() {
	}

	private void updateGear() {
		lowGear = !lowGear;
		shift();
	}

	//TODO Reimplement this if deemed needed
	private void updateGearAuto() {
		if (encoderL.getRate() == 0 && encoderR.getRate() == 0) {
			lowGear = true;
			shift();
		}
	}

	private void shift() {
		if(lowGear) {
			frontShifter.setAngle(0);
			backShifter.setAngle(100);
		} else {
			frontShifter.setAngle(110);
			backShifter.setAngle(0);
		}
	}

	private void displayData() {
		SmartDashboard.putNumber("Limited Forward", limitedForward);
		SmartDashboard.putNumber("Limited Turn", limitedTurn);
		SmartDashboard.putNumber("Limit", LIMIT);
		SmartDashboard.putNumber("Left Speed", encoderL.getRate() / 12.0);
		SmartDashboard.putNumber("Right Speed", encoderR.getRate() / 12.0);
		SmartDashboard.putNumber("Current Amperage", pdp.getTotalCurrent());
		SmartDashboard.putNumber("Current Energy", pdp.getTotalEnergy());
		SmartDashboard.putNumber("Current Power", pdp.getTotalPower());
		SmartDashboard.putNumber("Current Voltage", pdp.getVoltage());
		SmartDashboard.putNumber("Encoder Left", encoderL.get());
		SmartDashboard.putNumber("Encoder Right", encoderR.get());
	}

}
