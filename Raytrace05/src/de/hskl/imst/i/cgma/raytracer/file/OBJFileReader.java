package de.hskl.imst.i.cgma.raytracer.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasse zum Laden und Konvertieren von OBJ-Dateien in das T_Mesh-Format des Raytracers
 */
public class OBJFileReader {
      /**
     * Liest eine OBJ-Datei und konvertiert sie in ein OBJ_Mesh-Objekt
     * @param objFile Die OBJ-Datei
     * @return Ein OBJ_Mesh-Objekt mit den Daten aus der OBJ-Datei
     * @throws IOException Bei Fehlern beim Lesen der Datei
     */
    public static OBJ_Mesh read(File objFile) throws IOException {
        System.out.println("Lade OBJ-Datei: " + objFile.getPath());
        
        // Erstelle ein OBJ_Mesh-Objekt
        OBJ_Mesh mesh = new OBJ_Mesh();
        
        // Definiere Standard-Material
        float[][] defaultMaterials = new float[1][9];
        defaultMaterials[0] = new float[] {
            0.2f, 0.2f, 0.2f,  // ambient
            0.8f, 0.8f, 0.8f,  // diffuse
            0.0f, 0.0f, 0.0f   // specular
        };
        int[] defaultMaterialsN = new int[1];
        defaultMaterialsN[0] = 1;        // Listen für OBJ-Daten
        List<float[]> vertices = new ArrayList<>();
        List<float[]> uvCoordinates = new ArrayList<>(); // UV-Koordinaten aus der OBJ-Datei
        List<int[]> faces = new ArrayList<>();
        List<int[]> uvIndices = new ArrayList<>(); // UV-Indizes für jedes Face
        List<Integer> faceMaterials = new ArrayList<>(); // Material-Index für jedes Face
        Map<String, Integer> materialNameToIndex = new HashMap<>();
        Map<String, String> materialToTexturePath = new HashMap<>(); // Material-Name zu Textur-Pfad
        int currentMaterialIndex = 0; // Standard-Material
        
        try (BufferedReader reader = new BufferedReader(new FileReader(objFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Überspringe Kommentare und leere Zeilen
                }
                
                String[] parts = line.split("\\s+");                if (parts[0].equalsIgnoreCase("v")) { // Vertex
                    if (parts.length >= 4) {
                        float[] vertex = new float[3];
                        vertex[0] = Float.parseFloat(parts[1]);
                        vertex[1] = Float.parseFloat(parts[2]);
                        vertex[2] = Float.parseFloat(parts[3]);
                        vertices.add(vertex);
                    }
                }
                else if (parts[0].equalsIgnoreCase("vt")) { // Texture coordinate (UV)
                    if (parts.length >= 3) {
                        float[] uv = new float[2];
                        uv[0] = Float.parseFloat(parts[1]); // U
                        uv[1] = Float.parseFloat(parts[2]); // V
                        uvCoordinates.add(uv);
                    }
                }
                else if (parts[0].equalsIgnoreCase("f")) { // Face
                    if (parts.length >= 4) {
                        // Dreieck erzeugen (OBJ-Indizes beginnen bei 1, Java bei 0)
                        int[] face = new int[3];
                        int[] uvFace = new int[3]; // UV-Indizes für dieses Face
                        
                        // Für jeden Eckpunkt des Face
                        for (int i = 0; i < 3; i++) {
                            String[] vertexData = parts[i + 1].split("/");
                            // Vertex-Index (immer vorhanden)
                            face[i] = Integer.parseInt(vertexData[0]) - 1;
                            
                            // UV-Index (optional, nach dem ersten "/")
                            if (vertexData.length > 1 && !vertexData[1].isEmpty()) {
                                uvFace[i] = Integer.parseInt(vertexData[1]) - 1;
                            } else {
                                uvFace[i] = -1; // Kein UV-Index
                            }
                        }
                        
                        faces.add(face);
                        uvIndices.add(uvFace);
                        faceMaterials.add(currentMaterialIndex); // Speichere aktuelles Material für dieses Face
                    }
                }
                else if (parts[0].equalsIgnoreCase("usemtl")) { // Material verwenden
                    if (parts.length >= 2) {
                        String materialName = parts[1];
                        Integer materialIndex = materialNameToIndex.get(materialName);
                        if (materialIndex != null) {
                            currentMaterialIndex = materialIndex;
                        } else {
                            System.out.println("WARNUNG: Unbekanntes Material: " + materialName);
                        }
                    }
                }                else if (parts[0].equalsIgnoreCase("mtllib")) {
                    if (parts.length >= 2) {
                        String mtlFilename = parts[1];
                        File mtlFile = new File(objFile.getParent(), mtlFilename);
                        if (mtlFile.exists()) {
                            loadMTLFile(mtlFile, mesh, materialNameToIndex, materialToTexturePath);
                        } else {
                            System.out.println("WARNUNG: MTL-Datei nicht gefunden: " + mtlFilename);
                        }
                    }
                }
            }
        }
          // Konvertiere die Daten in das T_Mesh-Format
        mesh.vertices = new float[vertices.size()][3];
        for (int i = 0; i < vertices.size(); i++) {
            mesh.vertices[i] = vertices.get(i);
        }

        mesh.triangles = new int[faces.size()][3];
        for (int i = 0; i < faces.size(); i++) {
            mesh.triangles[i] = faces.get(i);
        }
        
        // UV-Koordinaten verarbeiten - erstelle UV-Array für jeden Vertex
        if (!uvCoordinates.isEmpty()) {
            System.out.println("Verarbeite UV-Koordinaten: " + uvCoordinates.size() + " UV-Paare für " + mesh.vertices.length + " Vertices");
            
            // Erstelle UV-Array basierend auf der größten Anzahl von Vertices
            mesh.uvCoordinates = new float[mesh.vertices.length][2];
            
            // Initialisiere alle UV-Koordinaten mit Standardwerten
            for (int i = 0; i < mesh.uvCoordinates.length; i++) {
                mesh.uvCoordinates[i][0] = 0.0f; // U
                mesh.uvCoordinates[i][1] = 0.0f; // V
            }
            
            // Setze UV-Koordinaten basierend auf Face-UV-Indizes
            for (int faceIndex = 0; faceIndex < uvIndices.size() && faceIndex < mesh.triangles.length; faceIndex++) {
                int[] face = mesh.triangles[faceIndex];
                int[] uvFace = uvIndices.get(faceIndex);
                
                for (int i = 0; i < 3; i++) {
                    int vertexIndex = face[i];
                    int uvIndex = uvFace[i];
                    
                    if (uvIndex >= 0 && uvIndex < uvCoordinates.size() && 
                        vertexIndex >= 0 && vertexIndex < mesh.uvCoordinates.length) {
                        
                        float[] uv = uvCoordinates.get(uvIndex);
                        mesh.uvCoordinates[vertexIndex][0] = uv[0];
                        mesh.uvCoordinates[vertexIndex][1] = uv[1];
                    }
                }
            }
        }
        
        // Textur-Pfade für Materialien setzen
        if (!materialToTexturePath.isEmpty()) {
            mesh.texturePaths = new String[mesh.materials != null ? mesh.materials.length : 1];
            
            for (Map.Entry<String, String> entry : materialToTexturePath.entrySet()) {
                String materialName = entry.getKey();
                String texturePath = entry.getValue();
                Integer materialIndex = materialNameToIndex.get(materialName);
                
                if (materialIndex != null && materialIndex < mesh.texturePaths.length) {
                    mesh.texturePaths[materialIndex] = texturePath;
                    System.out.println("Material '" + materialName + "' (Index " + materialIndex + ") -> Textur: " + texturePath);
                }
            }
        }
          // Setze Material für alle Vertices basierend auf Face-Materialien
        mesh.verticesMat = new int[mesh.vertices.length];
        // Initialisiere alle Vertices mit Material 0
        for (int i = 0; i < mesh.verticesMat.length; i++) {
            mesh.verticesMat[i] = 0;
        }
        
        // Setze Material für Vertices basierend auf den Faces, die sie verwenden
        for (int faceIndex = 0; faceIndex < mesh.triangles.length; faceIndex++) {
            int materialIndex = faceMaterials.get(faceIndex);
            // Setze Material für alle drei Vertices dieses Faces
            for (int vertexIndex : mesh.triangles[faceIndex]) {
                if (vertexIndex >= 0 && vertexIndex < mesh.verticesMat.length) {
                    mesh.verticesMat[vertexIndex] = materialIndex;
                }
            }
        }
        
        // Setze Material und Material-N, falls nicht durch MTL-Datei gesetzt
        if (mesh.materials == null) {
            mesh.materials = defaultMaterials;
            mesh.materialsN = defaultMaterialsN;
        }
        
        // Setze Shading-Modus auf Phong
        mesh.fgp = 'p';
        
        // Berechne Bounding Box
        mesh.calcBoundingBox();
          System.out.println("OBJ-Datei erfolgreich geladen: " + 
                           mesh.vertices.length + " Vertices, " + 
                           mesh.triangles.length + " Dreiecke");
        
        // Debug: Zähle Materialverwendung
        int[] materialUsageCount = new int[mesh.materials.length];
        for (int i = 0; i < mesh.verticesMat.length; i++) {
            if (mesh.verticesMat[i] < materialUsageCount.length) {
                materialUsageCount[mesh.verticesMat[i]]++;
            }
        }
        System.out.print("Material-Verwendung: ");
        for (int i = 0; i < materialUsageCount.length; i++) {
            if (materialUsageCount[i] > 0) {
                System.out.print("Mat" + i + ":" + materialUsageCount[i] + " ");
            }
        }
        System.out.println();
        
        return mesh;
    }    /**
     * Liest eine MTL-Datei und setzt die Materialien im Mesh
     * @param mtlFile Die MTL-Datei
     * @param mesh Das Mesh, zu dem die Materialien hinzugefügt werden sollen
     * @param materialNameToIndex Map zur Zuordnung von Material-Namen zu Indizes
     * @param materialToTexturePath Map zur Zuordnung von Material-Namen zu Textur-Pfaden
     * @throws IOException Bei Fehlern beim Lesen der Datei
     */
    private static void loadMTLFile(File mtlFile, T_Mesh mesh, Map<String, Integer> materialNameToIndex, Map<String, String> materialToTexturePath) throws IOException {
        System.out.println("Lade MTL-Datei: " + mtlFile.getPath());
        
        List<float[]> materials = new ArrayList<>();
        List<Integer> materialsN = new ArrayList<>();
        
        // Füge Standardmaterial hinzu
        materials.add(new float[] {
            0.2f, 0.2f, 0.2f,  // ambient
            0.8f, 0.8f, 0.8f,  // diffuse
            0.0f, 0.0f, 0.0f   // specular
        });
        materialsN.add(1);
          try (BufferedReader reader = new BufferedReader(new FileReader(mtlFile))) {
            String line;
            float[] currentMaterial = null;
            int currentShine = 1;
            String currentMaterialName = "";
            int materialIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");                if (parts[0].equalsIgnoreCase("newmtl")) {
                    // Speichere vorheriges Material, wenn vorhanden
                    if (currentMaterial != null && !currentMaterialName.isEmpty()) {
                        materials.add(currentMaterial);
                        materialsN.add(currentShine);
                        materialNameToIndex.put(currentMaterialName, materialIndex);
                        materialIndex++;
                    }
                      // Erstelle neues Material mit neutralen Standardwerten
                    // Diese werden durch die echten MTL-Werte überschrieben
                    currentMaterial = new float[9];
                    currentMaterialName = parts.length > 1 ? parts[1] : "";
                      // Setze unterschiedliche Standardwerte basierend auf Material-Namen für bessere Sichtbarkeit
                    if (currentMaterialName.toLowerCase().contains("kuppel")) {
                        // Kuppel - deutlich bläulich
                        currentMaterial[0] = 0.1f; currentMaterial[1] = 0.1f; currentMaterial[2] = 0.4f; // ambient
                        currentMaterial[3] = 0.2f; currentMaterial[4] = 0.5f; currentMaterial[5] = 1.0f; // diffuse - noch blauer
                        currentMaterial[6] = 0.3f; currentMaterial[7] = 0.3f; currentMaterial[8] = 0.8f; // specular
                    } else if (currentMaterialName.toLowerCase().contains("cylinder")) {
                        // Zylinder - metallisch silber/weiß
                        currentMaterial[0] = 0.3f; currentMaterial[1] = 0.3f; currentMaterial[2] = 0.3f; // ambient
                        currentMaterial[3] = 0.9f; currentMaterial[4] = 0.9f; currentMaterial[5] = 1.0f; // diffuse - sehr hell
                        currentMaterial[6] = 1.0f; currentMaterial[7] = 1.0f; currentMaterial[8] = 1.0f; // specular
                    } else if (currentMaterialName.toLowerCase().contains("door")) {
                        // Tür - deutlich dunkler/schwarz
                        currentMaterial[0] = 0.02f; currentMaterial[1] = 0.02f; currentMaterial[2] = 0.05f; // ambient
                        currentMaterial[3] = 0.1f; currentMaterial[4] = 0.1f; currentMaterial[5] = 0.2f; // diffuse - sehr dunkel
                        currentMaterial[6] = 0.05f; currentMaterial[7] = 0.05f; currentMaterial[8] = 0.1f; // specular
                    } else if (currentMaterialName.toLowerCase().contains("ausenflaeche") || currentMaterialName.toLowerCase().contains("außen")) {
                        // Außenfläche - hell/weiß mit leichtem Gelbstich
                        currentMaterial[0] = 0.4f; currentMaterial[1] = 0.4f; currentMaterial[2] = 0.3f; // ambient
                        currentMaterial[3] = 1.0f; currentMaterial[4] = 0.95f; currentMaterial[5] = 0.8f; // diffuse - cremeweiß
                        currentMaterial[6] = 0.3f; currentMaterial[7] = 0.3f; currentMaterial[8] = 0.2f; // specular
                    } else if (currentMaterialName.toLowerCase().contains("detail")) {
                        // Details - deutlich rötlich
                        currentMaterial[0] = 0.3f; currentMaterial[1] = 0.1f; currentMaterial[2] = 0.1f; // ambient
                        currentMaterial[3] = 1.0f; currentMaterial[4] = 0.2f; currentMaterial[5] = 0.2f; // diffuse - kräftig rot
                        currentMaterial[6] = 0.4f; currentMaterial[7] = 0.1f; currentMaterial[8] = 0.1f; // specular
                    } else if (currentMaterialName.toLowerCase().contains("oeffnung")) {
                        // Öffnungen - grünlich für Kontrast
                        currentMaterial[0] = 0.1f; currentMaterial[1] = 0.2f; currentMaterial[2] = 0.1f; // ambient
                        currentMaterial[3] = 0.2f; currentMaterial[4] = 0.8f; currentMaterial[5] = 0.3f; // diffuse - grün
                        currentMaterial[6] = 0.1f; currentMaterial[7] = 0.3f; currentMaterial[8] = 0.1f; // specular
                    } else {
                        // Standard - neutral grau (aber heller als vorher)
                        currentMaterial[0] = 0.3f; currentMaterial[1] = 0.3f; currentMaterial[2] = 0.3f; // ambient
                        currentMaterial[3] = 0.7f; currentMaterial[4] = 0.7f; currentMaterial[5] = 0.7f; // diffuse
                        currentMaterial[6] = 0.2f; currentMaterial[7] = 0.2f; currentMaterial[8] = 0.2f; // specular
                    }
                    currentShine = 32;
                }                else if (parts[0].equalsIgnoreCase("ka")) { // Ambient
                    if (currentMaterial != null && parts.length >= 4) {
                        float ka_r = Float.parseFloat(parts[1]);
                        float ka_g = Float.parseFloat(parts[2]);
                        float ka_b = Float.parseFloat(parts[3]);
                        
                        // Nur überschreiben wenn die MTL-Werte nicht neutral-weiß sind (1.0, 1.0, 1.0)
                        if (!(ka_r == 1.0f && ka_g == 1.0f && ka_b == 1.0f)) {
                            currentMaterial[0] = ka_r;
                            currentMaterial[1] = ka_g;
                            currentMaterial[2] = ka_b;
                        }
                    }
                }else if (parts[0].equalsIgnoreCase("kd")) { // Diffuse
                    if (currentMaterial != null && parts.length >= 4) {
                        float kd_r = Float.parseFloat(parts[1]);
                        float kd_g = Float.parseFloat(parts[2]);
                        float kd_b = Float.parseFloat(parts[3]);
                        
                        // Nur überschreiben wenn die MTL-Werte nicht neutral-grau sind
                        // oder wenn sie signifikant von unseren Standardwerten abweichen
                        if (!(kd_r == 0.8f && kd_g == 0.8f && kd_b == 0.8f)) {
                            currentMaterial[3] = kd_r;
                            currentMaterial[4] = kd_g;
                            currentMaterial[5] = kd_b;
                        }
                        // Für neutrale graue Werte behalten wir unsere bunten Standardwerte
                    }
                }                else if (parts[0].equalsIgnoreCase("map_kd")) { // Diffuse texture
                    if (parts.length >= 2) {
                        String texturePath = parts[1];
                        // Speichere Textur-Pfad für das aktuelle Material
                        if (!currentMaterialName.isEmpty()) {
                            materialToTexturePath.put(currentMaterialName, texturePath);
                            System.out.println("Material '" + currentMaterialName + "' -> Textur: " + texturePath);
                        }
                    }
                    
                    // Wenn nur eine Textur ohne Kd-Werte definiert ist, verwende vernünftige Standardwerte
                    if (currentMaterial != null) {
                        // Prüfe ob Kd noch die Standardwerte hat (0.8, 0.8, 0.8)
                        if (currentMaterial[3] == 0.8f && currentMaterial[4] == 0.8f && currentMaterial[5] == 0.8f) {
                            // Setze bessere Standardwerte für Materialien mit Texturen
                            currentMaterial[3] = 0.7f; currentMaterial[4] = 0.7f; currentMaterial[5] = 0.7f;
                        }
                    }
                }else if (parts[0].equalsIgnoreCase("ks")) { // Specular
                    if (currentMaterial != null && parts.length >= 4) {
                        float ks_r = Float.parseFloat(parts[1]);
                        float ks_g = Float.parseFloat(parts[2]);
                        float ks_b = Float.parseFloat(parts[3]);
                        
                        // Nur überschreiben wenn die MTL-Werte nicht neutral-grau sind (0.5, 0.5, 0.5)
                        if (!(ks_r == 0.5f && ks_g == 0.5f && ks_b == 0.5f)) {
                            currentMaterial[6] = ks_r;
                            currentMaterial[7] = ks_g;
                            currentMaterial[8] = ks_b;
                        }
                    }
                }
                else if (parts[0].equalsIgnoreCase("ns")) { // Shininess
                    if (parts.length >= 2) {
                        float ns = Float.parseFloat(parts[1]);
                        // Konvertiere zu einem vernünftigen Wert
                        currentShine = (int) Math.max(1, ns / 10);
                    }
                }
            }
              // Speichere letztes Material, wenn vorhanden
            if (currentMaterial != null && !currentMaterialName.isEmpty()) {
                materials.add(currentMaterial);
                materialsN.add(currentShine);
                materialNameToIndex.put(currentMaterialName, materialIndex);
            }
        }
        
        // Konvertiere zu Arrays
        mesh.materials = new float[materials.size()][9];
        mesh.materialsN = new int[materialsN.size()];
          for (int i = 0; i < materials.size(); i++) {
            mesh.materials[i] = materials.get(i);
            mesh.materialsN[i] = materialsN.get(i);
        }
        
        System.out.println("MTL-Datei erfolgreich geladen: " + materials.size() + " Materialien");
        System.out.println("Material-Zuordnungen: " + materialNameToIndex.toString());
    }
}
