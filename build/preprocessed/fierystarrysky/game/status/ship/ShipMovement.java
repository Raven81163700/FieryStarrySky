/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.status.ship;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.model.ship.ShipModel;
import fierystarrysky.util.MathUtils;

/**
 *
 * @author Raven
 */
public class ShipMovement {

    public static final int AUTO_NONE = 0;
    public static final int AUTO_APPROACH = 1;
    public static final int AUTO_ORBIT = 2;
    private float x, y;         // 世界坐标
    private float vx, vy;       // 速度分量
    private float targetAngle;          // 目标朝向角度
    private float currentAngle; //当前朝向角度
    private float maxSpeed;        // 最大速度
    private float currentSpeed; // 当前速度大小
    private float acceleration; // 加速度
    private float brakingRatio;
    private float turnRate; //转向速度
    private int throttle;       // 节流阀（0~100）
    private float targetX, targetY;
    private int mode = AUTO_NONE;
    private float targetDistance = 0;
    private SpaceCanvas canvas;
    private boolean clockwise;

    public ShipMovement(float startX, float startY, SpaceCanvas canvas) {
        this.x = startX;
        this.y = startY;
        this.targetAngle = 0;
        this.currentAngle = 0;
        this.currentSpeed = 0;
        this.throttle = 0;
        this.canvas = canvas;
    }

    private void updateRotation(float deltaTime) {
        float angleDiff = MathUtils.normalizeAngle(targetAngle - currentAngle);

        float maxTurn = turnRate * deltaTime; // 当前帧能转的最大角度
        if (Math.abs(angleDiff) <= maxTurn) {
            currentAngle += angleDiff; // 一次性转到目标角度
        } else {
            currentAngle += MathUtils.signum(angleDiff) * maxTurn; // 按最大转向速度逼近
        }

        currentAngle = (currentAngle + 360) % 360; // 保持在 [0, 360)
    }

    public void update(float deltaTime) {
        if (mode == AUTO_APPROACH) {

            autoApproachLogic();
        }
        if (mode == AUTO_ORBIT) {
            autoOrbitLogic(deltaTime);
        }

        float targetSpeed = maxSpeed * throttle / 100f;

        // 加速/减速
        if (currentSpeed != targetSpeed && deltaTime > 0) {
            float deltaV = acceleration * deltaTime;
            float diff = targetSpeed - currentSpeed;

            if (Math.abs(diff) <= deltaV) {
                currentSpeed = targetSpeed;
            } else {
                float factor = (diff > 0) ? 1f : brakingRatio; // 加速或减速时的倍率
                currentSpeed += MathUtils.signum(diff) * deltaV * factor;
            }
        }

        updateRotation(deltaTime);

        // 速度分量
        vx = (float) Math.cos(Math.toRadians(currentAngle)) * currentSpeed;
        vy = (float) Math.sin(Math.toRadians(currentAngle)) * currentSpeed;

        // 更新位置
        x += vx * deltaTime;
        y += vy * deltaTime;
    }

    private void autoOrbitLogic(float deltaTime) {
        // 获取目标位置
        targetX = canvas.getShipStatus().getTargetObject().getX();
        targetY = canvas.getShipStatus().getTargetObject().getY();

        // 当前位置到目标向量
        float dx = x - targetX;
        float dy = y - targetY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.01f) {
            return; // 防止除零
        }
        // 计算当前位置对应圆周点
        float ux = dx / distance;
        float uy = dy / distance;
        float orbitX = targetX + ux * targetDistance;
        float orbitY = targetY + uy * targetDistance;

        // 计算切向方向
        float tx = clockwise ? -uy : uy;
        float ty = clockwise ? ux : -ux;

        // 预测下一帧圆周点
        float angularSpeed = maxSpeed / targetDistance; // rad/s
        float dTheta = angularSpeed * deltaTime;    // 当前帧角增量
        // 用单位切向向量旋转 dTheta，得到下一帧圆周点偏移
        float nextOrbitX = orbitX + (-uy * dTheta * targetDistance); // 简化线性近似
        float nextOrbitY = orbitY + (ux * dTheta * targetDistance);

