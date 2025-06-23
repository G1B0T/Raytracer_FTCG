package de.hskl.imst.i.cgma.raytracer;
 
import de.hskl.imst.i.cgma.raytracer.file.I_Sphere;
import de.hskl.imst.i.cgma.raytracer.file.OBJFileReader;
import de.hskl.imst.i.cgma.raytracer.file.OBJ_Mesh;
import de.hskl.imst.i.cgma.raytracer.file.RTFile;
import de.hskl.imst.i.cgma.raytracer.file.RT_Object;
import de.hskl.imst.i.cgma.raytracer.file.T_Mesh;
import de.hskl.imst.i.cgma.raytracer.gui.IRayTracerImplementation;
import de.hskl.imst.i.cgma.raytracer.gui.RayTracerGui;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;

public class Raytracer05 implements IRayTracerImplementation {
    // viewing volume with infinite end
    private float fovyDegree;
    private float near;
    private float fovyRadians;

    // one hardcoded point light as a minimal solution :-(
    private float[] Ia = { 0.25f, 0.25f, 0.25f }; // ambient light color
    private float[] Ids = { 1.0f, 1.0f, 1.0f }; // diffuse and specular light
						// color
    private float[] ICenter = { 4.0f, 4.0f, 2.0f }; // center of point light
    
    // Textur-Manager für PNG-Texturen
    private Map<String, BufferedImage> textureCache = new HashMap<>();
    
    RayTracerGui gui = new RayTracerGui(this);private int resx, resy; // viewport resolution
    private float h, w, aspect; // window height, width and aspect ratio
    
    Vector<RT_Object> objects;
    
    private Raytracer05() {	try {
	    //gui.addObject(RTFileReader.read(I_Sphere.class, new File("Raytrace05/data/ikugel.dat")));
	    //gui.addObject(RTFileReader.read(I_Sphere.class, new File("Raytrace05/data/ikugel2.dat")));
	    //gui.addObject(RTFileReader.read(T_Mesh.class, new File("d:/FTCG/Raytrace05/Raytrace05/data/dreieck1.dat")));
	    //gui.addObject(RTFileReader.read(T_Mesh.class, new File("d:/FTCG/Raytrace05/Raytrace05/data/dreiecke2.dat")));
	    //gui.addObject(RTFileReader.read(T_Mesh.class, new File("d:/FTCG/Raytrace05/Raytrace05/data/kugel1.dat")));
	    //gui.addObject(RTFileReader.read(T_Mesh.class, new File("d:/FTCG/Raytrace05/Raytrace05/data/kugel2.dat")));
	    //gui.addObject(RTFileReader.read(T_Mesh.class, new File("d:/FTCG/Raytrace05/Raytrace05/data/kugel3.dat")));	    // Lade OBJ-Datei - nur ein R2D2-Modell in der Mitte
	    File objFile = new File("d:/FTCG/Raytrace05/Raytrace05/data/r2d2_joined.obj");
	    if (objFile.exists()) {
	        System.out.println("OBJ-Datei gefunden, lade R2-D2 Modell: " + objFile.getPath());
	        
	        // R2-D2: Gouraud Shading (in der Mitte)  
	        OBJ_Mesh objMesh = OBJFileReader.read(objFile);
	        objMesh.fgp = 'g'; // Gouraud shading
	        transformModel(objMesh, 0.0f, 0.0f, -5.0f); // Mitte positionieren
	        gui.addObject(objMesh);
	        System.out.println("R2-D2 Modell (Gouraud Shading) erfolgreich geladen und positioniert.");
	    } else {
	        System.out.println("Keine OBJ-Datei gefunden in: " + objFile.getPath());
	    }
	    
	    objects = gui.getObjects();

	} catch (IOException e) {
	    e.printStackTrace();
	    // Fallback: Initialisiere objects als leeren Vector, um NullPointerException zu vermeiden
	    objects = new Vector<RT_Object>();
	}
    }

    public void setViewParameters(float fovyDegree, float near) {
	    // set attributes fovyDegree, fovyRadians, near
    	this.near = near;
    	this.fovyDegree = fovyDegree;	
    	this.fovyRadians = (float) (fovyDegree/ (float) 360* (float)2*Math.PI);
	    // set attributes resx, resy, aspect
	    resx = gui.getResX();
	    resy = gui.getResY();
	    aspect = (float) resx/ (float) resy;
	    
	    // set attributes h, w
	    h = (float) (2*near* Math.tan(fovyRadians/2));	
	    w = h*aspect;
	    
    }

    @Override
    public void doRayTrace() {
	float x, y, z;			// intersection point in viewing plane
	float rayEx, rayEy, rayEz;	// eye point==ray starting point
	float rayVx, rayVy, rayVz;	// ray vector
	Color color;

	// prepare mesh data (normals and areas)
	prepareMeshData();
	// hardcoded viewing volume with fovy and near
	setViewParameters(90.0f, 1.0f);
	// set eye point
	rayEx = 0;
	rayEy = 0;
	rayEz = 0;
			
	
	z = -near;

	// prepare mesh data for shading
	precalculateMeshDataShading();
		
	// xp, yp: pixel coordinates
	for (int xp = 0; xp < resx; ++xp) { //resx
	    for (int yp = 0; yp < resy; ++yp) { //resy
		// for demo purposes
		// gui.setPixel(xp, yp, Color.WHITE.getRGB());	
		//gui.setPixel(xp, yp, new Color(rd.nextFloat(), rd.nextFloat(), rd.nextFloat()).getRGB());

		// x, y: view coordinates
    	x = (((float)xp)/(float) (resx-1))*w-((float)w)/((float)2);
	    y = ((float)(resy - 1) - yp)/(float)(resy -1)*h-(float)h/(float)2;
	    
		// ray vector
	    
	    rayVx = x;
	    rayVy = y;
	    rayVz = -near;
		
		
		// get color or null along the ray
		//color=traceRayAndGetColor...
		// if color!=null set pixel with color
    	color = traceRayAndGetColor(rayEx, rayEy, rayEz, rayVx, rayVy, rayVz);
    	if(color != null) {
    		gui.setPixel(xp, yp, color.getRGB());
    	}
		
	    }
	}
    }

