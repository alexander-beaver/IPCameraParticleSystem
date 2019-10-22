import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ipcapture.*; 
import gab.opencv.*; 
import java.awt.Rectangle; 
import java.util.Iterator; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class build extends PApplet {


IPCapture cam;
int camW = 640;
int camH = 360;


OpenCV opencv;


int minRectW = 10;
int minRectH = 10;

boolean showCam = false;
boolean showContours = false;
boolean showBounding = false;
boolean showCenterC = false;
boolean showCenterS = true;


ParticleManager pm;
int particleMin = 20;
int particleMax = 60;
int numParticles = 3;

String pathData = "../../data/";
PImage clr1, clr2, clr3, clr4, clr5, clr6, curColor;

// String[] texNames = {"tex1.jpg", "tex2.jpg", "tex3.jpg"};
// String[] texNames = {"tex1.png", "tex2.png", "tex3.png"};
String[] texNames = {"particle_00.png", "particle_01.png", "particle_02.png", "particle_03.png", "particle_04.png"};
int texNamesLen = texNames.length;
PImage[] texLoaded = new PImage[texNamesLen];

public void setup() {
	

	clr1 = loadImage(pathData + "color_001.png");
	clr2 = loadImage(pathData + "color_002.png");
	clr3 = loadImage(pathData + "color_003.png");
	clr4 = loadImage(pathData + "color_004.png");
	clr5 = loadImage(pathData + "color_005.png");
	clr6 = loadImage(pathData + "color_006.png");

	textureMode(NORMAL);
	for (int i = 0; i < texNamesLen; ++i) {
		texLoaded[i] = loadImage(pathData + texNames[i]);
	}

	curColor = clr4;

	pm = new ParticleManager();
	
	cam = new IPCapture(this);
	cam.start("http://169.254.196.29/axis-cgi/mjpg/video.cgi", "praystation", "star13star");

	opencv = new OpenCV(this, camW, camH);
	opencv.startBackgroundSubtraction(5, 3, 0.5f); // history, nMixtures, backgroundRatio
}

public void draw() {
	background(255);

	if(cam.isAvailable()) cam.read();
	if(showCam) image(cam, 0, 0);

	opencv.loadImage(cam);
	opencv.updateBackground();
	opencv.dilate();
	opencv.erode();

	// I'm always running to kill Systems when they are dead
	pm.run();

	for (Contour c : opencv.findContours()) {
		if(showContours) {
			strokeWeight(1);
			stroke(0xffFF3300);
			noFill();
			c.draw();
		}

		// bounding boxes
		Rectangle r = c.getBoundingBox();

		if (r.width>minRectW && r.height>minRectH) {
			if (showBounding) {
				strokeWeight(2);
				stroke(0xff00CC00);
				noFill();
				rect(r.x, r.y, r.width, r.height);
			}

			// box center
			float cX = r.x + (r.width/2);
			float cY = r.y + (r.height/2);
			float sX = map(cX, 0, camW, 0, width);
			float sY = map(cY, 0, camH, 0, height);

			// an openCV point exists lets create a ParticleSystem
			addParticleSystem(sX, sY);

			// box center CAM
			if (showCenterC) {
				strokeWeight(0);
				noStroke();
				fill(0xff0000CC);
				ellipse(cX, cY, 5, 5);
			}

			// box center STAGE
			if (showCenterS) {
				strokeWeight(0);
				noStroke();
				fill(0xff000000);
				ellipse(sX, sY, 5, 5);
			}
		}

	}

	noTint();
	image(curColor, 0, 0, width, 20);

	surface.setTitle("FPS = " + (int)frameRate );
}

public void addParticleSystem(float x, float y) {
	ParticleSystem ps = new ParticleSystem( x, y, numParticles, particleMin, particleMax );
	pm.addSystem(ps);
}

public void keyPressed() {
	switch (key) {
		case '1' : showCam = !showCam; break;
		case '2' : showContours = !showContours; break;
		case '3' : showBounding = !showBounding; break;
		case '4' : showCenterC = !showCenterC; break;
		case '5' : showCenterS = !showCenterS; break;

		case 'q' : curColor = clr1; break;
		case 'w' : curColor = clr2; break;
		case 'e' : curColor = clr3; break;
		case 'r' : curColor = clr4; break;
		case 't' : curColor = clr5; break;
		case 'y' : curColor = clr6; break;
	}
}









class Particle {
	float minS = 0; // death position
	float maxS; // random size
	float deathRate = 1.0f;

	int xOffsetMax = 50;
	int yOffsetMax = 50;
	PVector location;

	int myColor;

	PImage tex;

	PVector velocity;
	PVector acceleration;
	
	float   partSpeed = 5.0f;
	float   partForce = 0.25f;
	float   partSep   = 5.0f;
	float   partAli   = -3.0f;
	float   partCos   = 1.0f;

	// float   partSpeed = 10.0;
	// float   partForce = 0.05;
	// float   partSep   = 1.0;
	// float   partAli   = 5.0;
	// float   partCos   = -3.0;

	// float   partSpeed = 10.0;
	// float   partForce = 0.01;
	// float   partSep   = 3.0;
	// float   partAli   = 1.0;
	// float   partCos   = 7.0;

	Particle(int i, float x, float y, int particleSize) {
		maxS = particleSize;

		int xOffset = (int)random( -(xOffsetMax/2), (xOffsetMax/2) );
		int yOffset = (int)random( -(yOffsetMax/2), (yOffsetMax/2) );

		acceleration = new PVector(0,0);
		velocity = new PVector(random(-1,1),random(-1,1));
		location = new PVector(x+xOffset, y+yOffset);

		myColor = curColor.get(Math.round( (int)random(curColor.width) ), 1 );

		tex = texLoaded[ (int)random(texNamesLen) ];
	}

	public void run(ArrayList<Particle> p) {
		flock(p);
		update();
		render();
		maxS -= deathRate;
		if(maxS<=minS) maxS = minS;
	}

	public void applyForce(PVector force) {
		acceleration.add(force);
	}

	public void flock(ArrayList<Particle> p) {
		PVector sep = separate(p);
		PVector ali = align(p);
		PVector coh = cohesion(p);

		sep.mult(partSep);
		ali.mult(partAli);
		coh.mult(partCos);

		applyForce(sep);
		applyForce(ali);
		applyForce(coh);
	}

	// method to update location

	public void update() {
		velocity.add(acceleration);
		velocity.limit(partSpeed);
		location.add(velocity);
		acceleration.mult(0);
	}

	public void render() {
		pushMatrix();
			translate(location.x, location.y, 0);
			scale(maxS);

			strokeWeight(0);
			noStroke();
			fill(255);
			tint(myColor);

			beginShape(QUAD);
				texture(tex);
				vertex( -(0.5f), -(0.5f), 0, 0,0);
				vertex(  (0.5f), -(0.5f), 0, 1,0);
				vertex(  (0.5f),  (0.5f), 0, 1,1);
				vertex( -(0.5f),  (0.5f), 0, 0,1);
			endShape(CLOSE);

		popMatrix();
	}

	public boolean isDead() {
		return (maxS<=minS);
	}

	// A method that calculates and applies a steering force towards a target
	// STEER = DESIRED MINUS VELOCITY

	public PVector seek(PVector target) {
		PVector desired = PVector.sub(target,location);
		desired.normalize();
		desired.mult(partSpeed);
		PVector steer = PVector.sub(desired,velocity);
		steer.limit(partForce);
		return steer;
	}

	// Separation
	// Method checks for nearby particles and steers away

	public PVector separate (ArrayList<Particle> p) {
		float desiredseparation = 25.0f;
		PVector steer = new PVector(0,0,0);
		int count = 0;
		for (Particle other : p) {
			float d = PVector.dist(location,other.location);
			if ((d > 0) && (d < desiredseparation)) {
				PVector diff = PVector.sub(location,other.location);
				diff.normalize();
				diff.div(d);
				steer.add(diff);
				count++;
			}
		}
		if (count > 0) {
			steer.div((float)count);
		}
		if (steer.mag() > 0) {
			steer.normalize();
			steer.mult(partSpeed);
			steer.sub(velocity);
			steer.limit(partForce);
		}
		return steer;
	}

	// Alignment
	// For every nearby particle in the system, calculate the average velocity

	public PVector align (ArrayList<Particle> p) {
		float neighbordist = 50;
		PVector sum = new PVector(0,0);
		int count = 0;
		for (Particle other : p) {
			float d = PVector.dist(location,other.location);
			if ((d > 0) && (d < neighbordist)) {
				sum.add(other.velocity);
				count++;
			}
		}
		if (count > 0) {
			sum.div((float)count);
			sum.normalize();
			sum.mult(partSpeed);
			PVector steer = PVector.sub(sum,velocity);
			steer.limit(partForce);
			return steer;
		} else {
			return new PVector(0,0);
		}
	}

	// Cohesion
	// For the average location (i.e. center) of all nearby particles, calculate steering vector towards that location

	public PVector cohesion (ArrayList<Particle> p) {
		float neighbordist = 50;
		PVector sum = new PVector(0,0);
		int count = 0;
		for (Particle other : p) {
			float d = PVector.dist(location,other.location);
			if ((d > 0) && (d < neighbordist)) {
				sum.add(other.location);
				count++;
			}
		}
		if (count > 0) {
			sum.div(count);
			return seek(sum);
		} else {
			return new PVector(0,0);
		}
	}
}
class ParticleManager {
	ArrayList<ParticleSystem> ps;

	// runs once, like setup does, constructor
	ParticleManager() {
		ps = new ArrayList<ParticleSystem>();
	}

	// I'm going to handle the creation of ParticleSystems
	public void addSystem(ParticleSystem system) {
		ps.add(system);
	}

	// I'm always running to kill Systems when they are dead
	public void run() {
		Iterator<ParticleSystem> it = ps.iterator();

		while(it.hasNext()) {
			ParticleSystem temp = it.next();
			temp.run();

			if(temp.dead()) it.remove();
		}
	}
}








class ParticleSystem {
	ArrayList<Particle> p;

	ParticleSystem(float x, float y, int numP, int particleMin, int particleMax) {
		p = new ArrayList<Particle>();

		for (int i = 0; i < numP; ++i) {
			p.add( new Particle(i, x, y, (int)random(particleMin, particleMax)));
		}
	}

	public void run() {
		Iterator<Particle> it = p.iterator();

		while (it.hasNext()) {
			Particle temp = it.next();
			temp.run(p);
			if(temp.isDead()) it.remove();
		}
	}

	public boolean dead() {
		return p.isEmpty();
	}
}






  public void settings() { 	size(1280,720,P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "build" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
