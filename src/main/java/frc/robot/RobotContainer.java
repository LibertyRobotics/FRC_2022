// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static frc.robot.Constants.ControllerConstants.*;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.util.net.PortForwarder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.PneumaticsControlModule;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController.Button;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.autonomous.FiveBallAuto;
import frc.robot.commands.autonomous.TaxiThenShoot;
import frc.robot.commands.autonomous.TwoBallAuto;
import frc.robot.commands.climber.ClimbTimed;
import frc.robot.commands.drive.AlignWithHub;
import frc.robot.commands.drive.DefaultDrive;
import frc.robot.commands.indexer.RunUpperIndexer;
import frc.robot.commands.intake.FullyLiftIntake;
import frc.robot.commands.intake.LowerIntake;
import frc.robot.commands.intake.RunIntakeRoller;
import frc.robot.commands.shooter.RampThenShoot;
import frc.robot.commands.utilities.enums.CargoDirection;
import frc.robot.hardware.Limelight;
import frc.robot.hardware.Limelight.LEDMode;
import frc.robot.subsystems.ClimberSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.IndexerSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final Limelight m_limelight = new Limelight();

  private final DriveSubsystem m_robotDrive = new DriveSubsystem();
  private final ShooterSubsystem m_shooterSubsystem = new ShooterSubsystem(m_limelight);
  private final IntakeSubsystem m_intakeSubsystem = new IntakeSubsystem();
  private final IndexerSubsystem m_indexerSubsystem = new IndexerSubsystem();
  private final ClimberSubsystem m_climberSubsystem = new ClimberSubsystem();

  private final XboxController m_driverController = new XboxController(kDriverPort);
  private final XboxController m_secondaryController = new XboxController(kSecondaryPort);

  // PDP and PCM
  // FIXME: Initializing the PDP this way leads to repeated CAN errors for some reason
  // private final PowerDistribution m_pdp = new PowerDistribution();
  private final PneumaticsControlModule m_pcm = new PneumaticsControlModule();

  private boolean m_compressorEnabled = true;

  // Automodes - if you add more here, add them to the chooser in the container
  private TwoBallAuto m_twoBall =
      new TwoBallAuto(
          m_robotDrive, m_shooterSubsystem, m_indexerSubsystem, m_intakeSubsystem, m_limelight);
  private FiveBallAuto m_fiveBall =
      new FiveBallAuto(
          m_robotDrive, m_shooterSubsystem, m_indexerSubsystem, m_intakeSubsystem, m_limelight);
  private TaxiThenShoot m_taxiThenShoot =
      new TaxiThenShoot(
          m_robotDrive, m_intakeSubsystem, m_indexerSubsystem, m_shooterSubsystem, m_limelight);

  // Sets up driver controlled auto choices
  SendableChooser<Command> m_chooser = new SendableChooser<>();

  boolean m_babyMode = false;

  /** The container for the robot. Contains subsystems, IO devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();

    // Turn on the limelight's LEDs
    m_limelight.setLEDMode(LEDMode.On);

    // Set up USB (rear-facing) camera
    UsbCamera camera = CameraServer.startAutomaticCapture();
    camera.setResolution(320, 240);

    // Forward limelight ports over USB
    PortForwarder.add(5800, "10.64.43.11", 5800);
    PortForwarder.add(5801, "10.64.43.11", 5801);
    PortForwarder.add(5805, "10.64.43.11", 5805);

    // Set up autonomous chooser
    // IMPORTANT: Add Automodes here, don't override the chooser
    m_chooser.setDefaultOption("Taxi & Shoot", m_taxiThenShoot);
    // m_chooser.addOption("Two Ball Auto", m_twoBall);
    // m_chooser.addOption("Five Ball Auto*", m_fiveBall);

    SmartDashboard.putData(m_chooser);

    // Set default drivetrain command to arcade driving (happens during teleop)
    m_robotDrive.setDefaultCommand(
        new DefaultDrive(
            m_robotDrive, m_driverController::getLeftY, m_driverController::getRightX));
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    // PRIMARY CONTROLLER
    // Homing to Hub - A Button
    new JoystickButton(m_driverController, Button.kA.value)
        .whenPressed(new AlignWithHub(m_limelight, m_robotDrive).withTimeout(1.5));

    // Climb sequence - Start Button
    new JoystickButton(m_driverController, Button.kStart.value)
        .whenPressed(
            new ClimbTimed(
                m_climberSubsystem, m_intakeSubsystem, m_driverController::getStartButtonPressed));

    // SECONDARY CONTROLLER

    // Ramps up the shooter then runs the upper indexer into it
    // TODO: the limelight needs to be turned on before checking if it has a valid target
    new JoystickButton(m_secondaryController, Button.kB.value)
        .whileHeld(
            new RampThenShoot(
                m_intakeSubsystem,
                m_indexerSubsystem,
                m_shooterSubsystem,
                m_limelight,
                m_driverController));

    // Operate the intake lift
    new JoystickButton(m_secondaryController, Button.kRightBumper.value)
        .whenPressed(new FullyLiftIntake(m_intakeSubsystem));

    new JoystickButton(m_secondaryController, Button.kLeftBumper.value)
        .whileHeld(new LowerIntake(m_intakeSubsystem));

    // Manual modification of the shooter RPM during a match
    Trigger dpadUp = new Trigger(() -> m_secondaryController.getPOV() == 0);
    Trigger dpadDown = new Trigger(() -> m_secondaryController.getPOV() == 180);

    dpadUp.whenActive(m_shooterSubsystem::incrementRPMOffset, m_shooterSubsystem);
    dpadDown.whenActive(m_shooterSubsystem::decrementRPMOffset, m_shooterSubsystem);

    // Run the intake roller & lower indexer belts (?)
    // TODO: Also lower the intake before doing this?
    new JoystickButton(m_secondaryController, Button.kA.value)
        .whileHeld(new RunIntakeRoller(m_intakeSubsystem, CargoDirection.Intake));

    // Eject any cargo in the indexer/intake
    new JoystickButton(m_secondaryController, Button.kX.value)
        .whileHeld(
            new ParallelCommandGroup(
                new RunIntakeRoller(m_intakeSubsystem, CargoDirection.Eject),
                new RunUpperIndexer(m_indexerSubsystem, CargoDirection.Eject)));
  }

  public void homeIntake() {

    // if (!m_intakeSubsystem.getHomingComplete()) {
    //   new HomeIntakeCommand(m_intakeSubsystem).schedule(false);
    // }
  }

  /** Used to toggle the compressor using the dashboard. */
  public void updateCompressorStatus() {
    m_compressorEnabled = SmartDashboard.getBoolean("Enable Compressor", m_compressorEnabled);
    if (m_compressorEnabled) {
      m_pcm.enableCompressorDigital();
    } else {
      m_pcm.disableCompressor();
    }

    // Put the toggle onto the dashboard
    SmartDashboard.putBoolean("Enable Compressor", m_compressorEnabled);
  }

  /** Clears all sticky faults on the PCM and PDP. */
  public void clearAllStickyFaults() {
    // TODO: Update the PDP firmware?
    // m_pdp.clearStickyFaults();
    // m_pcm.clearAllStickyFaults();
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // Returns the autonomous command selected on the dashboard
    return m_chooser.getSelected();
  }
}
