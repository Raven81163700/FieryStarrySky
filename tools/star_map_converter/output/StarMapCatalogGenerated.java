package com.aurora.world;

public final class StarMapCatalogGenerated {

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

    public static final class Constellation {
        public int id;
        public String name;
        public String controller;
        public String description;
        public String color;
        public int[] systemIds;
    }

    public static final class Domain {
        public int id;
        public String name;
        public String controller;
        public String description;
        public String color;
        public int[] constellationIds;
    }

    private static final StarSystem[] SYSTEMS = new StarSystem[] {
        makeSystem(1, "\u7ef4\u62c9\u5e0c\u9996\u661f", 696, 408, 1),
        makeSystem(2, "\u65b0\u90fd", 648, 432, 3),
        makeSystem(3, "\u83b1\u7eb3\u83b1\u591a", 720, 456, 3),
        makeSystem(4, "\u52a0\u52a0\u6797\u683c\u52d2", 816, 408, 1),
        makeSystem(5, "Glof-07", 600, 408, 3),
        makeSystem(6, "Peter-11", 696, 480, 3),
        makeSystem(7, "Alpha-21", 600, 480, 2),
        makeSystem(8, "Beta-06", 552, 336, 2),
        makeSystem(9, "Hotel-15", 696, 336, 3),
        makeSystem(10, "\u6012\u6d77", 600, 528, 3),
        makeSystem(11, "\u795e\u519c", 672, 504, 3),
        makeSystem(12, "\u8c37\u795e", 768, 504, 3),
        makeSystem(13, "\u5343\u6237", 552, 456, 3),
        makeSystem(14, "\u5229\u57c3\u90a3\u591a", 672, 552, 2),
        makeSystem(15, "\u5361\u52d2\u83b1\u7279", 744, 360, 3),
        makeSystem(16, "\u901a\u9014IV", 792, 312, 3),
        makeSystem(17, "\u5361\u624e\u90a3\u591a", 768, 456, 3),
        makeSystem(18, "\u5229\u57c3", 888, 432, 3),
        makeSystem(19, "\u5854\u6797", 840, 480, 2),
        makeSystem(20, "Zeta-7", 912, 312, 2),
        makeSystem(21, "\u8428\u5170", 840, 168, 2),
        makeSystem(22, "\u5df4\u5fb7", 1032, 216, 2),
        makeSystem(23, "\u5854\u514b", 936, 384, 2),
        makeSystem(24, "\u4e4c\u5170\u73ed\u514b", 720, 216, 2),
        makeSystem(25, "\u592a\u5b50\u6e2f", 552, 240, 3),
        makeSystem(26, "\u5e15\u514b\u7279\u514b", 984, 96, 2),
        makeSystem(28, "\u5361\u6587\u8bfa\u592b-\u5f00\u53d1\u533a", 1080, 120, 3),
        makeSystem(29, "\u68ee\u79d1", 960, 240, 2),
        makeSystem(30, "\u7edd\u5f84", 1128, 192, 2),
        makeSystem(31, "F8U-Z1", 888, 72, 2),
        makeSystem(32, "\u52aa\u514b\u6cb3\u53e3", 816, 120, 2),
        makeSystem(33, "K7-I9P", 768, 48, 2),
        makeSystem(34, "L0-X6D", 696, 96, 2),
        makeSystem(35, "GU8-08", 576, 168, 1),
        makeSystem(36, "L9K-OJ", 600, 72, 1),
        makeSystem(37, "\u5965\u6c57", 768, 240, 2),
        makeSystem(38, "\u963f\u5c14\u7279\u65af\u5170\u514b", 1056, 312, 2),
        makeSystem(39, "\u71d5", 984, 456, 3),
        makeSystem(40, "\u83b1\u897f\u8c37\u5730", 1032, 384, 2),
        makeSystem(41, "\u8c37\u53e3", 672, 48, 1),
        makeSystem(42, "L9-U8T", 480, 24, 1),
        makeSystem(43, "N8-6YU", 456, 144, 1),
        makeSystem(44, "8T-67U", 360, 24, 1),
        makeSystem(45, "LK7-OO", 312, 144, 1),
        makeSystem(46, "MM-O7U", 528, 72, 1),
        makeSystem(47, "7P-I7Y", 264, 48, 1),
        makeSystem(48, "HO-78G", 384, 240, 1),
        makeSystem(49, "KP8-F5", 240, 312, 1),
        makeSystem(50, "7L-9UI", 336, 336, 1),
        makeSystem(51, "\u83ab\u5c3c\u4e9a\u70b9", 24, 24, 1),
        makeSystem(52, "Z5O-UU", 144, 96, 1),
        makeSystem(53, "9J-L8B", 168, 216, 1),
        makeSystem(54, "L8-00T", 216, 120, 1),
        makeSystem(55, "CU9-L6", 264, 240, 1)
    };

