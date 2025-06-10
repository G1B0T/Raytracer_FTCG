package de.hskl.imst.i.cgma.raytracer.texture;

/**
 * Debug-Klasse für UV-Koordinaten-Transformationen
 * Testet verschiedene Orientierungen und Rotationen
 */
public class UVDebugger {
    
    public enum UVTransform {
        ORIGINAL,           // u, v (keine Transformation)
        FLIP_V,            // u, 1-v (V spiegeln)
        FLIP_U,            // 1-u, v (U spiegeln)
        FLIP_BOTH,         // 1-u, 1-v (beide spiegeln)
        ROTATE_90_CCW,     // v, 1-u (90° gegen Uhrzeigersinn)
        ROTATE_90_CW,      // 1-v, u (90° im Uhrzeigersinn)
        ROTATE_180,        // 1-u, 1-v (180° drehen)
        SWAP_UV            // v, u (U und V vertauschen)
    }
    
    /**
     * Transformiert UV-Koordinaten basierend auf dem gewählten Modus
     * @param u Original U-Koordinate
     * @param v Original V-Koordinate
     * @param transform Gewünschte Transformation
     * @param result Array für [u', v'] Ergebnis
     */
    public static void transformUV(float u, float v, UVTransform transform, float[] result) {
        // Wrap UV-Koordinaten (Wiederholung bei Überschreitung)
        u = u - (float)Math.floor(u);
        v = v - (float)Math.floor(v);
        
        switch (transform) {
            case ORIGINAL:
                result[0] = u;
                result[1] = v;
                break;
                
            case FLIP_V:
                result[0] = u;
                result[1] = 1.0f - v;
                break;
                
            case FLIP_U:
                result[0] = 1.0f - u;
                result[1] = v;
                break;
                
            case FLIP_BOTH:
                result[0] = 1.0f - u;
                result[1] = 1.0f - v;
                break;
                
            case ROTATE_90_CCW:
                result[0] = v;
                result[1] = 1.0f - u;
                break;
                
            case ROTATE_90_CW:
                result[0] = 1.0f - v;
                result[1] = u;
                break;
                
            case ROTATE_180:
                result[0] = 1.0f - u;
                result[1] = 1.0f - v;
                break;
                
            case SWAP_UV:
                result[0] = v;
                result[1] = u;
                break;
                
            default:
                result[0] = u;
                result[1] = v;
                break;
        }
    }
    
    /**
     * Gibt eine Beschreibung der Transformation zurück
     */
    public static String getTransformDescription(UVTransform transform) {
        switch (transform) {
            case ORIGINAL: return "Original (u, v)";
            case FLIP_V: return "V gespiegelt (u, 1-v)";
            case FLIP_U: return "U gespiegelt (1-u, v)";
            case FLIP_BOTH: return "Beide gespiegelt (1-u, 1-v)";
            case ROTATE_90_CCW: return "90° gegen Uhrzeigersinn (v, 1-u)";
            case ROTATE_90_CW: return "90° im Uhrzeigersinn (1-v, u)";
            case ROTATE_180: return "180° gedreht (1-u, 1-v)";
            case SWAP_UV: return "UV vertauscht (v, u)";
            default: return "Unbekannt";
        }
    }
}
