package com.aurora.world;

public final class StarMapCatalog {

	public static final class StarSystem {
		public int id;
		public String name;
		public int x;
		public int y;
		public int security;
	}

	public static final class SystemLink {
		public int fromId;
		public int toId;
		public int linkType;
		public int cost;
	}

	public static final class Body {
		public int id;
		public int systemId;
		public int bodyType;
		public String name;
		public int x;
		public int y;
		public int primary;
	}

	private static final StarSystem[] SYSTEMS = new StarSystem[] {
		makeSystem(1,  "Sol",       120, 120, 3),
		makeSystem(2,  "Alpha",     300, 90,  2),
		makeSystem(3,  "Cygnus",    520, 130, 1),
		makeSystem(4,  "Orion",     260, 280, 2),
		makeSystem(5,  "Perseus",   480, 300, 1),
		makeSystem(6,  "Vega",      700, 110, 2),
		makeSystem(7,  "Sirius",    860, 170, 2),
		makeSystem(8,  "Deneb",     1040, 120, 1),
		makeSystem(9,  "Rigel",     760, 320, 1),
		makeSystem(10, "Antares",   980, 350, 1),
		makeSystem(11, "Polaris",   1220, 150, 3),
		makeSystem(12, "Arcturus",  1320, 300, 2),
		makeSystem(13, "Altair",    560, 470, 2),
		makeSystem(14, "Bellatrix", 900, 500, 1)
	};

	private static final SystemLink[] LINKS = new SystemLink[] {
		makeLink(1, 2, 1, 5),
		makeLink(2, 3, 1, 6),
		makeLink(1, 4, 1, 4),
		makeLink(4, 5, 1, 5),
		makeLink(2, 4, 1, 3),
		makeLink(3, 6, 1, 4),
		makeLink(6, 7, 1, 3),
		makeLink(7, 8, 1, 3),
		makeLink(6, 9, 1, 4),
		makeLink(9, 10, 1, 4),
		makeLink(8, 11, 1, 5),
		makeLink(11, 12, 1, 4),
		makeLink(5, 13, 1, 4),
		makeLink(9, 13, 1, 5),
		makeLink(10, 14, 1, 3),
		makeLink(13, 14, 1, 5)
	};

