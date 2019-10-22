import ipcapture.*;
IPCapture cam;
int camW = 640;
int camH = 360;

import gab.opencv.*;
OpenCV opencv;

import java.awt.Rectangle;
int minRectW = 10;
int minRectH = 10;

boolean showCam = false;
boolean showContours = false;
boolean showBounding = false;
boolean showCenterC = false;
boolean showCenterS = true;

import java.util.Iterator;
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

void setup() {
	size(1280,720,P3D);

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
	cam.start("localhost:8000/stream.mjpeg", "praystation", "star13star");

	opencv = new OpenCV(this, camW, camH);
	opencv.startBackgroundSubtraction(5, 3, 0.5); // history, nMixtures, backgroundRatio
}

void draw() {
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
			stroke(#FF3300);
			noFill();
			c.draw();
		}

		// bounding boxes
		Rectangle r = c.getBoundingBox();

		if (r.width>minRectW && r.height>minRectH) {
			if (showBounding) {
				strokeWeight(2);
				stroke(#00CC00);
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
				fill(#0000CC);
				ellipse(cX, cY, 5, 5);
			}

			// box center STAGE
			if (showCenterS) {
				strokeWeight(0);
				noStroke();
				fill(#000000);
				ellipse(sX, sY, 5, 5);
			}
		}

	}

	noTint();
	image(curColor, 0, 0, width, 20);

	surface.setTitle("FPS = " + (int)frameRate );
}

void addParticleSystem(float x, float y) {
	ParticleSystem ps = new ParticleSystem( x, y, numParticles, particleMin, particleMax );
	pm.addSystem(ps);
}

void keyPressed() {
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
