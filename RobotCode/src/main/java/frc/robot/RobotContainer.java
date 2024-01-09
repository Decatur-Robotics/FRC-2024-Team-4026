package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;
import edu.wpi.first.wpilibj.shuffleboard.SuppliedValueWidget;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.lib.core.LogitechControllerButtons;
import frc.lib.modules.swervedrive.SwerveDriveSubsystem;
import frc.lib.modules.swervedrive.SwerveModule;
import frc.lib.modules.swervedrive.Commands.TeleopSwerveCommand;

public class RobotContainer
{

	private static RobotContainer instance;

	private final ShuffleboardTab shuffleboard;

	private final SendableChooser<Command> autoChooser;

	private final SwerveDriveSubsystem swerveDriveSubsystem;

	/** The container for the robot. Contains subsystems, OI devices, and commands. */
	public RobotContainer()
	{
		if(instance != null)
			System.err.println("WARNING: RobotContainer already instantiated!");
		instance = this;

		shuffleboard = Shuffleboard.getTab("Tab 1");

		// Init subsystems
		swerveDriveSubsystem = new SwerveDriveSubsystem();

		autoChooser = new SendableChooser<>();
		addAutonomousOptions();

		// Configure the button bindings
		configurePrimaryBindings();
		configureSecondaryBindings();
	}

	private void configurePrimaryBindings()
	{
		// Init buttons
		Joystick primaryController = new Joystick(0);

		JoystickButton a = new JoystickButton(primaryController,
			LogitechControllerButtons.a);
		JoystickButton b = new JoystickButton(primaryController,
			LogitechControllerButtons.b);
		JoystickButton x = new JoystickButton(primaryController,
			LogitechControllerButtons.x);
		JoystickButton y = new JoystickButton(primaryController,
			LogitechControllerButtons.y);
		JoystickButton bumperLeft = new JoystickButton(primaryController,
			LogitechControllerButtons.bumperLeft);
		JoystickButton bumperRight = new JoystickButton(primaryController,
			LogitechControllerButtons.bumperRight);
		JoystickButton triggerLeft = new JoystickButton(primaryController,
			LogitechControllerButtons.triggerLeft);
		JoystickButton triggerRight = new JoystickButton(primaryController,
			LogitechControllerButtons.triggerRight);

		// Init swerve
		swerveDriveSubsystem
			// vertical axis of left joystick -> translation
			.setDefaultCommand(new TeleopSwerveCommand(swerveDriveSubsystem,
				() -> -primaryController.getY(),
				() -> -primaryController.getX(), // horizontal axis of left joystick->  strafe
				// we want horizontal of right stick, if not twist, then change
				() -> primaryController.getTwist(),
				() -> triggerLeft.getAsBoolean(),
				() -> triggerRight.getAsBoolean()
			));

		x.onTrue(new InstantCommand(() -> {
			swerveDriveSubsystem.zeroGyro();
			swerveDriveSubsystem.setGyroOffset(0);
		}));
		y.onTrue(new InstantCommand(() -> {
			for (SwerveModule mod : swerveDriveSubsystem.getSwerveMods())
				mod.resetToAbsolute();
		}));

		a.onTrue(new InstantCommand(() -> {
			boolean invert = !SmartDashboard.getBoolean("Invert Serve", false);
			swerveDriveSubsystem.setAngleOffsets(invert);
			SmartDashboard.putBoolean("Invert Swerve", invert);
		}));
		b.onTrue(new InstantCommand(() -> {
			swerveDriveSubsystem.setGyro(180);
		}));
	}

	private void configureSecondaryBindings()
	{

	}

	// Add autonomous options to the SendableChooser
	public void addAutonomousOptions()
	{

	}

	public Command getAutonomousCommand()
	{
		return autoChooser.getSelected();
	}

	public static ShuffleboardTab getShuffleboard()
	{
		return instance.shuffleboard;
	}

}
