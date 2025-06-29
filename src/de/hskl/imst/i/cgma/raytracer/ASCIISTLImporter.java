package de.hskl.imst.i.cgma.raytracer;

import de.hskl.imst.i.cgma.raytracer.file.T_Mesh;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASCIISTLImporter {

    public static T_Mesh loadASCIISTL(File file) throws IOException {
        T_Mesh mesh = new T_Mesh();

        List<float[]> vertexList = new ArrayList<>();
        Map<String, Integer> vertexIndexMap = new HashMap<>();
        List<int[]> triangleList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int[] triangle = new int[3];
            int vertexIndex = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("vertex")) {
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);

                    float[] vertex = new float[]{x, y, z};
                    String key = Arrays.toString(vertex);
                    int index = vertexIndexMap.computeIfAbsent(key, k -> {
                        vertexList.add(vertex);
                        return vertexList.size() - 1;
                    });

                    triangle[vertexIndex++] = index;

                    if (vertexIndex == 3) {
                        triangleList.add(Arrays.copyOf(triangle, 3));
                        vertexIndex = 0;
                    }
                }
            }
        }

        int numVertices = vertexList.size();
        int numTriangles = triangleList.size();

        mesh.vertices = new float[numVertices][3];
        mesh.verticesMat = new int[numVertices];
        for (int i = 0; i < numVertices; i++) {
            mesh.vertices[i] = vertexList.get(i);
            mesh.verticesMat[i] = 0;
        }

        mesh.triangles = new int[numTriangles][3];
        mesh.triangleColors = new float[numTriangles][3];
        for (int i = 0; i < numTriangles; i++) {
            mesh.triangles[i] = triangleList.get(i);
            mesh.triangleColors[i] = new float[]{1.0f, 0.0f, 0.0f}; // Standardfarbe
        }

        mesh.materials = new float[1][9];
        // Die 9 Floats repräsentieren typischerweise:
        // [Ambient-R, Ambient-G, Ambient-B, Diffuse-R, Diffuse-G, Diffuse-B, Specular-R, Specular-G, Specular-B]
        mesh.materials[0] = new float[]{
            0.0f, 1.0f, 0.0f, // Ambient-Komponente (Rot)
            0.0f, 0.0f, 1.0f, // Diffuse-Komponente (Rot - die "Grundfarbe" des Objekts)
            1.7f, 1.7f, 1.7f  // Specular-Komponente (Hellweiß - für Glanzlichter)
        };
        mesh.materialsN = new int[]{50}; // Glanzzahl (Shininess). Eine höhere Zahl (z.B. 50-100) für schärfere Glanzlichter.
        mesh.fgp = 'p'; // 'f' oder 'F' bedeutet Flat-Shading, das die Farbe des Materials nutzt.
        mesh.calcBoundingBox();

        return mesh;
    }
    
}
