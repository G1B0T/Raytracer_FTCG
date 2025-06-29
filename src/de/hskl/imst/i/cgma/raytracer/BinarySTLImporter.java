package de.hskl.imst.i.cgma.raytracer;

import de.hskl.imst.i.cgma.raytracer.file.T_Mesh;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class BinarySTLImporter {

    public static T_Mesh loadBinarySTL(File file) throws IOException {
        return loadBinarySTL(file, false, 1.0f); // Standard: keine Transformation
    }

    public static T_Mesh loadBinarySTL(File file, boolean centerMesh, float scale) throws IOException {
        T_Mesh mesh = readBinarySTL(file);

        // Alle Dreiecke auf eine bestimmte Farbe setzen (z.B. Rot)
        for (int i = 0; i < mesh.triangleColors.length; i++) {
            mesh.triangleColors[i] = new float[]{1.0f, 0.0f, 0.0f}; // Rot
        }

        if (centerMesh || scale != 1.0f) {
            float[] min = new float[]{Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY};
            float[] max = new float[]{Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};

            // Bounding Box berechnen
            for (float[] v : mesh.vertices) {
                for (int i = 0; i < 3; i++) {
                    min[i] = Math.min(min[i], v[i]);
                    max[i] = Math.max(max[i], v[i]);
                }
            }

            float[] center = new float[]{
                (min[0] + max[0]) / 2f,
                (min[1] + max[1]) / 2f,
                (min[2] + max[2]) / 2f
            };

            for (int i = 0; i < mesh.vertices.length; i++) {
                for (int j = 0; j < 3; j++) {
                    if (centerMesh) {
                        mesh.vertices[i][j] -= center[j];
                    }
                    mesh.vertices[i][j] *= scale;
                }
            }

            mesh.calcBoundingBox();
        }

        return mesh;
    }

    private static T_Mesh readBinarySTL(File file) throws IOException {
        T_Mesh mesh = new T_Mesh();

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            dis.skipBytes(80); // Header ignorieren
            int triangleCount = Integer.reverseBytes(dis.readInt());

            List<float[]> vertexList = new ArrayList<>();
            Map<String, Integer> vertexIndexMap = new HashMap<>();
            List<int[]> triangleList = new ArrayList<>();
            List<float[]> triangleColorList = new ArrayList<>();

            for (int i = 0; i < triangleCount; i++) {
                float[] normal = readVec3(dis); // Ignoriert, kann aber gespeichert werden

                int[] triangle = new int[3];
                for (int v = 0; v < 3; v++) {
                    float[] vertex = readVec3(dis);
                    String key = Arrays.toString(vertex);

                    int index = vertexIndexMap.computeIfAbsent(key, k -> {
                        vertexList.add(vertex);
                        return vertexList.size() - 1;
                    });

                    triangle[v] = index;
                }

                triangleList.add(triangle);

                int attr = Short.toUnsignedInt(dis.readShort());
                boolean hasColor = (attr & 0x8000) == 0;

                if (hasColor) {
                    int r = (attr >> 10) & 0x1F;
                    int g = (attr >> 5) & 0x1F;
                    int b = attr & 0x1F;

                    float red = r / 31.0f;
                    float green = g / 31.0f;
                    float blue = b / 31.0f;
                    triangleColorList.add(new float[]{red, green, blue});
                } else {
                    triangleColorList.add(new float[]{1.0f, 0.0f, 0.0f}); // Default-Farbe
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
                mesh.triangleColors[i] = triangleColorList.get(i);
            }

            mesh.materials = new float[1][9];
            // Interpretation der 9 Floats für ein Material (häufig: Ambient, Diffuse, Specular)
            // [Ambient-R, Ambient-G, Ambient-B, Diffuse-R, Diffuse-G, Diffuse-B, Specular-R, Specular-G, Specular-B]
            mesh.materials[0] = new float[]{
                0.0f, 1.0f, 0.0f, // Ambient (Rot) - Licht, das unabhängig von der Lichtquelle abgestrahlt wird
                0.0f, 0.0f, 1.0f, // Diffuse (Rot) - Grundfarbe unter direkter Beleuchtung
                1.7f, 1.7f, 1.7f  // Specular (Hellweiß) - Farbe der Glanzlichter (kann über 1.0 gehen für HDR/helle Lichter)
            };
            mesh.materialsN = new int[]{50}; // Glanzzahl (Shininess): 50 ist ein guter Startwert für deutliche Glanzlichter. Höhere Werte = kleinere, schärfere Glanzlichter.


            mesh.fgp = 'p';
            mesh.calcBoundingBox();
        }

        return mesh;
    }

    private static float[] readVec3(DataInputStream dis) throws IOException {
        return new float[]{
            readFloatLE(dis),
            readFloatLE(dis),
            readFloatLE(dis)
        };
    }

    private static float readFloatLE(DataInputStream dis) throws IOException {
        byte[] bytes = new byte[4];
        dis.readFully(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
}