    private Color traceRayAndGetColor(float rayEx, float rayEy, float rayEz, float rayVx, float rayVy, float rayVz) {
    	// RTFile scene = gui.getFile();

    	double minT = Float.MAX_VALUE;
    	int minObjectsIndex = -1;
    	int minIndex = -1;
    	
    	float[] minIP = new float[3];
    	float[] minN = new float[3];
    	float[] minMaterial = new float[3];
    	float minMaterialN = 1;
    	float bu = 0, bv = 0, bw = 1;
    	
    	float[] v = new float[3];
    	float[] l = new float[3];

    	// viewing vector at intersection point
    	v[0] = -rayVx;
    	v[1] = -rayVy;
    	v[2] = -rayVz;
    	
		normalize(v);
    	RTFile scene;
    	I_Sphere sphere;
    	T_Mesh mesh;

    	// loop over all scene objects to find the nearest intersection, that
    	// is:
    	// object with number minObjectIndex
    	// minT is the minimal factor t of the ray equation s(t)=rayE+t*rayV
    	// where the nearest intersection takes place
    	for (int objectsNumber = 0; objectsNumber < objects.size(); objectsNumber++) {
    	    scene = objects.get(objectsNumber);

    	    // object is an implicit sphere?
    	    if (scene instanceof I_Sphere) {
    		sphere = (I_Sphere) scene;

    		float t;
    		
    		// no bounding box hit? -> next object
    		if (!bboxHit(sphere, rayEx, rayEy, rayEz, rayVx, rayVy, rayVz))
    		    continue;
    		
    		// ray intersection uses quadratic equation
    		float a, b, c, d;

    		a = rayVx*rayVx+rayVy*rayVy+rayVz*rayVz;
    		b = 2 * (rayVx*(rayEx- sphere.center[0]) +rayVy*(rayEy-sphere.center[1])+rayVz*(rayEz-sphere.center[2])) ;
    		c = (rayEx*rayEx+rayEy*rayEy+rayEz*rayEz)-(rayEx*sphere.center[0]+rayEy*sphere.center[1]+rayEz*sphere.center[2])-(rayEx*sphere.center[0]+rayEy*sphere.center[1]+rayEz*sphere.center[2])+(sphere.center[0]*sphere.center[0]+sphere.center[1]*sphere.center[1]+sphere.center[2]*sphere.center[2])-sphere.radius*sphere.radius;
    		d = b*b-4*a*c;
    		// positive discriminant determines intersection
    		
    		// no intersection point? => next object
    		if (d <= 0)
    		    continue;

    		// from here: intersection takes place!

    		// calculate first intersection point with sphere along the
    		// ray
    		t = (float) ((-b-Math.sqrt(d))/(2*a));
    		

    		// already a closer intersection point? => next object
    		if (t >= minT)
    		    continue;

    		// from here: t < minT
    		// I'm the winner until now!

    		minT = t;
    		minObjectsIndex = objectsNumber;

    		// prepare everything for phong shading

    		// the intersection point
    		minIP[0] = (float) (rayEx+minT*rayVx); 
    		minIP[1] = (float) (rayEy+minT*rayVy); 
    		minIP[2] = (float) (rayEz+minT*rayVz); 

    		// the normal vector at the intersection point
    		minN[0] = minIP[0]-sphere.center[0];
    		minN[1] = minIP[1]-sphere.center[1];
    		minN[2] = minIP[2]-sphere.center[2];
    		
    		normalize(minN);
    		// the material
    		minMaterial = sphere.material;
    		minMaterialN = sphere.materialN;
    	    }
    	    else if (scene instanceof T_Mesh) {
    			mesh = (T_Mesh) scene;

    			float t;
    			float[] n;
    			float[] ip = new float[3];
    			
    			// no bounding box hit? -> next object
    			if (!bboxHit(mesh, rayEx, rayEy, rayEz, rayVx, rayVy, rayVz))
    			    continue;

    			float a, rayVn, pen;
    			float[] p1, p2, p3;
    			float[] ai = new float[3];

    			// loop over all triangles
    			for (int i = 0; i < mesh.triangles.length; i++) {
    				
    			    // get the three vertices
    			    p1 = mesh.vertices[mesh.triangles[i][0]];
    			    p2 = mesh.vertices[mesh.triangles[i][1]];
    			    p3 = mesh.vertices[mesh.triangles[i][2]];

    			    // intermediate version
    			    // calculate normal n and triangle area a
    			    //n = new float[3];
    			    //a = calculateN(n,p1,p2,p3);

    			    // fetch precalculated face areas and face normals
    			    a = mesh.triangleAreas[i];
    			    n = mesh.triangleNormals[i];
    			    
    			    rayVn = rayVx*n[0]+ rayVy*n[1]+ rayVz*n[2];

    			    // backface? => next triangle
    			    if (rayVn >= 0)
    				continue;	    

    			    // no intersection point? => next triangle
    			    if (Math.abs(rayVn) < 1E-3)
    				continue;

    			    pen = (p1[0]-rayEx)*n[0]+(p1[1]-rayEy)*n[1]+(p1[2]-rayEz)*n[2];

    			    // calculate stretch factor 
    			    t = pen/rayVn;

    			    // already a closer intersection point? => next triangle
    			    if (t >= minT)
    				continue;

    			    // the intersection point with the plane
    			    ip[0] = (float) (rayEx+t*rayVx); 
    			    ip[1] = (float) (rayEy+t*rayVy); 
    			    ip[2] = (float) (rayEz+t*rayVz); 

    			    // no intersection point with the triangle? => next
    			    // triangle
    			    if (!triangleTest(ip, p1, p2, p3, a, ai))
    				continue;

    			    // from here: t < minT and triangle intersection
    			    // I'm the winner until now!

    			    minT = t;
    			    minObjectsIndex = objectsNumber;
    			    minIndex = i;

    			    // prepare everything for shading alternatives

    			    // the intersection point
    			    minIP[0] = ip[0];
    			    minIP[1] = ip[1];
    			    minIP[2] = ip[2];    			    switch (mesh.fgp) {
    			    case 'f':
    			    case 'F':
    				// Für Flat Shading: Berechne baryzentrische Koordinaten für UV-Interpolation
    				bu = ai[0] / a;
    				bv = ai[1] / a;
    				bw = ai[2] / a;
    				break;
    			    
    			    case 'g':
    			    case 'G':
    				// remember barycentric coordinates bu, bv, bw for shading
    				bu = ai[0] / a;
    				bv = ai[1] / a;
    				bw = ai[2] / a;

    				break;
    			    case 'p':
    			    case 'P':
    				// the normal is barycentrically interpolated between
    				// the three vertices
    				bu = ai[0] / a;
    				bv = ai[1] / a;
    				bw = ai[2] / a;
    				float nTemp[] = new float[3];
    				nTemp[0] = bu * mesh.vertexNormals[mesh.triangles[minIndex][2]][0] + bv * mesh.vertexNormals[mesh.triangles[minIndex][0]][0] + bw
    						* mesh.vertexNormals[mesh.triangles[minIndex][1]][0];
    				nTemp[1] = bu * mesh.vertexNormals[mesh.triangles[minIndex][2]][1] + bv * mesh.vertexNormals[mesh.triangles[minIndex][0]][1] + bw
    						* mesh.vertexNormals[mesh.triangles[minIndex][1]][1];
    				nTemp[2] = bu * mesh.vertexNormals[mesh.triangles[minIndex][2]][2] + bv * mesh.vertexNormals[mesh.triangles[minIndex][0]][2] + bw
    						* mesh.vertexNormals[mesh.triangles[minIndex][1]][2];
    				
    				normalize(nTemp);
    				minN = nTemp;

    				// intermediate version
    				// the material is not interpolated
    				// matIndex =
    				// mesh.verticesMat[mesh.triangles[minIndex][0]];
    				// minMaterial = mesh.materials[matIndex];
    				// minMaterialN = mesh.materialsN[matIndex];    				// the material is barycentrically interpolated between
    				// the three vertex materials
    				
    				// Für OBJ-Meshes: Verwende Material des ersten Vertex (nicht interpoliert)
    				// Das ist besser für distinkte Materialien pro Face
    				int matIndex = mesh.verticesMat[mesh.triangles[minIndex][0]];
    				minMaterial = mesh.materials[matIndex];
    				minMaterialN = mesh.materialsN[matIndex];
    			    }
    			}
    		    } else
    			continue; // return null;
    	}

    	// no intersection point found => return with no result
    	if (minObjectsIndex == -1)
    	    return null;
    	
    	// light vector at the intersection point
    	l[0] = ICenter[0] - minIP[0];
    	l[1] = ICenter[1] - minIP[1];
    	l[2] = ICenter[2] - minIP[2];
    	
    	normalize(l);
    	// decide which shading model will be applied    	// implicit: only phong shading available => shade=illuminate
    	if (objects.get(minObjectsIndex) instanceof I_Sphere)
    	    return phongIlluminate(minMaterial, minMaterialN, l, minN, v, Ia, Ids);
    	
    	// triangle mesh: flat, gouraud or phong shading according to file data
    	else if ("TRIANGLE_MESH".equals(objects.get(minObjectsIndex).getHeader())) {
    	    mesh = ((T_Mesh) objects.get(minObjectsIndex));
    	    switch (mesh.fgp) {    	    case 'f':
    	    case 'F':
    		// Flat shading mit Textur-Unterstützung
    		Color baseColor = new Color(mesh.triangleColors[minIndex][0], mesh.triangleColors[minIndex][1], mesh.triangleColors[minIndex][2]);
    		
    		// Prüfe auf Textur-Unterstützung
    		if (mesh instanceof OBJ_Mesh) {
    		    OBJ_Mesh objMesh = (OBJ_Mesh) mesh;
    		    
    		    // Interpoliere UV-Koordinaten für den Schnittpunkt
    		    float[] uvCoords = new float[2];
    		    if (objMesh.interpolateUV(minIndex, bu, bv, uvCoords)) {
    			// Hole Material-Index des ersten Vertex
    			int matIndex = objMesh.verticesMat[objMesh.triangles[minIndex][0]];
    			String texturePath = objMesh.getTexturePath(matIndex);
    			
    			if (texturePath != null) {
    			    BufferedImage texture = loadTexture(texturePath);
    			    if (texture != null) {
    				// Sample Textur
    				float[] textureColor = new float[3];
    				sampleTexture(texture, uvCoords[0], uvCoords[1], textureColor);
    				
    				// Debug: Zeige UV und Textur-Info
    				if (Math.random() < 0.001) {
    				    System.out.printf("Flat: UV(%.3f,%.3f) -> RGB(%.3f,%.3f,%.3f) aus %s%n", 
    				        uvCoords[0], uvCoords[1], textureColor[0], textureColor[1], textureColor[2], texturePath);
    				}
    						// KORREKTUR: Verwende fast nur Textur mit minimaler Beleuchtung
				// um die blauen Details und Markierungen deutlich sichtbar zu machen
				float baseR = baseColor.getRed() / 255.0f;
				float baseG = baseColor.getGreen() / 255.0f;
				float baseB = baseColor.getBlue() / 255.0f;
				
				// Neue Mischung: 98% Textur, 2% Beleuchtung für maximale Textur-Sichtbarkeit
				float r = textureColor[0] * 0.98f + baseR * 0.02f;
				float g = textureColor[1] * 0.98f + baseG * 0.02f;
				float b = textureColor[2] * 0.98f + baseB * 0.02f;
    				
    				return new Color(Math.min(1.0f, r), Math.min(1.0f, g), Math.min(1.0f, b));
    			    }
    			}
    		    }
    		}
    		
    		// Fallback auf normale Flat-Schattierung
    		return baseColor;    		    case 'g':
    		    case 'G':
    			// Gouraud shading mit Textur-Unterstützung
    			float colorf[] = new float[3];
    			colorf[0] = bu * mesh.vertexColors[mesh.triangles[minIndex][2]][0] + bv * mesh.vertexColors[mesh.triangles[minIndex][0]][0] + bw
    				* mesh.vertexColors[mesh.triangles[minIndex][1]][0];
    			colorf[1] = bu * mesh.vertexColors[mesh.triangles[minIndex][2]][1] + bv * mesh.vertexColors[mesh.triangles[minIndex][0]][1] + bw
    				* mesh.vertexColors[mesh.triangles[minIndex][1]][1];
    			colorf[2] = bu * mesh.vertexColors[mesh.triangles[minIndex][2]][2] + bv * mesh.vertexColors[mesh.triangles[minIndex][0]][2] + bw
    				* mesh.vertexColors[mesh.triangles[minIndex][1]][2];

    			// Prüfe auf Textur-Unterstützung
    			if (mesh instanceof OBJ_Mesh) {
    			    OBJ_Mesh objMesh = (OBJ_Mesh) mesh;
    			    
    			    // Interpoliere UV-Koordinaten für den Schnittpunkt
    			    float[] uvCoords = new float[2];
    			    if (objMesh.interpolateUV(minIndex, bu, bv, uvCoords)) {
    				// Hole Material-Index des ersten Vertex
    				int matIndex = objMesh.verticesMat[objMesh.triangles[minIndex][0]];
    				String texturePath = objMesh.getTexturePath(matIndex);
    				
    				if (texturePath != null) {
    				    BufferedImage texture = loadTexture(texturePath);
    				    if (texture != null) {
    					// Sample Textur
    					float[] textureColor = new float[3];
    					sampleTexture(texture, uvCoords[0], uvCoords[1], textureColor);
    					
    					// Debug: Zeige UV und Textur-Info
    					if (Math.random() < 0.001) {
    					    System.out.printf("Gouraud: UV(%.3f,%.3f) -> RGB(%.3f,%.3f,%.3f) aus %s%n", 
    					        uvCoords[0], uvCoords[1], textureColor[0], textureColor[1], textureColor[2], texturePath);
    					}					// OPTIMIERT: Besseres Textur-Beleuchtungs-Verhältnis für mehr Kontrast
					colorf[0] = textureColor[0] * 0.85f + colorf[0] * 0.15f;
					colorf[1] = textureColor[1] * 0.85f + colorf[1] * 0.15f;
					colorf[2] = textureColor[2] * 0.85f + colorf[2] * 0.15f;
					
					// Kontrast-Boost für bessere Sichtbarkeit
					colorf[0] = Math.min(1.0f, colorf[0] * 1.1f);
					colorf[1] = Math.min(1.0f, colorf[1] * 1.1f);
					colorf[2] = Math.min(1.0f, colorf[2] * 1.1f);
    				    }
    				}
    			    }
    			}
    			
    			return new Color(colorf[0] < 1.0f ? colorf[0] : 1.0f, colorf[1] < 1.0f ? colorf[1] : 1.0f, colorf[2] < 1.0f ? colorf[2] : 1.0f);    		    case 'p':
    		    case 'P':
    			// Phong shading mit Textur-Unterstützung
    			Color phongColor = phongIlluminate(minMaterial, minMaterialN, l, minN, v, Ia, Ids);
    			
    			// Prüfe auf Textur-Unterstützung
    			if (mesh instanceof OBJ_Mesh) {
    			    OBJ_Mesh objMesh = (OBJ_Mesh) mesh;
    			    
    			    // Interpoliere UV-Koordinaten für den Schnittpunkt
    			    float[] uvCoords = new float[2];
    			    if (objMesh.interpolateUV(minIndex, bu, bv, uvCoords)) {
    				// Hole Material-Index des ersten Vertex
    				int matIndex = objMesh.verticesMat[objMesh.triangles[minIndex][0]];
    				String texturePath = objMesh.getTexturePath(matIndex);
    				
    				if (texturePath != null) {
    				    BufferedImage texture = loadTexture(texturePath);
    				    if (texture != null) {
    					// Sample Textur
    					float[] textureColor = new float[3];
    					sampleTexture(texture, uvCoords[0], uvCoords[1], textureColor);
    					
    					// Debug: Zeige UV und Textur-Info
    					if (Math.random() < 0.001) {
    					    System.out.printf("Phong: UV(%.3f,%.3f) -> RGB(%.3f,%.3f,%.3f) aus %s%n", 
    					        uvCoords[0], uvCoords[1], textureColor[0], textureColor[1], textureColor[2], texturePath);
    					}
    										// KORREKTUR: Mehr Textur-Dominanz für Phong-Beleuchtung
					float phongR = phongColor.getRed() / 255.0f;
					float phongG = phongColor.getGreen() / 255.0f;
					float phongB = phongColor.getBlue() / 255.0f;
							// Neue Mischung: 95% Textur, 5% Phong für maximale Textur-Sichtbarkeit
					float r = textureColor[0] * 0.95f + phongR * 0.05f;
					float g = textureColor[1] * 0.95f + phongG * 0.05f;
					float b = textureColor[2] * 0.95f + phongB * 0.05f;
    					
    					return new Color(Math.min(1.0f, r), Math.min(1.0f, g), Math.min(1.0f, b));
    				    }
    				}
    			    }
    			}
    			
    			// Fallback auf normale Phong-Schattierung
    			return phongColor;
    			// return new Color(material[3], material[4], material[5]);
    			// break;

    		    

    	    }
    	}
    	return null;
    }
          // calculate phong illumination model with material parameters material and
        // materialN, light vector l, normal vector n, viewing vector v, ambient
        // light Ia, diffuse and specular light Ids
        // return value is a new Color object
        private Color phongIlluminate(float[] material, float materialN, float[] l, float[] n, float[] v, float[] Ia, float[] Ids) {
    	float ir = 0, ig = 0, ib = 0; // reflected intensity, rgb channels
    	float[] r = new float[3]; // reflection vector
    	float ln, rv; // scalar products <l,n> and <r,v>

    	// Debug: Zeige Material-Werte (nur gelegentlich)
    	if (Math.random() < 0.0001) { // 0.01% der Zeit
    	    System.out.printf("Material verwendet: Ka(%.2f,%.2f,%.2f) Kd(%.2f,%.2f,%.2f) Ks(%.2f,%.2f,%.2f) N=%.0f%n",
    	        material[0], material[1], material[2], 
    	        material[3], material[4], material[5],
    	        material[6], material[7], material[8], materialN);
    	}

    	// <l,n>
    	ln = l[0]*n[0]+l[1]*n[1]+l[2]*n[2];

    	// ambient component, Ia*ra
    	ir += Ia[0]*material[0];
    	ig += Ia[1]*material[1];
    	ib += Ia[2]*material[2];

    	// diffuse component, Ids*rd*<l,n>
    	if (ln > 0) {
    	    ir += Ids[0] * material[3]*ln;
    	    ig += Ids[1] * material[4]*ln;
    	    ib += Ids[2] * material[5]*ln;

    	    // reflection vector r=2*<l,n>*n-l
    	    r[0] = 2*ln*n[0]-l[0];
    	    r[1] = 2*ln*n[1]-l[1];
    	    r[2] = 2*ln*n[2]-l[2];

    	    // <r,v>
    	    rv = r[0]*v[0]+r[1]*v[1]+r[2]*v[2];

    	    // specular component, Ids*rs*<r,v>^n
    	    if (rv > 0) {
    		float pow = materialN;
    		ir += Ids[0]*material[6]*Math.pow(rv,pow);
    		ig += Ids[1]*material[7]*Math.pow(rv,pow);
    		ib += Ids[2]*material[8]*Math.pow(rv,pow);
    	    }
    	}

    	// System.out.println(ir+" "+ig+" "+ib);
    	return new Color(ir>1.0?1.0f:ir,ig>1.0?1.0f:ig,ib>1.0?1.0f:ib);
        }
        
