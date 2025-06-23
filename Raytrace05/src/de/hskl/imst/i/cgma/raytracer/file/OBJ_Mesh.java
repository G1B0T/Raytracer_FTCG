package de.hskl.imst.i.cgma.raytracer.file;

/**
 * OBJ_Mesh erweitert T_Mesh, um das OBJ-Dateiformat mit UV-Koordinaten und Texturen zu unterstützen.
 */
public class OBJ_Mesh extends T_Mesh {
    
    /**
     * UV-Koordinaten für jeden Vertex im Format [u, v]
     * uvCoordinates[i] = UV-Koordinaten für Vertex i
     */
    public float[][] uvCoordinates;
    
    /**
     * Textur-Pfade für jedes Material
     */
    public String[] texturePaths;
    
    /**
     * Material-Index für jedes Face (Dreieck)
     * faceMaterials[i] = Material-Index für Face i
     */
    public int[] faceMaterials;
    
    /**
     * Konstruktor für ein leeres OBJ_Mesh
     */
    public OBJ_Mesh() {
        super();
        // Setze Shading-Modus auf Phong
        this.fgp = 'p';
    }
    
    /**
     * Holt die UV-Koordinaten für einen Vertex
     * @param vertexIndex Der Index des Vertex
     * @param result Array für das Ergebnis [u, v]
     * @return true wenn UV-Koordinaten gefunden wurden
     */
    public boolean getUVCoordinates(int vertexIndex, float[] result) {
        if (uvCoordinates != null && vertexIndex >= 0 && vertexIndex < uvCoordinates.length) {
            result[0] = uvCoordinates[vertexIndex][0];
            result[1] = uvCoordinates[vertexIndex][1];
            return true;
        }
        return false;
    }    /**
     * Interpoliert UV-Koordinaten basierend auf baryzentrischen Koordinaten
     * @param triangleIndex Index des Dreiecks
     * @param bu Baryzentrische Koordinate bu (für v2)
     * @param bv Baryzentrische Koordinate bv (für v0) 
     * @param result Array für das Ergebnis [u_texture, v_texture]
     * @return true wenn UV-Koordinaten interpoliert werden konnten
     */
    public boolean interpolateUV(int triangleIndex, float bu, float bv, float[] result) {
        if (uvCoordinates == null || triangleIndex < 0 || triangleIndex >= triangles.length) {
            return false;
        }
        
        // Hole die drei Vertex-Indizes des Dreiecks
        int[] triangle = triangles[triangleIndex];
        int v0 = triangle[0];
        int v1 = triangle[1]; 
        int v2 = triangle[2];
        
        // Prüfe, ob alle Vertex-Indizes gültige UV-Koordinaten haben
        if (v0 >= uvCoordinates.length || v1 >= uvCoordinates.length || v2 >= uvCoordinates.length) {
            if (Math.random() < 0.001) {
                System.err.printf("FEHLER: UV-Index außerhalb Grenzen: v0=%d, v1=%d, v2=%d, uvLength=%d%n", 
                    v0, v1, v2, uvCoordinates.length);
            }
            return false;
        }
        
        // Prüfe auf null UV-Koordinaten
        if (uvCoordinates[v0] == null || uvCoordinates[v1] == null || uvCoordinates[v2] == null) {
            if (Math.random() < 0.001) {
                System.err.printf("FEHLER: Null UV-Koordinaten für Triangle %d%n", triangleIndex);
            }
            return false;
        }
        
        // Hole UV-Koordinaten der drei Vertices
        float[] uv0 = uvCoordinates[v0];
        float[] uv1 = uvCoordinates[v1];
        float[] uv2 = uvCoordinates[v2];
        
        // Prüfe auf ungültige UV-Werte
        if (Float.isNaN(uv0[0]) || Float.isNaN(uv0[1]) || 
            Float.isNaN(uv1[0]) || Float.isNaN(uv1[1]) || 
            Float.isNaN(uv2[0]) || Float.isNaN(uv2[1])) {
            if (Math.random() < 0.001) {
                System.err.printf("FEHLER: NaN in UV-Koordinaten für Triangle %d%n", triangleIndex);
            }
            return false;
        }
        
        // KORRIGIERTE baryzentrische Interpolation
        // bu, bv, bw entsprechen den Gewichtungen für v2, v0, v1 (wie im Raytracer definiert)
        float bw = 1.0f - bu - bv;
        
        // Interpoliere UV-Koordinaten: bu*uv2 + bv*uv0 + bw*uv1
        result[0] = bu * uv2[0] + bv * uv0[0] + bw * uv1[0];
        result[1] = bu * uv2[1] + bv * uv0[1] + bw * uv1[1];
        
        // Debug: Zeige UV-Interpolation (selten)
        if (Math.random() < 0.0001) {
            System.out.printf("UV-Interpolation: Triangle %d, Bary(%.3f,%.3f,%.3f) -> UV(%.3f,%.3f)%n", 
                triangleIndex, bu, bv, bw, result[0], result[1]);
            System.out.printf("  UV0(%.3f,%.3f) UV1(%.3f,%.3f) UV2(%.3f,%.3f)%n",
                uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]);
        }
        
        return true;
    }
    
    /**
     * Holt den Textur-Pfad für einen Material-Index
     * @param materialIndex Der Index des Materials
     * @return Der Textur-Pfad oder null
     */
    public String getTexturePath(int materialIndex) {
        if (texturePaths != null && materialIndex >= 0 && materialIndex < texturePaths.length) {
            return texturePaths[materialIndex];
        }
        return null;
    }
    
    /**
     * Holt das Material für ein Face (Dreieck)
     * @param faceIndex Index des Faces
     * @return Material-Index oder 0 wenn nicht gefunden
     */
    public int getFaceMaterial(int faceIndex) {
        if (faceMaterials != null && faceIndex >= 0 && faceIndex < faceMaterials.length) {
            return faceMaterials[faceIndex];
        }
        return 0; // Standard-Material
    }
    
    @Override
    public String getHeader() {
        return "TRIANGLE_MESH"; // Gebe den gleichen Header zurück, damit es mit der vorhandenen Logik funktioniert
    }
}