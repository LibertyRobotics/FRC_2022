// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.util.Units;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static final class DriveConstants {
    // Wheels are 8 inches in diameter, so their circumference can be calculated
    // (converted to meters for convenience)
    public static final double kWheelCircumferenceMeters = Units.inchesToMeters(Math.PI * 7.736);
    public static final double kMotorRotationsPerWheelRotation = 7.56 * 2.8;

    public static final double nominalVoltage = 12.0;

    // Motor controller ports (on 2019 bot)
    public static final int kLeftFront = 8;
    public static final int kLeftCenter = 7;
    public static final int kLeftBack = 6;

    public static final int kRightFront = 1;
    public static final int kRightCenter = 2;
    public static final int kRightBack = 3;

    // Split PID-related constants based on whether robot is turning/going straight
    public static final class StraightPID {
      public static final class SmartMotion {
        public static final double kP = 0;
        public static final double kI = 0;
        public static final double kD = 0;
        public static final double kIz = 0;

        public static final double kFF = 0.000457;
        public static final double kMaxOutput = 1;
        public static final double kMinOutput = -1;
        public static final double kMaxRPM = 5676;
        public static final double kMinVel = 0;
        public static final double kMaxVel = 2000; // rpm
        public static final double kMaxAcc = 1500;
        public static final double kAllowedErr = 0.05;
      }

      // Basic PID constants
      // public static final double kP = 0;
      // public static final double kI = 0;
      // public static final double kD = 0;

      // Profiling
      public static final double kMaxVelocityMetersPerSecond = .33333;
      public static final double kMaxAccelerationMeterPerSecondSquared = 5;

      // Feedforward
      public static final double kTestPower = 0.05;
      public static final double kTestSeconds = 3;
      public static final double kSVolts =
          0.00; // Power!! for now               82 inches -> 2.08 meters
      // .4445 meters in 3 seconds at 0.05 power
      public static final double kVVoltMetersPerSecond =
          ((kTestPower - kSVolts) / (.4445 / kTestSeconds))
              / 1.55; // REMOVE ME. FIGURE OUT WHY IT IS OFF

      // PID tolerances
      public static final double kDriveVelocityToleranceMetersPerSecond = 2;
      public static final double kDriveToleranceMeters = 0.1;
    }

    public static final class TurnPID {
      public static final double kP = 0.006;
      public static final double kI = 0;
      public static final double kD = 0;

      // Profiling
      public static final double kMaxVelocityDegreesPerSecond = 360 / 5;
      public static final double kMaxAccelerationDegreesPerSecondSquared = 120;

      // Feedforward
      private static final double kSecondsPerRotation = .85; // works for dead-er battery
      private static final double kTestPower = 0.4;

      public static final double kSVolts = 0.045;
      public static final double kVVoltDegreesPerSecond =
          (kTestPower - kSVolts) / (356.494 / kSecondsPerRotation);

      public static final double kTurnToleranceDeg = 3.0;
      public static final double kTurnRateToleranceDegPerS = 20.0;
    }
  }
}
/* feedforward feedback gains
// These are example values only - DO NOT USE THESE FOR YOUR OWN ROBOT!
    // These characterization values MUST be determined either experimentally or theoretically
    // for *your* robot's drive.
    // The Robot Characterization Toolsuite provides a convenient tool for obtaining these
    // values for your robot.
    public static final double ksVolts = 0.16741;
    public static final double kvVoltSecondsPerMeter = .12549;
    public static final double kaVoltSecondsSquaredPerMeter = 0.20605;

    // Example value only - as above, this must be tuned for your drive!
    public static final double kPDriveVel = .16767;
//differential drive kinematics
public static final double kTrackwidthMeters = 8.3588;
    public static final DifferentialDriveKinematics kDriveKinematics =
        new DifferentialDriveKinematics(kTrackwidthMeters);

//max trajectory vel/accel
    public static final double kMaxSpeedMetersPerSecond = 3;
    public static final double kMaxAccelerationMetersPerSecondSquared = 3;

//ramsete patterns.
    // Reasonable baseline values for a RAMSETE follower in units of meters and seconds
    public static final double kRamseteB = 2;
    public static final double kRamseteZeta = 0.7;
*/