        // 计算朝向下一帧圆周点的角度
        float targetDx = nextOrbitX - x;
        float targetDy = nextOrbitY - y;
        targetAngle = MathUtils.atan2Deg(targetDy, targetDx);

        // 角度保持在 [0,360)
        if (targetAngle < 0) {
            targetAngle += 360;
        }
    }

    public void startAutoOrbit(float targetX, float targetY, float targetDistance) {
        float diffAngle = MathUtils.shortestAngleDifference(currentAngle, targetAngle);
        clockwise = diffAngle > 0;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetDistance = targetDistance;
        this.throttle = 100;
        this.mode = AUTO_ORBIT;
    }

    private void autoApproachLogic() {
        targetX = canvas.getShipStatus().getTargetObject().getX();
        targetY = canvas.getShipStatus().getTargetObject().getY();

        float dx = targetX - x;
        float dy = targetY - y;
        float distanceToTarget = (float) Math.sqrt(dx * dx + dy * dy);

        // 计算与目标保持的距离差
        float distanceDiff = distanceToTarget - targetDistance;

        // 目标角度（向目标或远离目标）
        if (Math.abs(distanceDiff) < 0.01f) {
            // 已经在目标距离附近，不动
            throttle = 0;
            currentSpeed = 0;
            return;
        } else if (distanceDiff > 0) {
            // 需要接近目标
            targetAngle = MathUtils.atan2Deg(dy, dx);
        } else {
            // 需要远离目标
            targetAngle = MathUtils.atan2Deg(-dy, -dx); // 反方向
        }
        if (targetAngle < 0) {
            targetAngle += 360;
        }

        // 提前减速计算
        float stoppingDistance = (currentSpeed * currentSpeed) / (2 * acceleration * brakingRatio);

        // 节流控制：根据剩余距离差线性减速
        float absDiff = Math.abs(distanceDiff);
        if (absDiff <= 0.05f) {
            throttle = 0;
            currentSpeed = 0;
        } else if (absDiff < stoppingDistance) {
            throttle = (int) (absDiff / stoppingDistance * 100);
            throttle = Math.max(throttle, 10); // 保持最小推进
        } else {
            throttle = 100;
        }
    }

    // 启动自动接近
    public void startAutoApproach(float targetX, float targetY, float targetDistance) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetDistance = targetDistance;
        this.mode = AUTO_APPROACH;
    }

    //手动控制以取消自动导航
    private void stopAutoPilot(int action) {
        //0为转向 1为控制最大速度

        if (mode == AUTO_APPROACH) {
            //所有行为都会打断
            this.cancelAutoPilot();
        } else if (mode == AUTO_ORBIT) {
            //只有0会打断
            if (action == 0) {
                this.cancelAutoPilot();
            }
        }
    }

    // 取消自动接近，保持当前移动状态
    public void cancelAutoPilot() {
        this.mode = AUTO_NONE; // 定义一个手动模式常量，比如 MANUAL = 0
    }

    // 控制方法
    public void turnLeft() {
        stopAutoPilot(0);
        targetAngle = (targetAngle + 5) % 360;
    }

    public void turnRight() {
        stopAutoPilot(0);
        targetAngle = (targetAngle - 5 + 360) % 360;
    }

    public void throttleUp() {
        stopAutoPilot(1);
        if (throttle < 100) {
            throttle++;
        }
    }

    public void throttleDown() {
        stopAutoPilot(1);
        if (throttle > 0) {
            throttle--;
        }
    }

    // getter
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getTargetAngle() {
        return targetAngle;
    }

    public float getCurrentAngle() {
        return currentAngle;
    }

    public int getThrottle() {
        return throttle;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public float getMode() {
        return mode;
    }

    public float getTargetDistance() {
        return targetDistance;
    }

    public void loadShip(float acceleration, float brakingRatio, float turnRate, float maxSpeed) {
        this.acceleration = acceleration;
        this.brakingRatio = brakingRatio;
        this.turnRate = turnRate;
        this.maxSpeed = maxSpeed;
    }
}
