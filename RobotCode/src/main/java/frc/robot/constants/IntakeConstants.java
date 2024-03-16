package frc.robot.constants;

public class IntakeConstants {
    /** Ticks per second */
    public static final double INTAKE_REST_VELOCITY = 0;
    /** Ticks per second */
    public static final double INTAKE_DEPLOYED_VELOCITY = 10000;

    public static final double INTAKE_REVERSE_VELOCITY = -5000;

    /** Ticks */
    public static final double INTAKE_RETRACTED_ROTATION = -9;
    /** Ticks */
    public static final double INTAKE_DEPLOYED_ROTATION = 3.5;

    public static final double INTAKE_DEPLOYMENT_KP = 0; // 0.5;
    public static final double INTAKE_DEPLOYMENT_KI = 0;
    public static final double INTAKE_DEPLOYMENT_KD = 0;
    public static final double INTAKE_DEPLOYMENT_KFF = 0;

    public static final double INTAKE_ROLLER_KP = 0;
    public static final double INTAKE_ROLLER_KI = 0;
    public static final double INTAKE_ROLLER_KD = 0;
    public static final double INTAKE_ROLLER_KFF = 0.05; 
}
