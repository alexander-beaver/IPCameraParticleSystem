class Particle {
	float minS = 0; // death position
	float maxS; // random size
	float deathRate = 1.0;

	int xOffsetMax = 50;
	int yOffsetMax = 50;
	PVector location;

	color myColor;

	PImage tex;

	PVector velocity;
	PVector acceleration;
	
	float   partSpeed = 5.0;
	float   partForce = 0.25;
	float   partSep   = 5.0;
	float   partAli   = -3.0;
	float   partCos   = 1.0;

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

	void run(ArrayList<Particle> p) {
		flock(p);
		update();
		render();
		maxS -= deathRate;
		if(maxS<=minS) maxS = minS;
	}

	void applyForce(PVector force) {
		acceleration.add(force);
	}

	void flock(ArrayList<Particle> p) {
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

	void update() {
		velocity.add(acceleration);
		velocity.limit(partSpeed);
		location.add(velocity);
		acceleration.mult(0);
	}

	void render() {
		pushMatrix();
			translate(location.x, location.y, 0);
			scale(maxS);

			strokeWeight(0);
			noStroke();
			fill(255);
			tint(myColor);

			beginShape(QUAD);
				texture(tex);
				vertex( -(0.5), -(0.5), 0, 0,0);
				vertex(  (0.5), -(0.5), 0, 1,0);
				vertex(  (0.5),  (0.5), 0, 1,1);
				vertex( -(0.5),  (0.5), 0, 0,1);
			endShape(CLOSE);

		popMatrix();
	}

	boolean isDead() {
		return (maxS<=minS);
	}

	// A method that calculates and applies a steering force towards a target
	// STEER = DESIRED MINUS VELOCITY

	PVector seek(PVector target) {
		PVector desired = PVector.sub(target,location);
		desired.normalize();
		desired.mult(partSpeed);
		PVector steer = PVector.sub(desired,velocity);
		steer.limit(partForce);
		return steer;
	}

	// Separation
	// Method checks for nearby particles and steers away

	PVector separate (ArrayList<Particle> p) {
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

	PVector align (ArrayList<Particle> p) {
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

	PVector cohesion (ArrayList<Particle> p) {
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