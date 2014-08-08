package com.maoguangming.bluetoothdemo;

/**
 * Created by garnel on 14-8-8.
 */
public class SensorData {
    private double temperature;
    private double pressure;
    private double windFront;
    private double windSide;
    private double accelerationX;
    private double accelerationY;
    private double accelerationZ;

    public SensorData() {
        this(0, 0, 0, 0, 0, 0, 0);
    }

    public SensorData(double temperature, double pressure, double windFront, double windSide,
                      double accelerationX, double accelerationY, double accelerationZ) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.windFront = windFront;
        this.windSide = windSide;
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getWindFront() {
        return windFront;
    }

    public void setWindFront(double windFront) {
        this.windFront = windFront;
    }

    public double getWindSide() {
        return windSide;
    }

    public void setWindSide(double windSide) {
        this.windSide = windSide;
    }

    public double getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(double accelerationX) {
        this.accelerationX = accelerationX;
    }

    public double getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(double accelerationY) {
        this.accelerationY = accelerationY;
    }

    public double getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(double accelerationZ) {
        this.accelerationZ = accelerationZ;
    }
}
