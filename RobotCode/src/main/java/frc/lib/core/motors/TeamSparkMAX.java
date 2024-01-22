package frc.lib.core.motors;

import frc.lib.core.PidParameters;
import frc.lib.core.util.TeamUtils;

import com.revrobotics.CANSparkMax;

import com.revrobotics.RelativeEncoder;

import com.revrobotics.REVLibError;
import com.revrobotics.SparkPIDController;

/**
 * Used for NEOs. Provides support for PID control and an encoder.
 */
public class TeamSparkMAX extends CANSparkMax
{

  private static final double TELEMETRY_UPDATE_INTERVAL_SECS = 0.0;
  private double lastTelemetryUpdate = 0;

  private final String SmartDashboardPrefix;

  private double maxSpeed = Double.MAX_VALUE;

  private double smartMotionLoopTarget;

  private final SparkPIDController CanPidController;

  private final RelativeEncoder CanEncoder;

  private final PidParameters PidProfiles[];

  private CANSparkMax.ControlType ctrlType;

  public TeamSparkMAX(String smartDashboardPrefix, int deviceID)
  {
    super(deviceID, MotorType.kBrushless); // Neos are brushless

    this.SmartDashboardPrefix = smartDashboardPrefix;

    PidProfiles = new PidParameters[4];
    CanPidController = getPIDController();
    CanEncoder = getEncoder();

    enableVoltageCompensation(12.0);
  }

  public String getSmartDashboardPrefix()
  {
    return SmartDashboardPrefix;
  }

  @Override
  public SparkPIDController getPIDController()
  {
    return CanPidController;
  }

  private static boolean isPidControlMode(CANSparkMax.ControlType mode)
  {

    // kDutyCycle, kVelocity, kVoltage, kPosition, kSmartMotion, kCurrent, kSmartVelocity

    // Are all possible values. If one of these are not part of PID, add case for them and return
    // false.
    return mode != CANSparkMax.ControlType.kCurrent;
  }

  public double getCurrentEncoderValue()
  {
    // This should be configurable
    return CanEncoder.getPosition();
  }

  public void resetEncoder()
  {
    CanEncoder.setPosition(0.0);
  }

  public boolean isRunningPidControlMode()
  {
    // Dunno if this is safe, but its the easiest way to get around
    // problems with the PidParameters.
    return isPidControlMode(ctrlType);
  }

  public void periodic()
  {
    double now = TeamUtils.getCurrentTime();

    if ((now - lastTelemetryUpdate) < TELEMETRY_UPDATE_INTERVAL_SECS)
    {
      return;
    }

    lastTelemetryUpdate = now;

    double currentSpeed = CanEncoder.getVelocity();

    if (maxSpeed == Double.MAX_VALUE || currentSpeed > maxSpeed)
      maxSpeed = currentSpeed;
  }

  public double getClosedLoopTarget()
  {
    return this.smartMotionLoopTarget;
  }

  public double setClosedLoopTarget(double value)
  {
    this.smartMotionLoopTarget = value;
    return this.smartMotionLoopTarget;
  }

  public REVLibError setSmartMotionVelocity(double speed, String reason)
  {
    setClosedLoopTarget(speed);
    ctrlType = CANSparkMax.ControlType.kSmartVelocity;
    REVLibError errors = this.CanPidController.setReference(Math.abs(speed),
        CANSparkMax.ControlType.kSmartVelocity);
    return errors;
  }

  public double getVelocityError()
  {
    double currentSpeed = CanEncoder.getVelocity();
    return getClosedLoopTarget() - currentSpeed;
  }

  public void configureWithPidParameters(PidParameters pidParameters, int pidSlotIndex)
  {
    PidProfiles[pidSlotIndex] = pidParameters;

    CanPidController.setFF(pidParameters.kF, pidSlotIndex); // Feed-forward
    CanPidController.setP(pidParameters.kP, pidSlotIndex);
    CanPidController.setI(pidParameters.kI, pidSlotIndex);
    CanPidController.setD(pidParameters.kD, pidSlotIndex);
    CanPidController.setOutputRange(-pidParameters.kPeakOutput, pidParameters.kPeakOutput);

    CanPidController.setSmartMotionMaxVelocity(pidParameters.maxVel, pidSlotIndex);
    CanPidController.setSmartMotionMinOutputVelocity(0, pidSlotIndex);
    CanPidController.setSmartMotionMaxAccel(pidParameters.maxAcc, pidSlotIndex);
    CanPidController.setSmartMotionAllowedClosedLoopError(pidParameters.errorTolerance,
        pidSlotIndex);
  }

  /**
   * @param power  Between -1 and 1
   * @param reason Unused for now
   * @see #set(double)
   */
  public void set(double power, String reason)
  {
    super.set(power);
  }
}