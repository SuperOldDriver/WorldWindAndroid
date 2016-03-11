/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

public class BasicGlobe implements Globe {

    protected double equatorialRadius;

    protected double polarRadius;

    protected double eccentricitySquared;

    protected GeographicProjection projection;

    public BasicGlobe(double semiMajorAxis, double inverseFlattening, GeographicProjection projection) {
        if (semiMajorAxis <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "Semi-major axis is invalid"));
        }

        if (inverseFlattening <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "Inverse flattening is invalid"));
        }

        if (projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "missingProjection"));
        }

        double f = 1 / inverseFlattening;
        this.equatorialRadius = semiMajorAxis;
        this.polarRadius = semiMajorAxis * (1 - f);
        this.eccentricitySquared = 2 * f - f * f;
        this.projection = projection;
    }

    @Override
    public double getEquatorialRadius() {
        return equatorialRadius;
    }

    @Override
    public double getPolarRadius() {
        return polarRadius;
    }

    @Override
    public double getRadiusAt(double latitude, double longitude) {
        // The radius for an ellipsoidal globe is a function of its latitude. The following solution was derived by
        // observing that the length of the ellipsoidal point at the specified latitude and longitude indicates the
        // radius at that location. The formula for the length of the ellipsoidal point was then converted into the
        // simplified form below.

        double sinLat = Math.sin(Math.toRadians(latitude));
        double ec2 = this.eccentricitySquared;
        double rpm = this.equatorialRadius / Math.sqrt(1 - ec2 * sinLat * sinLat);
        return rpm * Math.sqrt(1 + (ec2 * ec2 - 2 * ec2) * sinLat * sinLat);
    }

    @Override
    public double getEccentricitySquared() {
        return eccentricitySquared;
    }

    @Override
    public GeographicProjection getProjection() {
        return projection;
    }

    @Override
    public void setProjection(GeographicProjection projection) {
        if (projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "setProjection", "missingProjection"));
        }

        this.projection = projection;
    }

    @Override
    public Vec3 geographicToCartesian(double latitude, double longitude, double altitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesian", "missingResult"));
        }

        return this.projection.geographicToCartesian(this, latitude, longitude, altitude, null, result);
    }

    @Override
    public Vec3 geographicToCartesianNormal(double latitude, double longitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianNormal", "missingResult"));
        }

        return this.projection.geographicToCartesianNormal(this, latitude, longitude, result);
    }

    @Override
    public Matrix4 geographicToCartesianTransform(double latitude, double longitude, double altitude, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianTransform", "missingResult"));
        }

        return this.projection.geographicToCartesianTransform(this, latitude, longitude, altitude, null, result);
    }

    @Override
    public FloatBuffer geographicToCartesianGrid(Sector sector, int numLat, int numLon, double[] elevations,
                                                 Vec3 referencePoint, FloatBuffer result, int stride) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "BasicGlobe",
                "geographicToCartesianGrid", "Number of latitude or longitude locations is less than one"));
        }

        int numPoints = numLat * numLon;
        if (elevations != null && elevations.length < numPoints) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "BasicGlobe",
                "geographicToCartesianGrid", "missingArray"));
        }

        if (result == null || result.remaining() < numPoints * stride) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingResult"));
        }

        return this.projection.geographicToCartesianGrid(this, sector, numLat, numLon, elevations,
            referencePoint, null, result, stride);
    }

    @Override
    public Position cartesianToGeographic(double x, double y, double z, Position result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cartesianToGeographic", "missingResult"));
        }

        return this.projection.cartesianToGeographic(this, x, y, z, null, result);
    }
}