        // calculate normalized face normal fn of the triangle p1, p2 and p3
        // the return value is the area of triangle
        // CAUTION: fn is an output parameter; the referenced object will be
        // altered!
        private float calculateN(float[] fn, float[] p1, float[] p2, float[] p3) {
    	float ax, ay, az, bx, by, bz;

    	// a = Vi2-Vi1, b = Vi3-Vi1
    	ax = p2[0]-p1[0];
    	ay = p2[1]-p1[1];
    	az = p2[2]-p1[2];

    	
    	bx = p3[0]-p1[0];
    	by = p3[1]-p1[1];
    	bz = p3[2]-p1[2];
    	
    	
    	

    	// n =a x b
    	
    	fn[0] = (ay*bz-az*by);
    	fn[1] = (az*bx-ax*bz);
    	fn[2] = (ax*by-ay*bx);

    	

    	// normalize n, calculate and return area of triangle
    	return normalize(fn) / 2;
        }
        
        
        private void calculateN2(float[] fn, float[] p1, float[] p2, float[] p3) {
        	float ax, ay, az, bx, by, bz;

        	// a = Vi2-Vi1, b = Vi3-Vi1
        	ax = p2[0]-p1[0];
        	ay = p2[1]-p1[1];
        	az = p2[2]-p1[2];

        	
        	bx = p3[0]-p1[0];
        	by = p3[1]-p1[1];
        	bz = p3[2]-p1[2];
        	
        	
        	

        	// n =a x b
        	
        	fn[0] = (ay*bz-az*by);
        	fn[1] = (az*bx-ax*bz);
        	fn[2] = (ax*by-ay*bx);
            }
        
