// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.JoystickLimits;
import frc.robot.Constants.SwerveConstants;

public class SwerveGroup extends SubsystemBase{
    //get all swerve modules
    SwerveUnit moduleFR1;
    SwerveUnit moduleFL2;
    SwerveUnit moduleBL3;
    SwerveUnit moduleBR4;

    AHRS navx;


    //Get gyro values
    
    static double gyroZero = 0;
    static double rawAngle;
    static boolean useField = false;

    public void zero() {
        gyroZero = navx.getAngle();
    }
    
    public static void switchToField() { //Methods for changing field or robot orientation 
        useField = true;
    }
    
    public static void switchToRobot() {
        useField = false;
    }
    
    public static void switchField() {
        useField = !useField;
    }

    public SwerveGroup(SwerveUnit moduleFR1, SwerveUnit moduleFL2, SwerveUnit moduleBL3, SwerveUnit moduleBR4, AHRS navx) {
        this.moduleFR1 = moduleFR1;
        this.moduleFL2 = moduleFL2;
        this.moduleBL3 = moduleBL3;
        this.moduleBR4 = moduleBR4;
        this.navx = navx;
    }

    public void Drive(double vx, double vy, double r) {
        //Getting the proper swerve values 


        Translation2d m_frontRight = new Translation2d(SwerveConstants.TrackwidthM/2,-SwerveConstants.WheelbaseM/2); //Making 2D translations from the center of the robot to the swerve modules
        Translation2d m_frontLeft = new Translation2d(SwerveConstants.TrackwidthM/2,SwerveConstants.WheelbaseM/2);
        Translation2d m_backLeft = new Translation2d(-SwerveConstants.TrackwidthM/2,SwerveConstants.WheelbaseM/2);
        Translation2d m_backRight = new Translation2d(-SwerveConstants.TrackwidthM/2,-SwerveConstants.WheelbaseM/2);

        SwerveDriveKinematics m_Kinematics = new SwerveDriveKinematics(m_frontLeft, m_frontRight, m_backLeft, m_backRight);

        ChassisSpeeds speeds;

        if(useField) {
            speeds = ChassisSpeeds.fromFieldRelativeSpeeds(vx, vy, r, Rotation2d.fromDegrees(rawAngle)); 
        } else {
            speeds = new ChassisSpeeds(vx, vy, r); 
        }

        SwerveModuleState[] moduleStates = m_Kinematics.toSwerveModuleStates(speeds);
        SwerveModuleState frontLeft = moduleStates[0];
        SwerveModuleState frontRight = moduleStates[1];
        SwerveModuleState backLeft = moduleStates[2];
        SwerveModuleState backRight = moduleStates[3];



        //normalize the speeds if they are over a maximumm set speed

        if(!underRobotLimits(moduleStates)) {
            moduleStates = normalizeVelocities(moduleStates);
        }

        moduleFL2.GetMotorValues(frontLeft.speedMetersPerSecond, frontLeft.angle.getDegrees());
        moduleFR1.GetMotorValues(frontRight.speedMetersPerSecond, frontRight.angle.getDegrees());
        moduleBL3.GetMotorValues(backLeft.speedMetersPerSecond, backLeft.angle.getDegrees());
        moduleBR4.GetMotorValues(backRight.speedMetersPerSecond, backRight.angle.getDegrees());
    }

    

    public static boolean underRobotLimits(SwerveModuleState[] array)
    {
        for(int i = 0; i <= array.length; i++) {
            if( array[i].speedMetersPerSecond <= JoystickLimits.VELOCITY_MAX_SPEED_MS) return false;
        }
        return true;
    }

    public static SwerveModuleState[] normalizeVelocities(SwerveModuleState[] array) { //limit the velocites to the maximum velocity 
        //get highest velocity in the group 
        double maximum = 0;
        for(int i = 0; i <= array.length; i++) {
            if(maximum < array[i].speedMetersPerSecond) maximum = array[i].speedMetersPerSecond;

        }

        for(int i = 0; i <= array.length; i++ ) {
            array[i].speedMetersPerSecond = (JoystickLimits.VELOCITY_MAX_SPEED_MS*(array[i].speedMetersPerSecond / maximum));
        }

        return array;
    }




  @Override
  public void periodic() {
    rawAngle = navx.getAngle() - gyroZero;
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
