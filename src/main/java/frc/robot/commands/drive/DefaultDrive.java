package frc.robot.commands.drive;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.DriveSubsystem;
import java.util.function.DoubleSupplier;

public class DefaultDrive extends CommandBase {
  private final DriveSubsystem m_drive;

  // Left & right stick inputs from main controller
  private final DoubleSupplier m_left;
  private final DoubleSupplier m_right;

  // Acceleration limiter for forward/rotational movement
  SlewRateLimiter m_forwardSlewLimiter = new SlewRateLimiter(2.0);
  SlewRateLimiter m_turningSlewLimiter = new SlewRateLimiter(2.0);

  // Drive at full speed for driver practice
  // TODO: Move to Constants.java?
  private double m_speedMultiplier = 1.0;
  private double m_rotationMultiplier = 0.6;

  public DefaultDrive(DriveSubsystem drive, DoubleSupplier left, DoubleSupplier right) {
    m_drive = drive;
    m_left = left;
    m_right = right;
    addRequirements(drive);
  }

  @Override
  public void execute() {
    // Calculate appropriate powers using above slew limiters
    double forwardPower = m_speedMultiplier * -m_left.getAsDouble();
    forwardPower = m_forwardSlewLimiter.calculate(forwardPower);

    double rotationPower = m_rotationMultiplier * -m_right.getAsDouble();
    rotationPower = m_turningSlewLimiter.calculate(rotationPower);

    m_drive.arcadeDrive(forwardPower, rotationPower, true);
  }
}
