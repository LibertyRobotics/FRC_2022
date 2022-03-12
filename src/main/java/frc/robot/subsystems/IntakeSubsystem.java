package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.SoftLimitDirection;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.hardware.ClosedLoopSparkMax;

public class IntakeSubsystem extends SubsystemBase {

  // motor controllers
  private final CANSparkMax liftLeft =
      new CANSparkMax(Constants.IntakeConstants.kLiftLeftPort, MotorType.kBrushless);
  private final CANSparkMax liftRight =
      new CANSparkMax(Constants.IntakeConstants.kLiftRightPort, MotorType.kBrushless);

  private final ClosedLoopSparkMax intakeRoller =
      new ClosedLoopSparkMax(Constants.IntakeConstants.kRollerPort, MotorType.kBrushless);
  private final ClosedLoopSparkMax indexEntryRoller =
      new ClosedLoopSparkMax(
          Constants.IntakeConstants.kIndexerLowerBottomBeltPort, MotorType.kBrushless);

  // Encoders
  private final RelativeEncoder m_liftLeftEncoder = liftLeft.getEncoder();
  private final RelativeEncoder m_liftRightEncoder = liftRight.getEncoder();
  private final RelativeEncoder m_indexEntryEncoder = indexEntryRoller.getEncoder();

  private SparkMaxPIDController m_rightcontroller = liftRight.getPIDController();

  // whether the subsystem is successfully homed to its max point
  private boolean homingComplete = false;

  // The encoder positions for the lowest and highest allowed
  private double m_lowestAllowedPosition;
  private double m_highestAllowedPosition;

  private boolean rollerRunning = false;

  // motor geared 125:1 -> 24:72 gearing
  public IntakeSubsystem() {

    // TODO: set position conversion factor
    double factor = 1;

    // I value needs to be nonzero in order for closed loop PID to work
    intakeRoller.kI(000001);

    // upper/inner roller follows the outside
    indexEntryRoller.follow(intakeRoller);

    liftLeft.setIdleMode(CANSparkMax.IdleMode.kCoast);
    liftRight.setIdleMode(CANSparkMax.IdleMode.kCoast);

    // set the left side to follow the right side, invert=false
    liftLeft.follow(liftRight, true);
    disableLiftSoftLimits();

    // set the position conversion factors for the lift encoders
    m_liftLeftEncoder.setPositionConversionFactor(factor);
    m_liftRightEncoder.setPositionConversionFactor(factor);

    setHome(0, 90);
  }

  /** set the intake mechanism to run at the target RPM */
  public void setRPM(double rpm) {
    final double kGearRatio = 1 / 12;
    intakeRoller.setVelocity(rpm * kGearRatio);
  }

  public void runRollerAtMaxPower(boolean invert) {
    if (invert) {
      intakeRoller.set(-0.75);
    } else {
      intakeRoller.set(0.75);
    }
  }

  public void toggleRoller() {
    if (rollerRunning) {
      intakeRoller.set(0);
      rollerRunning = false;
    } else {
      runRollerAtMaxPower(false);
      rollerRunning = true;
    }
  }

  /** sets the range of motion for the intake lift. */
  public void setHome(double min, double max) {
    if (homingComplete) {
      System.out.println("Intake being re-homed!");
    }

    // Enable forward and reverse soft limits for the lift motors
    // enableLiftSoftLimits();

    // Set the soft limits for the lift motors
    // setLiftSoftLimits((float) max, (float) min);

    m_lowestAllowedPosition = min;
    m_highestAllowedPosition = max;

    homingComplete = true;
  }

  public void lowerIntake() {
    m_rightcontroller.setReference(m_highestAllowedPosition, CANSparkMax.ControlType.kPosition);
  }

  public void raiseIntake() {
    m_rightcontroller.setReference(m_lowestAllowedPosition, CANSparkMax.ControlType.kPosition);
  }

  public void enableLiftSoftLimits() {
    liftLeft.enableSoftLimit(SoftLimitDirection.kForward, true);
    liftRight.enableSoftLimit(SoftLimitDirection.kForward, true);

    liftLeft.enableSoftLimit(SoftLimitDirection.kReverse, true);
    liftRight.enableSoftLimit(SoftLimitDirection.kReverse, true);
  }

  public void disableLiftSoftLimits() {
    liftLeft.enableSoftLimit(SoftLimitDirection.kForward, false);
    liftRight.enableSoftLimit(SoftLimitDirection.kForward, false);

    liftLeft.enableSoftLimit(SoftLimitDirection.kReverse, false);
    liftRight.enableSoftLimit(SoftLimitDirection.kReverse, false);
  }

  public void setLiftSoftLimits(float max, float min) {
    liftLeft.setSoftLimit(SoftLimitDirection.kForward, max);
    liftRight.setSoftLimit(SoftLimitDirection.kForward, max);

    liftLeft.setSoftLimit(SoftLimitDirection.kReverse, min);
    liftRight.setSoftLimit(SoftLimitDirection.kReverse, min);
  }

  public boolean isAtHardLimit() {
    SmartDashboard.putNumber("Lift Current Draw", liftLeft.getOutputCurrent());
    return (liftLeft.getOutputCurrent() > Constants.IntakeConstants.kMaxExpectedCurrent ||
       liftRight.getOutputCurrent() > Constants.IntakeConstants.kMaxExpectedCurrent);
  }

  public void setLiftPower(double power) {
    liftRight.set(power);
  }

  /** gets the encoder position of the left lift */
  public double getLiftPosition() {
    return liftRight.getEncoder().getPosition();
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Lift", liftLeft.getOutputCurrent());
    SmartDashboard.putNumber("Lift Position", m_liftLeftEncoder.getPosition());
  }
}
