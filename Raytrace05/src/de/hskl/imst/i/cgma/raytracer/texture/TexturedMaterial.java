package de.hskl.imst.i.cgma.raytracer.texture;

/**
 * Erweiterte Material-Klasse mit Textur-Unterstützung
 */
public class TexturedMaterial {
    
    // Standard-Material-Eigenschaften
    public float[] ambient = new float[3];   // Ka
    public float[] diffuse = new float[3];   // Kd
    public float[] specular = new float[3];  // Ks
    public int shininess = 32;               // Ns
    
    // Textur-Eigenschaften
    public Texture diffuseTexture = null;    // map_Kd
    public Texture normalTexture = null;     // map_Bump
    public Texture specularTexture = null;   // map_Ks
    
    /**
     * Erstellt ein Material aus einem Standard-Material-Array
     * @param materialData Array mit [r_a, g_a, b_a, r_d, g_d, b_d, r_s, g_s, b_s]
     * @param shininess Shininess-Wert
     */
    public TexturedMaterial(float[] materialData, int shininess) {
        if (materialData.length >= 9) {
            System.arraycopy(materialData, 0, ambient, 0, 3);
            System.arraycopy(materialData, 3, diffuse, 0, 3);
            System.arraycopy(materialData, 6, specular, 0, 3);
        }
        this.shininess = shininess;
    }
    
    /**
     * Erstellt ein leeres Material mit Standard-Werten
     */
    public TexturedMaterial() {
        ambient[0] = 0.2f; ambient[1] = 0.2f; ambient[2] = 0.2f;
        diffuse[0] = 0.8f; diffuse[1] = 0.8f; diffuse[2] = 0.8f;
        specular[0] = 0.0f; specular[1] = 0.0f; specular[2] = 0.0f;
        shininess = 32;
    }
    
    /**
     * Setzt die Diffuse-Textur
     * @param texture Die Textur
     */
    public void setDiffuseTexture(Texture texture) {
        this.diffuseTexture = texture;
    }
    
    /**
     * Holt die Diffuse-Farbe an den gegebenen UV-Koordinaten
     * @param u UV-Koordinate U
     * @param v UV-Koordinate V
     * @param result Array für das Ergebnis [r, g, b]
     */
    public void getDiffuseColor(float u, float v, float[] result) {
        if (diffuseTexture != null) {
            // Textur-Farbe sampeln
            float[] textureColor = new float[3];
            diffuseTexture.sampleBilinear(u, v, textureColor);
            
            // Mit Material-Diffuse-Farbe modulieren
            result[0] = diffuse[0] * textureColor[0];
            result[1] = diffuse[1] * textureColor[1];
            result[2] = diffuse[2] * textureColor[2];
        } else {
            // Nur Material-Farbe verwenden
            System.arraycopy(diffuse, 0, result, 0, 3);
        }
    }
    
    /**
     * Holt die Ambient-Farbe an den gegebenen UV-Koordinaten
     * @param u UV-Koordinate U
     * @param v UV-Koordinate V
     * @param result Array für das Ergebnis [r, g, b]
     */
    public void getAmbientColor(float u, float v, float[] result) {
        if (diffuseTexture != null) {
            // Textur-Farbe sampeln und mit Ambient modulieren
            float[] textureColor = new float[3];
            diffuseTexture.sampleBilinear(u, v, textureColor);
            
            result[0] = ambient[0] * textureColor[0];
            result[1] = ambient[1] * textureColor[1];
            result[2] = ambient[2] * textureColor[2];
        } else {
            // Nur Material-Farbe verwenden
            System.arraycopy(ambient, 0, result, 0, 3);
        }
    }
    
    /**
     * Holt die Specular-Farbe
     * @param result Array für das Ergebnis [r, g, b]
     */
    public void getSpecularColor(float[] result) {
        System.arraycopy(specular, 0, result, 0, 3);
    }
    
    /**
     * Gibt zurück, ob das Material eine Diffuse-Textur hat
     * @return true wenn eine Diffuse-Textur vorhanden ist
     */
    public boolean hasDiffuseTexture() {
        return diffuseTexture != null;
    }
}
