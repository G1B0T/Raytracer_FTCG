package de.hskl.imst.i.cgma.raytracer.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        defaultMaterialsN[0] = 1;
        
        // Listen für OBJ-Daten
        List<float[]> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(objFile))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Überspringe Kommentare und leere Zeilen
                }
                
                String[] parts = line.split("\\s+");
                
                if (parts[0].equalsIgnoreCase("v")) { // Vertex
                    if (parts.length >= 4) {
                        float[] vertex = new float[3];
                        vertex[0] = Float.parseFloat(parts[1]);
                        vertex[1] = Float.parseFloat(parts[2]);
                        vertex[2] = Float.parseFloat(parts[3]);
                        vertices.add(vertex);
                    }
                }
                else if (parts[0].equalsIgnoreCase("f")) { // Face
                    if (parts.length >= 4) {
                        // Dreieck erzeugen (OBJ-Indizes beginnen bei 1, Java bei 0)
                        int[] face = new int[3];
                        
                        // Für jeden Eckpunkt des Face
                        for (int i = 0; i < 3; i++) {
                            String[] vertexData = parts[i + 1].split("/");
                            // Vertex-Index (immer vorhanden)
                            face[i] = Integer.parseInt(vertexData[0]) - 1;
                        }
                        
                        faces.add(face);
                    }
                }
                else if (parts[0].equalsIgnoreCase("mtllib")) {
                    if (parts.length >= 2) {
                        String mtlFilename = parts[1];
                        File mtlFile = new File(objFile.getParent(), mtlFilename);
                        if (mtlFile.exists()) {
                            loadMTLFile(mtlFile, mesh);
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
        
        // Setze Standardmaterial für alle Vertices
        mesh.verticesMat = new int[mesh.vertices.length];
        for (int i = 0; i < mesh.verticesMat.length; i++) {
            mesh.verticesMat[i] = 0;
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
        
        return mesh;
    }
    
    /**
     * Liest eine MTL-Datei und setzt die Materialien im Mesh
     * @param mtlFile Die MTL-Datei
     * @param mesh Das Mesh, zu dem die Materialien hinzugefügt werden sollen
     * @throws IOException Bei Fehlern beim Lesen der Datei
     */
    private static void loadMTLFile(File mtlFile, T_Mesh mesh) throws IOException {
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
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                
                if (parts[0].equalsIgnoreCase("newmtl")) {
                    // Speichere vorheriges Material, wenn vorhanden
                    if (currentMaterial != null) {
                        materials.add(currentMaterial);
                        materialsN.add(currentShine);
                    }
                    
                    // Erstelle neues Material
                    currentMaterial = new float[9];
                    currentShine = 1;
                }
                else if (parts[0].equalsIgnoreCase("ka")) { // Ambient
                    if (currentMaterial != null && parts.length >= 4) {
                        currentMaterial[0] = Float.parseFloat(parts[1]);
                        currentMaterial[1] = Float.parseFloat(parts[2]);
                        currentMaterial[2] = Float.parseFloat(parts[3]);
                    }
                }
                else if (parts[0].equalsIgnoreCase("kd")) { // Diffuse
                    if (currentMaterial != null && parts.length >= 4) {
                        currentMaterial[3] = Float.parseFloat(parts[1]);
                        currentMaterial[4] = Float.parseFloat(parts[2]);
                        currentMaterial[5] = Float.parseFloat(parts[3]);
                    }
                }
                else if (parts[0].equalsIgnoreCase("ks")) { // Specular
                    if (currentMaterial != null && parts.length >= 4) {
                        currentMaterial[6] = Float.parseFloat(parts[1]);
                        currentMaterial[7] = Float.parseFloat(parts[2]);
                        currentMaterial[8] = Float.parseFloat(parts[3]);
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
            if (currentMaterial != null) {
                materials.add(currentMaterial);
                materialsN.add(currentShine);
            }
        }
        
        // Konvertiere zu Arrays
        mesh.materials = new float[materials.size()][9];
        mesh.materialsN = new int[materialsN.size()];
        
        for (int i = 0; i < materials.size(); i++) {
            mesh.materials[i] = materials.get(i);
            mesh.materialsN[i] = materialsN.get(i);
        }
    }
}
