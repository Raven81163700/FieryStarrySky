package com.aurora.ui;

import java.io.IOException;

import javax.microedition.lcdui.Image;

public final class ImageTransformUtils {

    private ImageTransformUtils() {
    }

    public static Image loadResource(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.length() == 0) {
            throw new IllegalArgumentException("resourcePath is empty");
        }
        return Image.createImage(resourcePath);
    }

    public static Image scale(Image src, int targetWidth, int targetHeight) {
        if (src == null) {
            throw new IllegalArgumentException("src == null");
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("target size must be > 0");
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        if (srcWidth == targetWidth && srcHeight == targetHeight) {
            return src;
        }

        int[] srcRgb = new int[srcWidth * srcHeight];
        int[] dstRgb = new int[targetWidth * targetHeight];
        src.getRGB(srcRgb, 0, srcWidth, 0, 0, srcWidth, srcHeight);

        int x;
        int y;
        for (y = 0; y < targetHeight; y++) {
            int srcY = y * srcHeight / targetHeight;
            int srcRow = srcY * srcWidth;
            int dstRow = y * targetWidth;
            for (x = 0; x < targetWidth; x++) {
                int srcX = x * srcWidth / targetWidth;
                dstRgb[dstRow + x] = srcRgb[srcRow + srcX];
            }
        }

        return Image.createRGBImage(dstRgb, targetWidth, targetHeight, true);
    }

    public static Image rotate(Image src, int angleDegrees) {
        if (src == null) {
            throw new IllegalArgumentException("src == null");
        }

        int normalized = normalizeAngle(angleDegrees);
        if (normalized == 0) {
            return src;
        }
        if (normalized == 90 || normalized == 180 || normalized == 270) {
            return rotateRightAngle(src, normalized);
        }

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int[] srcRgb = new int[srcWidth * srcHeight];
        src.getRGB(srcRgb, 0, srcWidth, 0, 0, srcWidth, srcHeight);

        double radians = normalized * Math.PI / 180.0d;
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double srcCx = (srcWidth - 1) * 0.5d;
        double srcCy = (srcHeight - 1) * 0.5d;

        double[] bounds = computeRotatedBounds(srcWidth, srcHeight, sin, cos);
        double minX = bounds[0];
        double minY = bounds[1];
        double maxX = bounds[2];
        double maxY = bounds[3];

        int dstWidth = (int) Math.floor(maxX - minX + 1.0d + 1e-6d);
        int dstHeight = (int) Math.floor(maxY - minY + 1.0d + 1e-6d);
        int[] dstRgb = new int[dstWidth * dstHeight];

        double dstCx = (dstWidth - 1) * 0.5d;
        double dstCy = (dstHeight - 1) * 0.5d;

        int x;
        int y;
        for (y = 0; y < dstHeight; y++) {
            int dstRow = y * dstWidth;
            double dy = y - dstCy;
            for (x = 0; x < dstWidth; x++) {
                double dx = x - dstCx;

                // Inverse map to source pixel using nearest-neighbor sampling.
                double srcXf = dx * cos + dy * sin + srcCx;
                double srcYf = -dx * sin + dy * cos + srcCy;
                int srcX = (int) Math.floor(srcXf + 0.5d);
                int srcY = (int) Math.floor(srcYf + 0.5d);

                if (srcX >= 0 && srcX < srcWidth && srcY >= 0 && srcY < srcHeight) {
                    dstRgb[dstRow + x] = srcRgb[srcY * srcWidth + srcX];
                } else {
                    dstRgb[dstRow + x] = 0x00000000;
                }
            }
        }

        return Image.createRGBImage(dstRgb, dstWidth, dstHeight, true);
    }

    public static Image scaleAndRotate(Image src, int targetWidth, int targetHeight, int angleDegrees) {
        return rotate(scale(src, targetWidth, targetHeight), angleDegrees);
    }

    public static Image composeLayers(Image background, Image foreground, int targetWidth, int targetHeight) {
        if (background == null || foreground == null) {
            throw new IllegalArgumentException("image layer is null");
        }
        Image bg = scale(background, targetWidth, targetHeight);
        Image fg = scale(foreground, targetWidth, targetHeight);

        int[] bgRgb = new int[targetWidth * targetHeight];
        int[] fgRgb = new int[targetWidth * targetHeight];
        int[] out = new int[targetWidth * targetHeight];

        bg.getRGB(bgRgb, 0, targetWidth, 0, 0, targetWidth, targetHeight);
        fg.getRGB(fgRgb, 0, targetWidth, 0, 0, targetWidth, targetHeight);

        int i;
        for (i = 0; i < out.length; i++) {
            int f = fgRgb[i];
            int fa = (f >>> 24) & 0xFF;
            if (fa == 0) {
                out[i] = bgRgb[i];
                continue;
            }
            if (fa == 255) {
                out[i] = f;
                continue;
            }

            int b = bgRgb[i];
            int br = (b >>> 16) & 0xFF;
            int bgc = (b >>> 8) & 0xFF;
            int bb = b & 0xFF;

            int fr = (f >>> 16) & 0xFF;
            int fgc = (f >>> 8) & 0xFF;
            int fb = f & 0xFF;

            int outR = (fr * fa + br * (255 - fa)) / 255;
            int outG = (fgc * fa + bgc * (255 - fa)) / 255;
            int outB = (fb * fa + bb * (255 - fa)) / 255;
            out[i] = (0xFF << 24) | (outR << 16) | (outG << 8) | outB;
        }

        return Image.createRGBImage(out, targetWidth, targetHeight, true);
    }

    private static Image rotateRightAngle(Image src, int normalizedAngle) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int[] srcRgb = new int[srcWidth * srcHeight];
        src.getRGB(srcRgb, 0, srcWidth, 0, 0, srcWidth, srcHeight);

        int dstWidth = normalizedAngle == 180 ? srcWidth : srcHeight;
        int dstHeight = normalizedAngle == 180 ? srcHeight : srcWidth;
        int[] dstRgb = new int[dstWidth * dstHeight];

        int x;
        int y;
        if (normalizedAngle == 90) {
            for (y = 0; y < srcHeight; y++) {
                int srcRow = y * srcWidth;
                for (x = 0; x < srcWidth; x++) {
                    int dstX = srcHeight - 1 - y;
                    int dstY = x;
                    dstRgb[dstY * dstWidth + dstX] = srcRgb[srcRow + x];
                }
            }
        } else if (normalizedAngle == 180) {
            for (y = 0; y < srcHeight; y++) {
                int srcRow = y * srcWidth;
                for (x = 0; x < srcWidth; x++) {
                    int dstX = srcWidth - 1 - x;
                    int dstY = srcHeight - 1 - y;
                    dstRgb[dstY * dstWidth + dstX] = srcRgb[srcRow + x];
                }
            }
        } else {
            for (y = 0; y < srcHeight; y++) {
                int srcRow = y * srcWidth;
                for (x = 0; x < srcWidth; x++) {
                    int dstX = y;
                    int dstY = srcWidth - 1 - x;
                    dstRgb[dstY * dstWidth + dstX] = srcRgb[srcRow + x];
                }
            }
        }

        return Image.createRGBImage(dstRgb, dstWidth, dstHeight, true);
    }

    private static double[] computeRotatedBounds(int width, int height, double sin, double cos) {
        double cx = (width - 1) * 0.5d;
        double cy = (height - 1) * 0.5d;

        double[] cornersX = {-cx, (width - 1) - cx, (width - 1) - cx, -cx};
        double[] cornersY = {-cy, -cy, (height - 1) - cy, (height - 1) - cy};

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        int i;
        for (i = 0; i < 4; i++) {
            double rx = cornersX[i] * cos - cornersY[i] * sin;
            double ry = cornersX[i] * sin + cornersY[i] * cos;
            if (rx < minX) {
                minX = rx;
            }
            if (ry < minY) {
                minY = ry;
            }
            if (rx > maxX) {
                maxX = rx;
            }
            if (ry > maxY) {
                maxY = ry;
            }
        }

        return new double[]{minX, minY, maxX, maxY};
    }

    private static int normalizeAngle(int angleDegrees) {
        int normalized = angleDegrees % 360;
        if (normalized < 0) {
            normalized += 360;
        }
        return normalized;
    }
}