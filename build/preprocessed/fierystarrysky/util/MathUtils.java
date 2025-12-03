/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.util;

/**
 *
 * @author Raven
 */
public class MathUtils {

    public static float atan2Deg(float y, float x) {
        if (x == 0 && y == 0) {
            return 0;
        }
        float angle;
        if (Math.abs(x) > Math.abs(y)) {
            float z = y / x;
            angle = (float) Math.toDegrees(z - z * z * z / 3 + z * z * z * z * z / 5); // 三阶泰勒展开
            if (x < 0) {
                angle += 180;
            }
        } else {
            float z = x / y;
            angle = (float) Math.toDegrees((float) Math.PI / 2 - (z - z * z * z / 3 + z * z * z * z * z / 5));
            if (y < 0) {
                angle += 180;
            }
        }
        angle = (angle + 360) % 360;
        return angle;
    }

    // 补充 signum 方法
    public static float signum(float value) {
        if (value > 0) {
            return 1f;
        }
        if (value < 0) {
            return -1f;
        }
        return 0f;
    }

    public static float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle > 180) {
            angle -= 360;
        }
        if (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    public static float shortestAngleDifference(float angle1, float angle2) {
        float diff = (angle2 - angle1) % 360;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        return diff;
    }
}