        // calculate triangle test
        // is p (the intersection point with the plane through p1, p2 and p3) inside
        // the triangle p1, p2 and p3?
        // the return value answers this question
        // a is an input parameter - the given area of the triangle p1, p2 and p3
        // ai will be computed to be the areas of the sub-triangles to allow to
        // compute barycentric coordinates of the intersection point p
        // ai[0] is associated with bu (p1p2p) across from p3
        // ai[1] is associated with bv (pp2p3) across from p1
        // ai[2] is associated with bw (p1pp3) across form p2
        // CAUTION: ai is an output parameter; the referenced object will be
        // altered!
        private boolean triangleTest(float[] p, float[] p1, float[] p2, float[] p3, float a, float ai[]) {
    	float tmp[] = new float[3];

    	ai[0] = calculateN(tmp,p1,p2,p);
    	ai[1] = calculateN(tmp,p2,p,p3);
    	ai[2] = calculateN(tmp,p,p3,p1);

    	if ((Math.abs(ai[0]+ai[1]+ai[2]-a))<1.0e-5f)
    	    return true;

    	return false;
        }

        // vector normalization
        // CAUTION: vec is an in-/output parameter; the referenced object will be
        // altered!
        private float normalize(float[] vec) {
    	float l;
    
    	l = (float) Math.sqrt((vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]));

