class ParticleSystem {
	ArrayList<Particle> p;

	ParticleSystem(float x, float y, int numP, int particleMin, int particleMax) {
		p = new ArrayList<Particle>();

		for (int i = 0; i < numP; ++i) {
			p.add( new Particle(i, x, y, (int)random(particleMin, particleMax)));
		}
	}

	void run() {
		Iterator<Particle> it = p.iterator();

		while (it.hasNext()) {
			Particle temp = it.next();
			temp.run(p);
			if(temp.isDead()) it.remove();
		}
	}

	boolean dead() {
		return p.isEmpty();
	}
}






