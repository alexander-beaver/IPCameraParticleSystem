class ParticleManager {
	ArrayList<ParticleSystem> ps;

	// runs once, like setup does, constructor
	ParticleManager() {
		ps = new ArrayList<ParticleSystem>();
	}

	// I'm going to handle the creation of ParticleSystems
	void addSystem(ParticleSystem system) {
		ps.add(system);
	}

	// I'm always running to kill Systems when they are dead
	void run() {
		Iterator<ParticleSystem> it = ps.iterator();

		while(it.hasNext()) {
			ParticleSystem temp = it.next();
			temp.run();

			if(temp.dead()) it.remove();
		}
	}
}








