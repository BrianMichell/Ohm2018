package org.usfirst.frc.team1817.robot;

import edu.wpi.first.wpilibj.XboxController;

public class RaiderJoystick extends XboxController {

	private final double DEADZONE = 0.1;

	public RaiderJoystick(int channel) {
		super(channel);
	}

	public double getLeftX() {
		return deadzone(super.getX(Hand.kLeft));
	}

	public double getLeftY() {
		return deadzone(super.getY(Hand.kLeft));
	}

	public double getRightX() {
		return deadzone(super.getX(Hand.kRight));
	}

	public double getRightY() {
		return deadzone(super.getX(Hand.kRight));
	}

	public double getLeftXAbs() {
		return Math.abs(getLeftX());
	}

	public double getLeftYAbs() {
		return Math.abs(getLeftY());
	}

	public double getRightXAbs() {
		return Math.abs(getRightX());
	}

	public double getRightYAbs() {
		return Math.abs(getRightY());
	}

	public boolean getLeftXMoved() {
		return getLeftXAbs() > 0.0;
	}

	public boolean getLeftYMoved() {
		return getLeftYAbs() > 0.0;
	}

	public boolean getRightXMoved() {
		return getRightXAbs() > 0.0;
	}

	public boolean getRightYMoved() {
		return getRightYAbs() > 0.0;
	}

	public double getLeftTrigger() {
		return super.getTriggerAxis(Hand.kLeft);
	}

	public double getRightTrigger() {
		return super.getTriggerAxis(Hand.kRight);
	}

	public boolean getLeftTriggerPressed() {
		return getLeftTrigger() > 0;
	}

	public boolean getRightTriggerPressed() {
		return getRightTrigger() > 0;
	}

	public boolean getDUp() {
		return super.getPOV() == 0;
	}

	public boolean getDDown() {
		return super.getPOV() == 180;
	}

	public boolean getDLeft() {
		return super.getPOV() == 270;
	}

	public boolean getDRight() {
		return super.getPOV() == 90;
	}

	public boolean getLeftBumper() {
		return super.getBumper(Hand.kLeft);
	}

	public boolean getRightBumper() {
		return super.getBumper(Hand.kRight);
	}

	private double deadzone(double value) {
		return Math.abs(value) >= DEADZONE ? value : 0.0;
	}
}
