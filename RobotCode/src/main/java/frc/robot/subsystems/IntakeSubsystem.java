package frc.robot.subsystems;

import frc.lib.core.util.TeamCountdown;
import frc.lib.core.util.TeamMotorUtil;
import frc.robot.RobotContainer;
import frc.robot.constants.Constants;
import frc.robot.constants.IntakeConstants;
import frc.robot.constants.Ports;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkBase.FaultID;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.CANSparkLowLevel.PeriodicFrame;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeSubsystem extends SubsystemBase
{

	private CANSparkMax intakeDeployMotorLeft, intakeDeployMotorRight, intakeRollerMotor;
	private double desiredRotation, desiredVelocity;
	private SparkPIDController intakeDeployPidController, intakeRollerPidController;
	private RelativeEncoder intakeDeployEncoderRight;

	private TeamCountdown countdown;
	private boolean down;

	private double intakeRetractedRotation;

	public IntakeSubsystem()
	{
		intakeDeployMotorRight = new CANSparkMax(Ports.INTAKE_DEPLOY_MOTOR_RIGHT,
				MotorType.kBrushless);
		intakeDeployMotorLeft = new CANSparkMax(Ports.INTAKE_DEPLOY_MOTOR_LEFT,
				MotorType.kBrushless);
		intakeRollerMotor = new CANSparkMax(Ports.INTAKE_ROLLER_MOTOR, MotorType.kBrushless);

		intakeDeployEncoderRight = intakeDeployMotorRight.getEncoder();

		// Configure deployment motors
		intakeDeployMotorLeft.follow(intakeDeployMotorRight, true);
		intakeDeployMotorRight.setSmartCurrentLimit(Constants.NEO_MAX_CURRENT);
		intakeDeployMotorLeft.setSmartCurrentLimit(Constants.NEO_MAX_CURRENT);
		intakeDeployMotorRight.setIdleMode(IdleMode.kBrake);
		intakeDeployMotorLeft.setIdleMode(IdleMode.kBrake);

		// Configure deployment PID, slot 0 is for upwards movement, slot 1 downwards
		intakeDeployPidController = intakeDeployMotorRight.getPIDController();
		intakeDeployPidController.setP(IntakeConstants.INTAKE_DEPLOYMENT_UP_KP, 0);
		intakeDeployPidController.setI(IntakeConstants.INTAKE_DEPLOYMENT_UP_KI, 0);
		intakeDeployPidController.setD(IntakeConstants.INTAKE_DEPLOYMENT_UP_KD, 0);
		intakeDeployPidController.setFF(IntakeConstants.INTAKE_DEPLOYMENT_UP_KFF, 0);

		intakeDeployPidController.setP(IntakeConstants.INTAKE_DEPLOYMENT_DOWN_KP, 1);
		intakeDeployPidController.setI(IntakeConstants.INTAKE_DEPLOYMENT_DOWN_KI, 1);
		intakeDeployPidController.setD(IntakeConstants.INTAKE_DEPLOYMENT_DOWN_KD, 1);
		intakeDeployPidController.setFF(IntakeConstants.INTAKE_DEPLOYMENT_DOWN_KFF, 1);
		

		// Configure roller motors
		intakeRollerMotor.setInverted(true);
		intakeRollerMotor.setSmartCurrentLimit(30);
		intakeRollerMotor.setIdleMode(IdleMode.kBrake);

		// Configure roller PID
		intakeRollerPidController = intakeRollerMotor.getPIDController();
		intakeRollerPidController.setP(IntakeConstants.INTAKE_ROLLER_KP, 0);
		intakeRollerPidController.setI(IntakeConstants.INTAKE_ROLLER_KI, 0);
		intakeRollerPidController.setD(IntakeConstants.INTAKE_ROLLER_KD, 0);
		intakeRollerPidController.setFF(IntakeConstants.INTAKE_ROLLER_KFF, 0);

		desiredRotation = IntakeConstants.INTAKE_RETRACTED_ROTATION;
		desiredVelocity = IntakeConstants.INTAKE_REST_VELOCITY;

		RobotContainer.getShuffleboardTab().addDouble("Actual Intake Velocity",
				() -> intakeRollerMotor.getEncoder().getVelocity());
		RobotContainer.getShuffleboardTab().addDouble("Desired Intake Velocity",
				() -> desiredVelocity);
		RobotContainer.getShuffleboardTab().addDouble("Actual Intake Rotation",
				() -> intakeDeployEncoderRight.getPosition());
		RobotContainer.getShuffleboardTab().addDouble("Desired Intake Rotation",
				() -> intakeRetractedRotation);

		intakeRetractedRotation = intakeDeployEncoderRight.getPosition();
		intakeDeployPidController.setReference(intakeRetractedRotation, ControlType.kPosition, 0);
	
		down = false;
	}

	@Override
	public void periodic()
	{
		if (intakeDeployMotorLeft.getStickyFault(FaultID.kHasReset)
				|| intakeDeployMotorRight.getStickyFault(FaultID.kHasReset)
				|| intakeRollerMotor.getStickyFault(FaultID.kHasReset))
		{
			TeamMotorUtil.optimizeCANSparkBusUsage(intakeRollerMotor);
			intakeRollerMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 20);
			intakeRollerMotor.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 20);

			TeamMotorUtil.optimizeCANSparkBusUsage(intakeDeployMotorLeft);
			intakeDeployMotorLeft.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 20);
			intakeDeployMotorLeft.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 20);

			TeamMotorUtil.optimizeCANSparkBusUsage(intakeDeployMotorRight);
			intakeDeployMotorRight.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 20);
			intakeDeployMotorRight.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 20);
		}

		if (countdown != null && (!down && countdown.isDone()))
		{
			intakeDeployMotorRight.set(0);
			countdown = null;
		}
	}

	/** @param desiredRotation Ticks */
	public void setDesiredRotation(boolean deployed, int deploymentPIDSlot)
	{
		if (deployed)
		{
			intakeDeployMotorRight.set(0);
			countdown = new TeamCountdown(1000);
		}
		else
		{
			intakeDeployMotorRight.set(0);
			countdown = new TeamCountdown(1000);
		}
	}

	/** @param desiredVelocity Ticks per second */
	public void setDesiredVelocity(double desiredVelocity)
	{
		this.desiredVelocity = desiredVelocity;
		intakeRollerPidController.setReference(desiredVelocity, ControlType.kVelocity, 0);
		// if (desiredVelocity == 0 /*|| intakeDeployEncoderRight.getPosition() < IntakeConstants.INTAKE_SPIN_ROTATION*/);
		// {
		// 	intakeRollerMotor.set(0);
		// }
	}

}
