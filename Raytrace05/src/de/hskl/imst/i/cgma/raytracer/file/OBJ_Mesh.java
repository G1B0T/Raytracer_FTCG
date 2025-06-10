package de.hskl.imst.i.cgma.raytracer.file;

/**
 * OBJ_Mesh erweitert T_Mesh, um das OBJ-Dateiformat zu unterstützen.
 * Die Klasse dient hauptsächlich als Typidentifikator für OBJ-Mesh-Objekte.
 */
public class OBJ_Mesh extends T_Mesh {
    
    /**
     * Konstruktor für ein leeres OBJ_Mesh
     */
    public OBJ_Mesh() {
        super();
        // Setze Shading-Modus auf Phong
        this.fgp = 'p';
    }
    
    @Override
    public String getHeader() {
        return "TRIANGLE_MESH"; // Gebe den gleichen Header zurück, damit es mit der vorhandenen Logik funktioniert
    }
}