	private static final Body[] BODIES = new Body[] {
		makeBody(1001, 1, 1, "Sol-A", 58, 44, 0),
		makeBody(1002, 1, 2, "Sol-I", 28, 58, 1001),
		makeBody(1003, 1, 4, "Sol-Belt", 64, 70, 1001),
		makeBody(1004, 1, 3, "Sol-Station", 90, 54, 1001),

		makeBody(2001, 2, 1, "Alpha-A", 60, 40, 0),
		makeBody(2002, 2, 2, "Alpha-I", 34, 60, 2001),
		makeBody(2003, 2, 4, "Alpha-Belt", 66, 72, 2001),
		makeBody(2004, 2, 3, "Alpha-Station", 92, 50, 2001),

		makeBody(3001, 3, 1, "Cygnus-A", 56, 44, 0),
		makeBody(3002, 3, 2, "Cygnus-I", 24, 52, 3001),
		makeBody(3003, 3, 4, "Cygnus-Belt", 64, 68, 3001),
		makeBody(3004, 3, 3, "Cygnus-Station", 88, 56, 3001),

		makeBody(4001, 4, 1, "Orion-A", 58, 40, 0),
		makeBody(4002, 4, 2, "Orion-I", 30, 62, 4001),
		makeBody(4003, 4, 4, "Orion-Belt", 66, 70, 4001),
		makeBody(4004, 4, 3, "Orion-Station", 92, 50, 4001),

		makeBody(5001, 5, 1, "Perseus-A", 62, 42, 0),
		makeBody(5002, 5, 2, "Perseus-I", 30, 58, 5001),
		makeBody(5003, 5, 4, "Perseus-Belt", 66, 72, 5001),
		makeBody(5004, 5, 3, "Perseus-Station", 94, 54, 5001),

		makeBody(6001, 6, 1, "Vega-A", 58, 40, 0),
		makeBody(6002, 6, 2, "Vega-I", 32, 56, 6001),
		makeBody(6003, 6, 4, "Vega-Belt", 66, 72, 6001),
		makeBody(6004, 6, 3, "Vega-Station", 90, 52, 6001),

		makeBody(7001, 7, 1, "Sirius-A", 60, 44, 0),
		makeBody(7002, 7, 2, "Sirius-I", 28, 58, 7001),
		makeBody(7003, 7, 4, "Sirius-Belt", 66, 70, 7001),
		makeBody(7004, 7, 3, "Sirius-Station", 92, 52, 7001),

		makeBody(8001, 8, 1, "Deneb-A", 58, 40, 0),
		makeBody(8002, 8, 2, "Deneb-I", 30, 56, 8001),
		makeBody(8003, 8, 4, "Deneb-Belt", 64, 70, 8001),
		makeBody(8004, 8, 3, "Deneb-Station", 90, 52, 8001),

		makeBody(9001, 9, 1, "Rigel-A", 58, 42, 0),
		makeBody(9002, 9, 2, "Rigel-I", 30, 58, 9001),
		makeBody(9003, 9, 4, "Rigel-Belt", 64, 70, 9001),
		makeBody(9004, 9, 3, "Rigel-Station", 92, 54, 9001),

		makeBody(10001, 10, 1, "Antares-A", 60, 42, 0),
		makeBody(10002, 10, 2, "Antares-I", 28, 56, 10001),
		makeBody(10003, 10, 4, "Antares-Belt", 66, 70, 10001),
		makeBody(10004, 10, 3, "Antares-Station", 92, 54, 10001),

		makeBody(11001, 11, 1, "Polaris-A", 58, 40, 0),
		makeBody(11002, 11, 2, "Polaris-I", 30, 56, 11001),
		makeBody(11003, 11, 4, "Polaris-Belt", 64, 68, 11001),
		makeBody(11004, 11, 3, "Polaris-Station", 90, 52, 11001),

		makeBody(12001, 12, 1, "Arcturus-A", 60, 44, 0),
		makeBody(12002, 12, 2, "Arcturus-I", 30, 58, 12001),
		makeBody(12003, 12, 4, "Arcturus-Belt", 66, 72, 12001),
		makeBody(12004, 12, 3, "Arcturus-Station", 92, 54, 12001),

		makeBody(13001, 13, 1, "Altair-A", 58, 42, 0),
		makeBody(13002, 13, 2, "Altair-I", 30, 58, 13001),
		makeBody(13003, 13, 4, "Altair-Belt", 66, 70, 13001),
		makeBody(13004, 13, 3, "Altair-Station", 92, 54, 13001),

		makeBody(14001, 14, 1, "Bellatrix-A", 60, 44, 0),
		makeBody(14002, 14, 2, "Bellatrix-I", 30, 58, 14001),
		makeBody(14003, 14, 4, "Bellatrix-Belt", 66, 72, 14001),
		makeBody(14004, 14, 3, "Bellatrix-Station", 92, 54, 14001)
	};

	private StarMapCatalog() {
	}

	public static StarSystem[] systems() {
		return SYSTEMS;
	}

	public static SystemLink[] links() {
		return LINKS;
	}

	public static Body[] bodiesBySystem(int systemId) {
		int count = 0;
		int i;
		for (i = 0; i < BODIES.length; i++) {
			Body b = BODIES[i];
			if (b.systemId == systemId) {
				count++;
			}
		}
		Body[] out = new Body[count];
		int p = 0;
		for (i = 0; i < BODIES.length; i++) {
			Body b = BODIES[i];
			if (b.systemId == systemId) {
				out[p++] = b;
			}
		}
		return out;
	}

	private static StarSystem makeSystem(int id, String name, int x, int y, int security) {
		StarSystem s = new StarSystem();
		s.id = id;
		s.name = name;
		s.x = x;
		s.y = y;
		s.security = security;
		return s;
	}

	private static SystemLink makeLink(int fromId, int toId, int linkType, int cost) {
		SystemLink l = new SystemLink();
		l.fromId = fromId;
		l.toId = toId;
		l.linkType = linkType;
		l.cost = cost;
		return l;
	}

	private static Body makeBody(int id, int systemId, int bodyType, String name, int x, int y, int primary) {
		Body b = new Body();
		b.id = id;
		b.systemId = systemId;
		b.bodyType = bodyType;
		b.name = name;
		b.x = x;
		b.y = y;
		b.primary = primary;
		return b;
	}
}