    private static final SystemLink[] LINKS = new SystemLink[] {
        makeLink(2, 1, 2, 1),
        makeLink(3, 1, 2, 1),
        makeLink(4, 1, 2, 1),
        makeLink(4, 18, 2, 1),
        makeLink(6, 5, 2, 1),
        makeLink(7, 5, 2, 1),
        makeLink(8, 7, 2, 1),
        makeLink(9, 1, 2, 1),
        makeLink(9, 5, 2, 1),
        makeLink(9, 25, 2, 1),
        makeLink(10, 2, 2, 1),
        makeLink(10, 11, 2, 1),
        makeLink(11, 12, 2, 1),
        makeLink(13, 10, 2, 1),
        makeLink(14, 10, 2, 1),
        makeLink(14, 12, 2, 1),
        makeLink(15, 4, 2, 1),
        makeLink(17, 15, 2, 1),
        makeLink(18, 16, 2, 1),
        makeLink(18, 19, 2, 1),
        makeLink(19, 17, 2, 1),
        makeLink(20, 16, 2, 1),
        makeLink(21, 20, 2, 1),
        makeLink(21, 22, 2, 1),
        makeLink(21, 24, 2, 1),
        makeLink(22, 26, 2, 1),
        makeLink(23, 22, 2, 1),
        makeLink(25, 24, 2, 1),
        makeLink(28, 26, 2, 1),
        makeLink(29, 28, 2, 1),
        makeLink(30, 28, 2, 1),
        makeLink(31, 30, 2, 1),
        makeLink(32, 31, 2, 1),
        makeLink(33, 31, 2, 1),
        makeLink(34, 32, 2, 1),
        makeLink(34, 33, 2, 1),
        makeLink(35, 34, 2, 1),
        makeLink(36, 35, 2, 1),
        makeLink(38, 37, 2, 1),
        makeLink(38, 39, 2, 1),
        makeLink(39, 18, 2, 1),
        makeLink(40, 23, 2, 1),
        makeLink(40, 37, 2, 1),
        makeLink(41, 34, 2, 1),
        makeLink(41, 36, 2, 1),
        makeLink(42, 41, 2, 1),
        makeLink(43, 42, 2, 1),
        makeLink(44, 42, 2, 1),
        makeLink(44, 43, 2, 1),
        makeLink(45, 43, 2, 1),
        makeLink(46, 43, 2, 1),
        makeLink(46, 45, 2, 1),
        makeLink(47, 46, 2, 1),
        makeLink(48, 47, 2, 1),
        makeLink(49, 48, 2, 1),
        makeLink(49, 50, 2, 1),
        makeLink(50, 47, 2, 1),
        makeLink(52, 51, 2, 1),
        makeLink(54, 52, 2, 1),
        makeLink(54, 53, 2, 1),
        makeLink(55, 48, 2, 1),
        makeLink(55, 53, 2, 1)
    };

    private static final Body[] BODIES = new Body[] {

    };

