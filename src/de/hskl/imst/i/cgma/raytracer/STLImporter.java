package de.hskl.imst.i.cgma.raytracer;

import de.hskl.imst.i.cgma.raytracer.file.T_Mesh;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class STLImporter {

    public static T_Mesh loadSTL(File file, Float scale) throws IOException {
        T_Mesh mesh;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            if (firstLine != null && firstLine.toLowerCase().contains("solid")) {
                mesh = ASCIISTLImporter.loadASCIISTL(file);
            } else {
                mesh = BinarySTLImporter.loadBinarySTL(file);
            }
        }

        scaleMesh(mesh, scale);

        // Transformation anwenden
        transformMesh(mesh, 15f, -15f, 100f); // scale, zShiftBefore, zShiftAfter

        return mesh;
    }

    private static void scaleMesh(T_Mesh mesh, float factor) {
        for (int i = 0; i < mesh.vertices.length; i++) {
            mesh.vertices[i][0] *= factor;
            mesh.vertices[i][1] *= factor;
            mesh.vertices[i][2] *= factor;
        }
        mesh.calcBoundingBox();
    }

    public static void transformMesh(T_Mesh mesh, float scale, float zShiftBefore, float zShiftAfter) {
        for (int i = 0; i < mesh.vertices.length; i++) {
            // Vor der Skalierung verschieben (z. B. z -= 15)
            mesh.vertices[i][2] += zShiftBefore;
    
            // Skalieren
            for (int j = 0; j < 3; j++) {
                mesh.vertices[i][j] *= scale;
            }
    
            // Nach der Skalierung nochmal verschieben (z. B. z += 100)
            mesh.vertices[i][2] += zShiftAfter;
        }
    
        // Bounding-Box aktualisieren
        mesh.calcBoundingBox();
    }
}

