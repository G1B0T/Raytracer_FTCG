package de.hskl.imst.i.cgma.raytracer.texture;

import java.awt.image.BufferedImage;

/**
 * Test-Version der Texture-Klasse mit umschaltbaren UV-Transformationen
 * Zum schnellen Testen verschiedener UV-Orientierungen
 */
public class TextureTest {
    
    private final BufferedImage image;
    private final int width;
    private final int height;
    private final int[] pixels;
    
    // Verschiedene UV-Transformationsmodi zum Testen
    public enum UVMode {
        ORIGINAL,        // u, v (keine Transformation)
        FLIP_V,         // u, 1-v (V spiegeln - häufig für OpenGL)
        FLIP_U,         // 1-u, v (U spiegeln)
        FLIP_BOTH,      // 1-u, 1-v (beide spiegeln)
        ROTATE_90_CW,   // 1-v, u (90° im Uhrzeigersinn)
        ROTATE_90_CCW,  // v, 1-u (90° gegen Uhrzeigersinn)
        ROTATE_180,     // 1-u, 1-v (180° drehen)
        SWAP_UV         // v, u (U und V vertauschen)
    }
    
    private static UVMode currentMode = UVMode.ORIGINAL;
    
    public TextureTest(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
    }
    
    /**
     * Setzt den UV-Transformationsmodus
     */
    public static void setUVMode(UVMode mode) {
        currentMode = mode;
        System.out.println("UV-Test-Modus: " + mode + " - " + getDescription(mode));
    }
    
    private static String getDescription(UVMode mode) {
        switch (mode) {
            case ORIGINAL: return "Original (u, v)";
            case FLIP_V: return "V gespiegelt (u, 1-v)";
            case FLIP_U: return "U gespiegelt (1-u, v)";
            case FLIP_BOTH: return "Beide gespiegelt (1-u, 1-v)";
            case ROTATE_90_CW: return "90° im Uhrzeigersinn (1-v, u)";
            case ROTATE_90_CCW: return "90° gegen Uhrzeigersinn (v, 1-u)";
            case ROTATE_180: return "180° gedreht (1-u, 1-v)";
            case SWAP_UV: return "UV vertauscht (v, u)";
            default: return "Unbekannt";
        }
    }
    
    /**
     * Transformiert UV-Koordinaten basierend auf dem aktuellen Modus
     */
    private void transformUV(float u, float v, float[] result) {
        switch (currentMode) {
            case ORIGINAL:
                result[0] = u; result[1] = v;
                break;
            case FLIP_V:
                result[0] = u; result[1] = 1.0f - v;
                break;
            case FLIP_U:
                result[0] = 1.0f - u; result[1] = v;
                break;
            case FLIP_BOTH:
                result[0] = 1.0f - u; result[1] = 1.0f - v;
                break;
            case ROTATE_90_CW:
                result[0] = 1.0f - v; result[1] = u;
                break;
            case ROTATE_90_CCW:
                result[0] = v; result[1] = 1.0f - u;
                break;
            case ROTATE_180:
                result[0] = 1.0f - u; result[1] = 1.0f - v;
                break;
            case SWAP_UV:
                result[0] = v; result[1] = u;
                break;
        }
    }
    
    public void sampleBilinear(float u, float v, float[] result) {
        // Wrap UV-Koordinaten
        u = u - (float)Math.floor(u);
        v = v - (float)Math.floor(v);
        
        // Transformiere UV-Koordinaten
        float[] transformedUV = new float[2];
        transformUV(u, v, transformedUV);
        u = transformedUV[0];
        v = transformedUV[1];
        
        // Rest der bilinearen Interpolation...
        float fx = u * (width - 1);
        float fy = v * (height - 1);
        
        int x1 = (int)fx;
        int y1 = (int)fy;
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);
        
        float wx = fx - x1;
        float wy = fy - y1;
        
        int pixel00 = pixels[y1 * width + x1];
        int pixel10 = pixels[y1 * width + x2];
        int pixel01 = pixels[y2 * width + x1];
        int pixel11 = pixels[y2 * width + x2];
        
        float r00 = ((pixel00 >> 16) & 0xFF) / 255.0f;
        float g00 = ((pixel00 >> 8) & 0xFF) / 255.0f;
        float b00 = (pixel00 & 0xFF) / 255.0f;
        
        float r10 = ((pixel10 >> 16) & 0xFF) / 255.0f;
        float g10 = ((pixel10 >> 8) & 0xFF) / 255.0f;
        float b10 = (pixel10 & 0xFF) / 255.0f;
        
        float r01 = ((pixel01 >> 16) & 0xFF) / 255.0f;
        float g01 = ((pixel01 >> 8) & 0xFF) / 255.0f;
        float b01 = (pixel01 & 0xFF) / 255.0f;
        
        float r11 = ((pixel11 >> 16) & 0xFF) / 255.0f;
        float g11 = ((pixel11 >> 8) & 0xFF) / 255.0f;
        float b11 = (pixel11 & 0xFF) / 255.0f;
        
        float r0 = r00 * (1 - wx) + r10 * wx;
        float r1 = r01 * (1 - wx) + r11 * wx;
        result[0] = r0 * (1 - wy) + r1 * wy;
        
        float g0 = g00 * (1 - wx) + g10 * wx;
        float g1 = g01 * (1 - wx) + g11 * wx;
        result[1] = g0 * (1 - wy) + g1 * wy;
        
        float b0 = b00 * (1 - wx) + b10 * wx;
        float b1 = b01 * (1 - wx) + b11 * wx;
        result[2] = b0 * (1 - wy) + b1 * wy;
    }
    
    // Getter-Methoden
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public BufferedImage getImage() { return image; }
}