    private static final Constellation[] CONSTELLATIONS = new Constellation[] {
        makeConstellation(1, "\u6307\u660e\u5ea7", "\u7ef4\u62c9\u5e0c\u8054\u90a6\u5408\u4f17\u56fd", "", "#ffb74d", new int[] {4, 1, 2, 3}),
        makeConstellation(2, "\u65b0\u89c6\u91ce\u5927\u73af\u5ea7", "\u7ef4\u62c9\u5e0c\u8054\u90a6\u5408\u4f17\u56fd", "", "#995511", new int[] {9, 5, 8, 7, 6}),
        makeConstellation(3, "\u4e91\u73af", "", "", "#550882", new int[] {12, 14, 11, 10, 13}),
        makeConstellation(4, "\u65b0\u5730", "\u7ef4\u62c9\u5e0c\u8054\u90a6\u5408\u4f17\u56fd", "", "#551200", new int[] {15, 17, 19, 18, 16}),
        makeConstellation(6, "\u5361\u6587\u8bfa\u592b", "\u7ef4\u62c9\u5e0c\u8054\u90a6\u5408\u4f17\u56fd", "", "#fff000", new int[] {30, 28, 29, 26}),
        makeConstellation(7, "\u5bc2\u5be5\u8c37\u53e3", "", "", "#010101", new int[] {31, 32, 33, 34, 36, 35}),
        makeConstellation(8, "\u98d8\u5e26", "", "", "#88C4EC", new int[] {40, 39, 38, 37}),
        makeConstellation(9, "\u5361\u6587\u901a\u8def", "", "", "#8537E7", new int[] {23, 20, 22, 21, 24, 25}),
        makeConstellation(10, "\u5bc2\u5be5\u8c37\u5730", "", "", "#770011", new int[] {44, 45, 43, 42, 46, 41}),
        makeConstellation(11, "\u7edd\u5730", "", "", "#8537E7", new int[] {47, 48, 50, 49}),
        makeConstellation(12, "\u5bc2\u5be5\u5c0f\u5f84", "", "", "#87230D", new int[] {55, 54, 53, 52, 51})
    };

    private static final Domain[] DOMAINS = new Domain[] {
        makeDomain(1, "\u7ef4\u62c9\u5e0c\u5185\u73af", "\u7ef4\u62c9\u5e0c\u8054\u90a6\u5408\u4f17\u56fd", "", "rgba(10, 10, 255, 0.5)", new int[] {4, 1, 2, 3}),
        makeDomain(2, "\u8499\u7279\u91cc\u79d1\u5927\u73af\u5e26", "", "", "rgba(72, 196, 77, 0.5)", new int[] {9, 6, 8}),
        makeDomain(3, "\u5bc2\u5be5\u8c37", "", "", "rgba(255, 100, 100, 0.5)", new int[] {12, 11, 10, 7})
    };

    private StarMapCatalogGenerated() {}

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
            if (BODIES[i].systemId == systemId) count++;
        }
        Body[] out = new Body[count];
        int p = 0;
        for (i = 0; i < BODIES.length; i++) {
            if (BODIES[i].systemId == systemId) out[p++] = BODIES[i];
        }
        return out;
    }

    public static Constellation[] constellations() {
        return CONSTELLATIONS;
    }

    public static Domain[] domains() {
        return DOMAINS;
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

    private static Constellation makeConstellation(int id, String name, String controller, String description, String color, int[] systemIds) {
        Constellation c = new Constellation();
        c.id = id;
        c.name = name;
        c.controller = controller;
        c.description = description;
        c.color = color;
        c.systemIds = systemIds;
        return c;
    }

    private static Domain makeDomain(int id, String name, String controller, String description, String color, int[] constellationIds) {
        Domain d = new Domain();
        d.id = id;
        d.name = name;
        d.controller = controller;
        d.description = description;
        d.color = color;
        d.constellationIds = constellationIds;
        return d;
    }
}
