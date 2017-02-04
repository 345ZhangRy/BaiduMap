package com.pdwy.baidumap;

import java.util.HashMap;
import java.util.Map;

/**
 * @说明:通过经纬度计算距离的工具类
 * @作者:zry
 * @时间:2016/9/6
 */
public class GeoHasher {
    private static String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static String convertToString(char[] a) {
        String s = "";
        for (int i = 0; i < a.length; i++) {
            s += a[i];
        }
        return s;
    }

    public static double GetDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s;
    }

    public String encode_geohash(double latitude, double longitude,
                                 int precision) {
        char[] geohash = new char[precision + 1];
        boolean is_even = true;
        int i = 0;
        double[] lat = new double[2];
        double[] lon = new double[2];
        double mid;
        char bits[] = { 16, 8, 4, 2, 1 };
        int bit = 0, ch = 0;
        lat[0] = -90.0;
        lat[1] = 90.0;
        lon[0] = -180.0;
        lon[1] = 180.0;
        while (i < precision) {
            if (is_even) {
                mid = (lon[0] + lon[1]) / 2;
                if (longitude > mid) {
                    ch |= bits[bit];
                    lon[0] = mid;
                } else
                    lon[1] = mid;
            } else {
                mid = (lat[0] + lat[1]) / 2;
                if (latitude > mid) {
                    ch |= bits[bit];
                    lat[0] = mid;
                } else
                    lat[1] = mid;
            }
            is_even = !is_even;
            if (bit < 4)
                bit++;
            else {
                geohash[i++] = BASE32.charAt(ch);
                bit = 0;
                ch = 0;
            }
        }
        geohash[i] = 0;
        String s = "";
        for (i = 0; i < geohash.length; i++)
            s += geohash[i];
        return s;
    }

    public String[] expand(String geoStr) {
        String eastNeighbour = getEastNeighbour(geoStr);
        String westNeighbour = getWestNeighbour(geoStr);
        String northNeighbour = getNorthNeibour(geoStr);
        String southNeighbour = getSouthNeibour(geoStr);
        String[] expandGeoStr = { geoStr, eastNeighbour, westNeighbour,
                northNeighbour, southNeighbour, getNorthNeibour(westNeighbour),
                getNorthNeibour(eastNeighbour), getSouthNeibour(westNeighbour),
                getSouthNeibour(eastNeighbour) };
        return expandGeoStr;
    }

    public String getEastNeighbour(String geoStr) {
        Map<String, Object> map = extractLonLatFromGeoStr(geoStr);
        long lon = (Long) map.get("lon") + 1;
        return getGeoStrFrom(lon, (String) map.get("latBitStr"), true);
    }

    public String getWestNeighbour(String geoStr) {
        Map<String, Object> map = extractLonLatFromGeoStr(geoStr);
        long lon = (Long) map.get("lon") - 1;
        return getGeoStrFrom(lon, (String) map.get("latBitStr"), true);
    }

    public String getNorthNeibour(String geoStr) {
        Map<String, Object> map = extractLonLatFromGeoStr(geoStr);
        long lat = (Long) map.get("lat") + 1;
        return getGeoStrFrom(lat, (String) map.get("lonBitStr"), false);
    }

    public String getSouthNeibour(String geoStr) {
        Map<String, Object> map = extractLonLatFromGeoStr(geoStr);
        long lat = (Long) map.get("lat") - 1;
        return getGeoStrFrom(lat, (String) map.get("lonBitStr"), false);
    }

    public Map<String, Object> extractLonLatFromGeoStr(String geoStr) {
        boolean is_even = true;
        char bits[] = { 16, 8, 4, 2, 1 };
        int bit = 0, ch = 0;
        int geoIdx;
        String lonBitStr = "";
        String latBitStr = "";
        long lon = 0;
        long lat = 0;
        for (int i = 0; i < geoStr.length(); i++) {
            geoIdx = BASE32.indexOf(geoStr.charAt(i));
            for (bit = 0; bit < 5; bit++) {
                ch = geoIdx & bits[bit];
                if (is_even) {
                    if (ch != 0) {
                        lonBitStr += "1";
                        lon = lon * 2 + 1;
                    } else {
                        lonBitStr += "0";
                        lon = lon * 2;
                    }
                } else {
                    if (ch != 0) {
                        latBitStr += "1";
                        lat = lat * 2 + 1;
                    } else {
                        latBitStr += "0";
                        lat = lat * 2;
                    }
                }
                is_even = !is_even;
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("lonBitStr", lonBitStr);
        map.put("latBitStr", latBitStr);
        map.put("lat", lat);
        map.put("lon", lon);
        return map;
    }

    public String getGeoStrFrom(long lonOrLat, String lonOrLatStr, boolean isLon) {
        String lonBitStr = "";
        String latBitStr = "";
        if (isLon) {
            lonBitStr = Long.toBinaryString(lonOrLat);
            latBitStr = lonOrLatStr;
        } else {
            latBitStr = Long.toBinaryString(lonOrLat);
            lonBitStr = lonOrLatStr;
        }
        boolean is_even = true;
        String geoStr = "";
        int ch, bit;
        int geoStrLength = (lonBitStr.length() + latBitStr.length()) / 5;
        for (int i = 0; i < lonBitStr.length();) {
            ch = 0;
            for (bit = 0; bit < 5; bit++) {
                if (is_even)
                    ch = ch * 2 + lonBitStr.charAt(i) - '0';
                else {
                    if (i < latBitStr.length())
                        ch = ch * 2 + latBitStr.charAt(i) - '0';
                    else
                        bit--;
                    i++;
                }
                is_even = !is_even;
            }
            geoStr += BASE32.charAt(ch);
            if (geoStr.length() == geoStrLength)
                return geoStr;
        }
        return geoStr;
    }
}
