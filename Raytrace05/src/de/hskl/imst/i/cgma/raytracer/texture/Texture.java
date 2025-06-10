package de.hskl.imst.i.cgma.raytracer.texture;

import java.awt.image.BufferedImage;

/**
 * Klasse zur Repräsentation einer Textur mit Sampling-Funktionalität
 * SYSTEMATISCHE TEST-VERSION: Testet alle UV-Transformationen
 */
public class Texture {
    
    private final BufferedImage image;
    private final int width;
    private final int height;
    private final int[] pixels;
    
    // TEST-MODUS: Welche UV-Transformation verwenden?
    private static int testMode = 1; // Start mit V gespiegelt
    
    /**
     * Erstellt eine neue Textur aus einem BufferedImage
     * @param image Das BufferedImage
     */
    public Texture(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        
        // Extrahiere Pixel für schnelleren Zugriff
        this.pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
    }
    
    /**
     * Setzt den Test-Modus für UV-Transformationen
     * @param mode 0-7 für verschiedene Transformationen
     */
    public static void setTestMode(int mode) {
        testMode = mode % 8;
        System.out.println("UV-Test-Modus: " + mode + " - " + getTestDescription(mode));
    }
    
    /**
     * Gibt eine Beschreibung des Test-Modus zurück
     */
    public static String getTestDescription(int mode) {
        switch (mode % 8) {
            case 0: return "Original (u, v)";
            case 1: return "V gespiegelt (u, 1-v)";
            case 2: return "U gespiegelt (1-u, v)";
            case 3: return "Beide gespiegelt (1-u, 1-v)";
            case 4: return "90° gegen Uhrzeigersinn (v, 1-u)";
            case 5: return "90° im Uhrzeigersinn (1-v, u)";
            case 6: return "UV vertauscht (v, u)";
            case 7: return "UV vertauscht + V gespiegelt (v, 1-u)";
            default: return "Unbekannt";
        }
    }
    
    /**
     * Transformiert UV-Koordinaten basierend auf dem Test-Modus
     */
    private void transformUV(float u, float v, float[] result) {
        // Wrap UV-Koordinaten
        u = u - (float)Math.floor(u);
        v = v - (float)Math.floor(v);
        
        switch (testMode) {
            case 0: // Original
                result[0] = u;
                result[1] = v;
                break;
            case 1: // V gespiegelt
                result[0] = u;
                result[1] = 1.0f - v;
                break;
            case 2: // U gespiegelt
                result[0] = 1.0f - u;
                result[1] = v;
                break;
            case 3: // Beide gespiegelt
                result[0] = 1.0f - u;
                result[1] = 1.0f - v;
                break;
            case 4: // 90° gegen Uhrzeigersinn
                result[0] = v;
                result[1] = 1.0f - u;
                break;
            case 5: // 90° im Uhrzeigersinn
                result[0] = 1.0f - v;
                result[1] = u;
                break;
            case 6: // UV vertauscht
                result[0] = v;
                result[1] = u;
                break;
            case 7: // UV vertauscht + V gespiegelt
                result[0] = v;
                result[1] = 1.0f - u;
                break;
            default:
                result[0] = u;
                result[1] = v;
                break;
        }
    }
    
    /**
     * Sampelt eine Farbe aus der Textur an den gegebenen UV-Koordinaten
     * @param u UV-Koordinate U (0.0 bis 1.0)
     * @param v UV-Koordinate V (0.0 bis 1.0)
     * @param result Array für das Ergebnis [r, g, b] (0.0 bis 1.0)
     */
    public void sample(float u, float v, float[] result) {
        // UV-Transformation anwenden
        float[] uv = new float[2];
        transformUV(u, v, uv);
        
        // Konvertiere zu Pixel-Koordinaten
        int x = (int)(uv[0] * (width - 1));
        int y = (int)(uv[1] * (height - 1));
        
        // Begrenze Koordinaten
        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));
        
        // Hole Pixel-Farbe
        int pixel = pixels[y * width + x];
        
        // Extrahiere RGB-Komponenten und normalisiere zu 0.0-1.0
        result[0] = ((pixel >> 16) & 0xFF) / 255.0f; // R
        result[1] = ((pixel >> 8) & 0xFF) / 255.0f;  // G
        result[2] = (pixel & 0xFF) / 255.0f;         // B
    }
    
    /**
     * Bilinear-Sampling für glattere Ergebnisse
     * @param u UV-Koordinate U (0.0 bis 1.0)
     * @param v UV-Koordinate V (0.0 bis 1.0)
     * @param result Array für das Ergebnis [r, g, b] (0.0 bis 1.0)
     */
    public void sampleBilinear(float u, float v, float[] result) {
        // UV-Transformation anwenden
        float[] uv = new float[2];
        transformUV(u, v, uv);
        
        // Konvertiere zu kontinuierlichen Pixel-Koordinaten
        float fx = uv[0] * (width - 1);
        float fy = uv[1] * (height - 1);
        
        // Ganzzahlige und Bruchteile
        int x1 = (int)fx;
        int y1 = (int)fy;
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);
        
        float wx = fx - x1;
        float wy = fy - y1;
        
        // Hole vier benachbarte Pixel
        int pixel00 = pixels[y1 * width + x1];
        int pixel10 = pixels[y1 * width + x2];
        int pixel01 = pixels[y2 * width + x1];
        int pixel11 = pixels[y2 * width + x2];
        
        // Extrahiere Farbkanäle für alle vier Pixel
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
        
        // Bilineare Interpolation
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
    
    /**
     * Gibt die Breite der Textur zurück
     * @return Breite in Pixeln
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gibt die Höhe der Textur zurück
     * @return Höhe in Pixeln
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gibt das zugrundeliegende BufferedImage zurück
     * @return Das BufferedImage
     */
    public BufferedImage getImage() {
        return image;
    }
}
