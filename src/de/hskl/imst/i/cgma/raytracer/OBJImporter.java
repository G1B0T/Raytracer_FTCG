package de.hskl.imst.i.cgma.raytracer;

import de.hskl.imst.i.cgma.raytracer.file.T_Mesh;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBJImporter {

    public static T_Mesh loadOBJ(File objFile, File mtlFile) throws IOException {
        T_Mesh mesh = new T_Mesh();
        
        // MTL-Datei parsen, wenn vorhanden
        Map<String, float[]> materialColors = new HashMap<>();   // Speichert die Farbwerte je Material
        Map<String, Integer> materialNValues = new HashMap<>();  // Speichert die N-Werte je Material
        Map<String, Integer> materialIndices = new HashMap<>();  // Speichert die Indices für jedes Material
        
        if (mtlFile != null && mtlFile.exists()) {
            parseMTL(mtlFile, materialColors, materialNValues);
            System.out.println("Geladene Materialien: " + materialColors.keySet());
            
            // Materialnamen zu Indizes zuordnen
            int index = 0;
            for (String matName : materialColors.keySet()) {
                materialIndices.put(matName, index++);
                System.out.println("Material: " + matName + " -> Index: " + (index-1));
            }
        }
        
        // Temporäre Listen für OBJ-Daten
        List<float[]> vertices = new ArrayList<>();
        List<int[]> triangles = new ArrayList<>();
        List<Integer> triangleMaterials = new ArrayList<>(); // Material-Index für jedes Dreieck
        
        // Aktuelles Material
        String currentMaterial = null;
        int currentMaterialIndex = 0;
        
        // Erstes Material als Standard verwenden, wenn vorhanden
        if (!materialIndices.isEmpty()) {
            currentMaterial = materialIndices.keySet().iterator().next();
            currentMaterialIndex = materialIndices.get(currentMaterial);
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(objFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Kommentare oder leere Zeilen überspringen
                }
                
                String[] parts = line.split("\\s+");
                
                switch (parts[0]) {
                    case "v":
                        // Vertex: v x y z
                        if (parts.length >= 4) {
                            float[] vertex = new float[3];
                            vertex[0] = Float.parseFloat(parts[1]);
                            vertex[1] = Float.parseFloat(parts[2]);
                            vertex[2] = Float.parseFloat(parts[3]);
                            vertices.add(vertex);
                        }
                        break;
                        
                    case "f":
                        // Face: f v1 v2 v3 oder f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
                        if (parts.length >= 4) {
                            int[] triangle = new int[3];
                            
                            for (int i = 0; i < 3; i++) {
                                String vertexData = parts[i + 1];
                                int vertexIndex;
                                
                                if (vertexData.contains("/")) {
                                    // Format: v/vt/vn
                                    vertexIndex = Integer.parseInt(vertexData.split("/")[0]) - 1;
                                } else {
                                    // Format: v
                                    vertexIndex = Integer.parseInt(vertexData) - 1;
                                }
                                
                                triangle[i] = vertexIndex;
                            }
                            
                            triangles.add(triangle);
                            triangleMaterials.add(currentMaterialIndex);
                            System.out.println("Dreieck hinzugefügt mit Material-Index: " + currentMaterialIndex);
                        }
                        break;
                        
                    case "usemtl":
                        // Material-Referenz: usemtl material_name
                        if (parts.length >= 2) {
                            currentMaterial = parts[1];
                            
                            // Material-Index finden oder neu zuweisen
                            if (materialIndices.containsKey(currentMaterial)) {
                                currentMaterialIndex = materialIndices.get(currentMaterial);
                                System.out.println("Wechsel zu Material: " + currentMaterial + " (Index: " + currentMaterialIndex + ")");
                            } else {
                                System.out.println("Material nicht gefunden: " + currentMaterial);
                            }
                        }
                        break;
                        
                    case "mtllib":
                        // MTL-Datei-Referenz wird hier ignoriert
                        break;
                }
            }
        }
        
        // T_Mesh-Daten erstellen
        mesh.vertices = vertices.toArray(new float[0][0]);
        mesh.triangles = triangles.toArray(new int[0][0]);
        
        // Materialien für jeden Vertex basierend auf den Dreiecken zuweisen
        mesh.verticesMat = new int[vertices.size()];
        
        // Zunächst alle Vertices mit dem Standard-Material initialisieren
        for (int i = 0; i < mesh.verticesMat.length; i++) {
            mesh.verticesMat[i] = 0;
        }
        
        // Dann für jedes Dreieck die Materialindizes der Vertices aktualisieren
        for (int i = 0; i < triangles.size(); i++) {
            int materialIndex = triangleMaterials.get(i);
            int[] triangle = triangles.get(i);
            
            // Jedem Vertex des Dreiecks das Material des Dreiecks zuweisen
            for (int vertexIdx : triangle) {
                mesh.verticesMat[vertexIdx] = materialIndex;
            }
        }
        
        // Materialien erstellen
        int numMaterials = materialColors.size();
        mesh.materials = new float[Math.max(1, numMaterials)][9]; // ar ag ab dr dg db sr sg sb
        mesh.materialsN = new int[Math.max(1, numMaterials)];
        
        if (numMaterials > 0) {
            int matIndex = 0;
            for (String matName : materialColors.keySet()) {
                float[] matValues = materialColors.get(matName);
                System.arraycopy(matValues, 0, mesh.materials[matIndex], 0, 9);
                mesh.materialsN[matIndex] = materialNValues.getOrDefault(matName, 1);
                matIndex++;
            }
        } else {
            // Standardmaterial, wenn keine Materialien gefunden wurden
            mesh.materials[0] = new float[]{0.2f, 0.2f, 0.2f, 0.8f, 0.8f, 0.8f, 0.0f, 0.0f, 0.0f};
            mesh.materialsN[0] = 1;
        }
        
        // BoundingBox berechnen
        mesh.calcBoundingBox();
        
        // Transformationen anwenden
        transformMesh(mesh, 15f, -15f, 100f);
        
        return mesh;
    }
    
    private static void parseMTL(File mtlFile, Map<String, float[]> materialColors, Map<String, Integer> materialNValues) throws IOException {
        String currentMaterial = null;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(mtlFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Kommentare oder leere Zeilen überspringen
                }
                
                String[] parts = line.split("\\s+");
                
                if (parts[0].equals("newmtl")) {
                    // Neues Material: newmtl material_name
                    if (parts.length >= 2) {
                        currentMaterial = parts[1];
                        materialColors.put(currentMaterial, new float[9]); // ar ag ab dr dg db sr sg sb
                        materialNValues.put(currentMaterial, 1);
                    }
                } else if (currentMaterial != null) {
                    float[] color = materialColors.get(currentMaterial);
                    
                    switch (parts[0]) {
                        case "Ka":
                            // Ambient color: Ka r g b
                            if (parts.length >= 4) {
                                color[0] = Float.parseFloat(parts[1]);
                                color[1] = Float.parseFloat(parts[2]);
                                color[2] = Float.parseFloat(parts[3]);
                            }
                            break;
                            
                        case "Kd":
                            // Diffuse color: Kd r g b
                            if (parts.length >= 4) {
                                color[3] = Float.parseFloat(parts[1]);
                                color[4] = Float.parseFloat(parts[2]);
                                color[5] = Float.parseFloat(parts[3]);
                            }
                            break;
                            
                        case "Ks":
                            // Specular color: Ks r g b
                            if (parts.length >= 4) {
                                color[6] = Float.parseFloat(parts[1]);
                                color[7] = Float.parseFloat(parts[2]);
                                color[8] = Float.parseFloat(parts[3]);
                            }
                            break;
                            
                        case "Ns":
                            // Specular exponent: Ns value
                            if (parts.length >= 2) {
                                float nsValue = Float.parseFloat(parts[1]);
                                // Konvertieren von Ns (0-1000) zu einem vernünftigen Wert für materialsN
                                int n = Math.max(1, Math.min(128, (int)(nsValue / 8)));
                                materialNValues.put(currentMaterial, n);
                            }
                            break;
                    }
                }
            }
        }
    }
    
    private static void transformMesh(T_Mesh mesh, float scale, float zShiftBefore, float zShiftAfter) {
        for (float[] vertex : mesh.vertices) {
            // Vor der Skalierung verschieben
            vertex[2] += zShiftBefore;
            
            // Skalieren
            for (int j = 0; j < 3; j++) {
                vertex[j] *= scale;
            }
            
            // Nach der Skalierung nochmal verschieben
            vertex[2] += zShiftAfter;
        }
        
        // Bounding-Box aktualisieren
        mesh.calcBoundingBox();
    }
}
