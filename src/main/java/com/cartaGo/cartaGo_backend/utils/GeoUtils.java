package com.cartaGo.cartaGo_backend.utils;

/**
 * Utilidades geográficas básicas.
 * - haversineKm: distancia entre dos puntos (lat/lon) en kilómetros.
 * - deltaLatFor / deltaLonFor: tamaño aproximado de 1 "radio" en grados ( para bounding boxes).
 */
public final class GeoUtils {

    private GeoUtils() {}

    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Distancia Haversine en kilómetros entre (lat1,lon1) y (lat2,lon2). */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /** Aproximación: 1º de latitud ~ 111 km. */
    public static double deltaLatFor(double radiusKm) {
        return radiusKm / 111.0;
    }

    /** Aproximación: 1º de longitud ~ 111 km * cos(lat). */
    public static double deltaLonFor(double lat, double radiusKm) {
        return radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
    }
}
