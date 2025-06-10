package de.hskl.imst.i.cgma.raytracer.texture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse zum Laden und Verwalten von Texturen für den Raytracer
 */
public class TextureLoader {
    
    /**
     * Cache für geladene Texturen, um wiederverwendete Texturen nicht mehrfach zu laden
     */
    private static Map<String, Texture> textureCache = new HashMap<>();
    
    /**
     * Lädt eine Textur aus einer Datei
     * @param filePath Der Pfad zur Textur-Datei
     * @return Eine Textur-Instanz oder null bei Fehlern
     */
    public static Texture loadTexture(String filePath) {
        // Prüfe Cache zuerst
        if (textureCache.containsKey(filePath)) {
            return textureCache.get(filePath);
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("WARNUNG: Textur-Datei nicht gefunden: " + filePath);
                return null;
            }
            
            System.out.println("Lade Textur: " + filePath);
            BufferedImage image = ImageIO.read(file);
            
            if (image == null) {
                System.err.println("FEHLER: Kann Textur nicht laden: " + filePath);
                return null;
            }
            
            Texture texture = new Texture(image);
            textureCache.put(filePath, texture);
            
            System.out.println("Textur erfolgreich geladen: " + image.getWidth() + "x" + image.getHeight() + " - " + filePath);
            return texture;
            
        } catch (IOException e) {
            System.err.println("FEHLER beim Laden der Textur " + filePath + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Lädt eine Textur relativ zum angegebenen Basis-Verzeichnis
     * @param baseDir Das Basis-Verzeichnis
     * @param relativePath Der relative Pfad zur Textur
     * @return Eine Textur-Instanz oder null bei Fehlern
     */
    public static Texture loadTexture(File baseDir, String relativePath) {
        File textureFile = new File(baseDir, relativePath);
        return loadTexture(textureFile.getAbsolutePath());
    }
    
    /**
     * Löscht den Textur-Cache (nützlich für Tests)
     */
    public static void clearCache() {
        textureCache.clear();
    }
    
    /**
     * Gibt die Anzahl der geladenen Texturen zurück
     * @return Anzahl der Texturen im Cache
     */
    public static int getCacheSize() {
        return textureCache.size();
    }
}
