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
    }
    
    /**
     * Interpoliert UV-Koordinaten basierend auf baryzentrischen Koordinaten
     * @param triangleIndex Index des Dreiecks
     * @param u Baryzentrische Koordinate u
     * @param v Baryzentrische Koordinate v
     * @param result Array für das Ergebnis [u_texture, v_texture]
     * @return true wenn UV-Koordinaten interpoliert werden konnten
     */
    public boolean interpolateUV(int triangleIndex, float u, float v, float[] result) {
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
            return false;
        }
        
        // Hole UV-Koordinaten der drei Vertices
        float[] uv0 = uvCoordinates[v0];
        float[] uv1 = uvCoordinates[v1];
        float[] uv2 = uvCoordinates[v2];
        
        // Baryzentrische Interpolation: P = (1-u-v)*P0 + u*P1 + v*P2
        float w = 1.0f - u - v;
        
        result[0] = w * uv0[0] + u * uv1[0] + v * uv2[0];
        result[1] = w * uv0[1] + u * uv1[1] + v * uv2[1];
        
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
    
    @Override
    public String getHeader() {
        return "TRIANGLE_MESH"; // Gebe den gleichen Header zurück, damit es mit der vorhandenen Logik funktioniert
    }
}