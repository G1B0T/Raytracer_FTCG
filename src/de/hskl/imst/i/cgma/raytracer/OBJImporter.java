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
        Map<String, float[]> materials = new HashMap<>();
        Map<String, Integer> materialsN = new HashMap<>();
        if (mtlFile != null && mtlFile.exists()) {
            parseMTL(mtlFile, materials, materialsN);
        }
        
        // Temporäre Listen für OBJ-Daten
        List<float[]> vertices = new ArrayList<>();
        List<Integer> verticesMat = new ArrayList<>();
        List<int[]> triangles = new ArrayList<>();
        
        // Aktuelles Material
        String currentMaterial = null;
        int currentMaterialIndex = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(objFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Kommentare oder leere Zeilen überspringen
                }
                
                String[] parts = line.split("\\s+");
                
                if (parts[0].equals("v")) {
                    // Vertex: v x y z
                    if (parts.length >= 4) {
                        float[] vertex = new float[3];
                        vertex[0] = Float.parseFloat(parts[1]);
                        vertex[1] = Float.parseFloat(parts[2]);
                        vertex[2] = Float.parseFloat(parts[3]);
                        vertices.add(vertex);
                        
                        // Material-Index für diesen Vertex setzen
                        verticesMat.add(currentMaterialIndex);
                    }
                } else if (parts[0].equals("f")) {
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
                    }
                } else if (parts[0].equals("usemtl")) {
                    // Material-Referenz: usemtl material_name
                    if (parts.length >= 2) {
                        currentMaterial = parts[1];
                        
                        // Material-Index finden oder neu zuweisen
                        if (materials.containsKey(currentMaterial)) {
                            currentMaterialIndex = new ArrayList<>(materials.keySet()).indexOf(currentMaterial);
                        }
                    }
                } else if (parts[0].equals("mtllib")) {
                    // MTL-Datei-Referenz: mtllib file.mtl
                    // Wir ignorieren das hier, da die MTL-Datei bereits als Parameter übergeben wurde
                }
            }
        }
        
        // T_Mesh-Daten aus den temporären Listen erstellen
        mesh.vertices = vertices.toArray(new float[0][0]);
        mesh.verticesMat = new int[verticesMat.size()];
        for (int i = 0; i < verticesMat.size(); i++) {
            mesh.verticesMat[i] = verticesMat.get(i);
        }
        
        mesh.triangles = triangles.toArray(new int[0][0]);
        
        // Materials in das richtige Format konvertieren
        int numMaterials = materials.size();
        mesh.materials = new float[numMaterials][9]; // ar ag ab dr dg db sr sg sb
        mesh.materialsN = new int[numMaterials];
        
        int materialIndex = 0;
        for (String matName : materials.keySet()) {
            float[] matValues = materials.get(matName);
            System.arraycopy(matValues, 0, mesh.materials[materialIndex], 0, 9);
            mesh.materialsN[materialIndex] = materialsN.getOrDefault(matName, 1);
            materialIndex++;
        }
        
        // Wenn keine Materialien gefunden wurden, ein Standardmaterial hinzufügen
        if (numMaterials == 0) {
            mesh.materials = new float[1][9];
            mesh.materialsN = new int[1];
            // ar ag ab dr dg db sr sg sb
            mesh.materials[0] = new float[]{0.2f, 0.2f, 0.2f, 0.8f, 0.8f, 0.8f, 0.0f, 0.0f, 0.0f};
            mesh.materialsN[0] = 1;
        }
        
        // BoundingBox berechnen
        mesh.calcBoundingBox();
        
        // Transformationen anwenden, ähnlich wie beim STLImporter
        transformMesh(mesh, 15f, -15f, 100f);
        
        return mesh;
    }
    
    private static void parseMTL(File mtlFile, Map<String, float[]> materials, Map<String, Integer> materialsN) throws IOException {
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
                        materials.put(currentMaterial, new float[9]); // ar ag ab dr dg db sr sg sb
                        materialsN.put(currentMaterial, 1);
                    }
                } else if (currentMaterial != null) {
                    if (parts[0].equals("Ka") && parts.length >= 4) {
                        // Ambient color: Ka r g b
                        float[] material = materials.get(currentMaterial);
                        material[0] = Float.parseFloat(parts[1]);
                        material[1] = Float.parseFloat(parts[2]);
                        material[2] = Float.parseFloat(parts[3]);
                    } else if (parts[0].equals("Kd") && parts.length >= 4) {
                        // Diffuse color: Kd r g b
                        float[] material = materials.get(currentMaterial);
                        material[3] = Float.parseFloat(parts[1]);
                        material[4] = Float.parseFloat(parts[2]);
                        material[5] = Float.parseFloat(parts[3]);
                    } else if (parts[0].equals("Ks") && parts.length >= 4) {
                        // Specular color: Ks r g b
                        float[] material = materials.get(currentMaterial);
                        material[6] = Float.parseFloat(parts[1]);
                        material[7] = Float.parseFloat(parts[2]);
                        material[8] = Float.parseFloat(parts[3]);
                    } else if (parts[0].equals("Ns") && parts.length >= 2) {
                        // Specular exponent: Ns value
                        // Für den T_Mesh materialsN verwenden
                        float nsValue = Float.parseFloat(parts[1]);
                        // Konvertieren von Ns (0-1000) zu einem vernünftigen Wert für materialsN
                        int n = Math.max(1, Math.min(128, (int)(nsValue / 8)));
                        materialsN.put(currentMaterial, n);
                    }
                }
            }
        }
    }
    
    private static void transformMesh(T_Mesh mesh, float scale, float zShiftBefore, float zShiftAfter) {
        for (int i = 0; i < mesh.vertices.length; i++) {
            // Vor der Skalierung verschieben
            mesh.vertices[i][2] += zShiftBefore;
            
            // Skalieren
            for (int j = 0; j < 3; j++) {
                mesh.vertices[i][j] *= scale;
            }
            
            // Nach der Skalierung nochmal verschieben
            mesh.vertices[i][2] += zShiftAfter;
        }
        
        // Bounding-Box aktualisieren
        mesh.calcBoundingBox();
    }
}