    	vec[0]/=l;
    	vec[1]/=l;
    	vec[2]/=l;
    	
    	return l;
    }
        
        // calculate bounding box test
        // decides whether the ray s(t)=rayE+t*rayV intersects the axis aligned
        // bounding box of object -> return value true
        // six plane intersections with rectangle inside tests; if one succeeds
        // bounding box is hit
 private boolean bboxHit(RT_Object object, float rayEx, float rayEy, float rayEz, float rayVx, float rayVy, float rayVz) {
	float t;
	float ip[] = new float[3];

	// front and back
	if (Math.abs(rayVz) > 1E-3){
	    // front xy
	    t = (object.max[2] - rayEz) / rayVz;

	    ip[0] = rayEx + t * rayVx;
	    ip[1] = rayEy + t * rayVy;

	    if (ip[0] > object.min[0] && ip[0] < object.max[0] && ip[1] > object.min[1] && ip[1] < object.max[1])
		return true;

	    // back xy
	    t = (object.min[2] - rayEz) / rayVz;

	    ip[0] = rayEx + t * rayVx;
	    ip[1] = rayEy + t * rayVy;

	    if (ip[0] > object.min[0] && ip[0] < object.max[0] && ip[1] > object.min[1] && ip[1] < object.max[1])
		return true;
	}

	// left and right
	if (Math.abs(rayVx) > 1E-3) {
	    // left yz
	    t = (object.min[0] - rayEx) / rayVx;

	    ip[1] = rayEy + t * rayVy;
	    ip[2] = rayEz + t * rayVz;

	    if (ip[1] > object.min[1] && ip[1] < object.max[1] && ip[2] > object.min[2] && ip[2] < object.max[2])
		return true;

	    // right yz
	    t = (object.max[0] - rayEx) / rayVx;

	    ip[1] = rayEy + t * rayVy;
	    ip[2] = rayEz + t * rayVz;

	    if (ip[1] > object.min[1] && ip[1] < object.max[1] && ip[2] > object.min[2] && ip[2] < object.max[2])
		return true;
	}
	// top and bottom
	if (Math.abs(rayVy) > 1E-3) {
	    // top xz
	    t = (object.min[1] - rayEy) / rayVy;

	    ip[0] = rayEx + t * rayVx;
	    ip[2] = rayEz + t * rayVz;

	    if (ip[0] > object.min[0] && ip[0] < object.max[0] && ip[2] > object.min[2] && ip[2] < object.max[2])
		return true;

	    // top xz
	    t = (object.max[1] - rayEy) / rayVy;

	    ip[0] = rayEx + t * rayVx;
	    ip[2] = rayEz + t * rayVz;	    if (ip[0] > object.min[0] && ip[0] < object.max[0] && ip[2] > object.min[2] && ip[2] < object.max[2])
		return true;
	}
	return false;
    }
    
    // precalulation of triangle normals and triangle areas        
    private void prepareMeshData() {
    	RTFile scene;

    	System.out.println("Vorverarbeitung 1 läuft");

    	float[] p1, p2, p3;

    	for (int objectsNumber = 0; objectsNumber < objects.size(); objectsNumber++) {
    	    scene = objects.get(objectsNumber);

    	    if ("TRIANGLE_MESH".equals(scene.getHeader())) {
    		T_Mesh mesh = (T_Mesh) scene;    		// init memory
    		mesh.triangleNormals = new float[mesh.triangles.length][3];
    		mesh.triangleAreas = new float[mesh.triangles.length];
    		
    		for (int i = 0; i < mesh.triangles.length; i++) {
    			p1 = mesh.vertices[mesh.triangles[i][0]];
    			p2 = mesh.vertices[mesh.triangles[i][1]];
    			p3 = mesh.vertices[mesh.triangles[i][2]];

    		    // calculate and store triangle normal n and triangle area a
    		    mesh.triangleAreas[i] = calculateN(mesh.triangleNormals[i],p1,p2,p3);
    		}}
    	}
    	System.out.println("Vorverarbeitung 1 beendet");
    }
    
    private void precalculateMeshDataShading() {
    	RTFile scene;

    	System.out.println("Vorverarbeitung 2 läuft");

    	float rayEx, rayEy, rayEz, rayVx, rayVy, rayVz;
    	double rayVn;
    	Color color;
    	float x, y, z;
    	float[] ip = new float[3];
    	float[] n = new float[3];
    	float[] l = new float[3];
    	float[] v = new float[3];
    	float[] material;
    	float materialN;
    	int matIndex;

    	for (int objectsNumber = 0; objectsNumber < objects.size(); objectsNumber++) {
    	    scene = objects.get(objectsNumber);

    	    if ("TRIANGLE_MESH".equals(scene.getHeader())) {
    		T_Mesh mesh = (T_Mesh) scene;

    		switch (mesh.fgp) {
    		case 'f':
    		case 'F':
    		    // for flat-shading: initialize and calculate triangle
    		    // colors
    		    mesh.triangleColors = new float[mesh.triangles.length][3];

    		    rayEx = 0.0f;
    		    rayEy = 0.0f;
    		    rayEz = 0.0f;

    		    // loop over all triangles
    		    for (int i = 0; i < mesh.triangles.length; i++) {
    			// the intersection point is the first vertex of the
    			// triangle
    			ip = mesh.vertices[mesh.triangles[i][0]];

    			// the material is the material of the first triangle
    			// point
    			matIndex = mesh.verticesMat[mesh.triangles[i][0]];
    			material = mesh.materials[matIndex];
    			materialN = mesh.materialsN[matIndex];

    			// x, y, z: view coordinates are intersection point
    			x = ip[0];
    			y = ip[1];
    			z = ip[2];

    			// ray vector
    			rayVx = x - rayEx;
    			rayVy = y - rayEy;
    			rayVz = z - rayEz;

    			// fetch precalculated face normal
    			n = mesh.triangleNormals[i];

    			rayVn = rayVx * n[0] + rayVy * n[1] + rayVz * n[2];

    			// backface? => next triangle
    			if (rayVn >= 0)
    			    continue;

        			// light vector at the intersection point
        			l[0] = ICenter[0] - ip[0];
        			l[1] = ICenter[1] - ip[1];
        			l[2] = ICenter[2] - ip[2];
        			normalize(l);

        			// viewing vector at intersection point
        			v[0] = -rayVx;
        			v[1] = -rayVy;
        			v[2] = -rayVz;
        			normalize(v);

        			// illuminate
        			color = phongIlluminate(material, materialN, l, n, v, Ia, Ids);

        			// write color to triangle
        			mesh.triangleColors[i][0] = (float) color.getRed()/255;
        			mesh.triangleColors[i][1] = (float) color.getGreen()/255;
        			mesh.triangleColors[i][2] = (float) color.getBlue()/255;
        		    }
        		    break;

        		case 'p':
        		case 'P':
        		case 'g':
        		case 'G':
        		    // initialize and calculate averaged vertex normals
        		    mesh.vertexNormals = new float[mesh.vertices.length][3];

        		    // loop over all vertices to initialize
        		    for (int j = 0; j < mesh.vertices.length; j++)
        			for (int k = 0; k < 3; k++)
        			    mesh.vertexNormals[j][k] = 0.0f;

        		    // loop over all faces to contribute
        		    for (int i = 0; i < mesh.triangles.length; i++)
        			for (int j = 0; j < 3; j++)
        			    for (int k = 0; k < 3; k++)
        			    	mesh.vertexNormals[mesh.triangles[i][j]][k] += mesh.triangleNormals[i][k];


        		    // loop over all vertices to normalize
        		    for (int j = 0; j < mesh.vertices.length; j++) {
        		    	normalize(mesh.vertexNormals[j]);
        		    }

        		    // these are all preparations for phong shading
        		    if (mesh.fgp == 'p' || mesh.fgp == 'P')
        			break;

        		    // for gouraud-shading: initialize and calculate vertex
        		    // colors
        		    mesh.vertexColors = new float [mesh.vertices.length][3];

        		    rayEx = 0.0f;
        		    rayEy = 0.0f;
        		    rayEz = 0.0f;

        		    // loop over all vertices
        		    for (int i = 0; i < mesh.vertices.length; i++) {
        			// the intersection point is the vertex
        			ip = mesh.vertices[i];

        			// the material is the material of the vertex
        			matIndex = mesh.verticesMat[i];
        			material = mesh.materials[matIndex];
        			materialN = mesh.materialsN[matIndex];

        			// x, y, z: view coordinates are intersection point
        			x = ip[0];
        			y = ip[1];
        			z = ip[2];

        			// ray vector
        			rayVx = x - rayEx;
        			rayVy = y - rayEy;
        			rayVz = z - rayEz;

        			// fetch precalculated vertex normal
        			n = mesh.vertexNormals[i];

        			rayVn = rayVx * n[0] + rayVy * n[1] + rayVz * n[2];

        			// backface? => next vertex
        			if (rayVn >= 0)
        			    continue;

        			// light vector at the intersection point
        			l[0] = ICenter[0] - ip[0];
        			l[1] = ICenter[1] - ip[1];
        			l[2] = ICenter[2] - ip[2];
        			normalize(l);

        			// viewing vector at intersection point
        			v[0] = -rayVx;
        			v[1] = -rayVy;
        			v[2] = -rayVz;
        			normalize(v);

        			// illuminate
        			color = phongIlluminate(material, materialN, l, n, v, Ia, Ids);

        			// write color to vertex
        			mesh.vertexColors[i][0] = (float) color.getRed()/255;
        			mesh.vertexColors[i][1] = (float) color.getGreen()/255;
        			mesh.vertexColors[i][2] = (float) color.getBlue()/255;
        		    }
        		}        	    }
        	}
        	System.out.println("Vorverarbeitung 2 beendet");
    }

    /**
     * Transformiert ein OBJ-Mesh: Rotiert um 180° um Y-Achse und verschiebt es an die gewünschte Position
     * @param mesh Das zu transformierende Mesh
     * @param offsetX X-Verschiebung
     * @param offsetY Y-Verschiebung  
     * @param offsetZ Z-Verschiebung
     */
    private void transformModel(OBJ_Mesh mesh, float offsetX, float offsetY, float offsetZ) {
        for (int i = 0; i < mesh.vertices.length; i++) {
            // Zuerst Rotation um 180 Grad um Y-Achse (cos(180°) = -1, sin(180°) = 0)
            float originalX = mesh.vertices[i][0];
            float originalZ = mesh.vertices[i][2];
            mesh.vertices[i][0] = -originalX;  // X-Koordinate invertieren
            mesh.vertices[i][2] = -originalZ;  // Z-Koordinate invertieren
            
            // Dann Verschiebung an gewünschte Position
            mesh.vertices[i][0] += offsetX;  // X-Verschiebung
            mesh.vertices[i][1] += offsetY - 1.0f;  // Y-Verschiebung (mit Standard-Offset)
            mesh.vertices[i][2] += offsetZ;  // Z-Verschiebung
        }
    }    public static void main(String[] args) {
	Raytracer05 rt = new Raytracer05();

	rt.doRayTrace();
    }
    
    /**
     * Lädt eine Textur und speichert sie im Cache
     * @param texturePath Pfad zur Textur-Datei (relativ zum data-Verzeichnis)
     * @return Das BufferedImage oder null bei Fehlern
     */
    private BufferedImage loadTexture(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            return null;
        }
        
        // Prüfe Cache zuerst
        if (textureCache.containsKey(texturePath)) {
            return textureCache.get(texturePath);
        }
        
        try {
            // Vollständiger Pfad zur Textur-Datei
            File textureFile = new File("d:/FTCG/Raytrace05/Raytrace05/data/" + texturePath);
            if (!textureFile.exists()) {
                System.err.println("WARNUNG: Textur-Datei nicht gefunden: " + textureFile.getAbsolutePath());
                return null;
            }
            
            System.out.println("Lade Textur: " + textureFile.getAbsolutePath());
            BufferedImage image = ImageIO.read(textureFile);
            
            if (image == null) {
                System.err.println("FEHLER: Kann Textur nicht laden: " + textureFile.getAbsolutePath());
                return null;
            }
            
            // Im Cache speichern
            textureCache.put(texturePath, image);
            System.out.println("Textur erfolgreich geladen: " + image.getWidth() + "x" + image.getHeight() + " - " + texturePath);
            
            return image;
            
        } catch (IOException e) {
            System.err.println("FEHLER beim Laden der Textur " + texturePath + ": " + e.getMessage());
            return null;
        }
    }    /**
     * Sampelt eine Farbe aus einer Textur an den gegebenen UV-Koordinaten
     * @param image Das BufferedImage
     * @param u UV-Koordinate U (0.0 bis 1.0)
     * @param v UV-Koordinate V (0.0 bis 1.0)
     * @param result Array für das Ergebnis [r, g, b] (0.0 bis 1.0)
     */
    private void sampleTexture(BufferedImage image, float u, float v, float[] result) {
        if (image == null) {
            result[0] = result[1] = result[2] = 1.0f; // Weiss als Fallback
            return;
        }
        
        // DEBUG: Prüfe ungültige UV-Werte
        if (Float.isNaN(u) || Float.isNaN(v) || Float.isInfinite(u) || Float.isInfinite(v)) {
            System.err.printf("FEHLER: Ungültige UV-Koordinaten: u=%.3f, v=%.3f%n", u, v);
            result[0] = 1.0f; result[1] = 0.0f; result[2] = 0.0f; // Rot für Debugging
            return;
        }
        
        // Sichere UV-Wrapping
        while (u < 0.0f) u += 1.0f;
        while (u > 1.0f) u -= 1.0f;
        while (v < 0.0f) v += 1.0f;
        while (v > 1.0f) v -= 1.0f;        // Standard UV-Transformation: V-Koordinate spiegeln (OpenGL zu DirectX Style)
        // Dies ist die häufigste Korrektur für UV-Mapping zwischen verschiedenen Systemen
        v = 1.0f - v;
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Sichere Pixel-Koordinaten berechnen
        int pixelX = Math.round(u * (width - 1));
        int pixelY = Math.round(v * (height - 1));
        
        // Bounds-Check
        pixelX = Math.max(0, Math.min(pixelX, width - 1));
        pixelY = Math.max(0, Math.min(pixelY, height - 1));
        
        // Hole Pixel und extrahiere Farbe
        int pixel = image.getRGB(pixelX, pixelY);
        result[0] = ((pixel >> 16) & 0xFF) / 255.0f; // Rot
        result[1] = ((pixel >> 8) & 0xFF) / 255.0f;  // Grün
        result[2] = (pixel & 0xFF) / 255.0f;         // Blau
          // Debug: Zeige UV und Textur-Info (reduziert)
        if (Math.random() < 0.0001) {
            System.out.printf("UV-Sample: (%.3f,%.3f) -> RGB(%.3f,%.3f,%.3f) [%dx%d]%n", 
                u, v, result[0], result[1], result[2], width, height);
        }
          // WARNUNG bei konstanten Grauwerten (reduziert für bessere Performance)
        if (Math.abs(result[0] - result[1]) < 0.01f && Math.abs(result[1] - result[2]) < 0.01f && 
            result[0] > 0.7f && result[0] < 0.9f) {
            if (Math.random() < 0.001) { // Sehr selten anzeigen
                System.err.printf("INFO: Metallische Textur: RGB(%.3f,%.3f,%.3f) bei UV(%.3f,%.3f)%n", 
                    result[0], result[1], result[2], u, v);
            }
        }
    }
}
