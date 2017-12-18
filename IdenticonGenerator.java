import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class IdenticonGenerator {
	
	static final int POLY_SIZE = 100;
	
	private static String hexColor(int r, int g, int b) {
		return String.format("#%02x%02x%02x", r, g, b);
	}
	
	static class Polygon {
		List<Point> points;
		String c;
		int position;
		
		Polygon(String c) {
			this.c = c;
			this.position = 0;
			points = new ArrayList<>();
		}
		
		Polygon(String c, int pos) {
			this.c = c;
			this.position = pos;
			this.points = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return String.format("<polygon points=\"%s\" style=\"%s\"/>",
					points.stream().map(Point::toString)
					.collect(Collectors.joining(" ")), "fill:" + c);
		}
	}
	
	static class Point {
		int x;
		int y;
		
		Point (int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString() {
			return x + "," + y;
		}
	}
	
	static class Square {
		List<Polygon> polys;
		String color;
		int sz;
		
		Square (Polygon poly, String color, int sz) {
			polys = new ArrayList<>();
			polys.add(poly);
			this.color = color;
			this.sz = sz;
		}
		
		Square (String color, int sz) {
			polys = new ArrayList<>();
			this.color = color;
			this.sz = sz;
		}
		
		@Override
		public String toString() {
			StringBuilder res = new StringBuilder();
			res.append(String.format("<svg height=\"%d\" width=\"%d\" style=\"background:%s\">\n",
					sz * POLY_SIZE, sz * POLY_SIZE, color));
			res.append(polys.stream().map(Polygon::toString)
					.collect(Collectors.joining("\n")));
			res.append("\n</svg>");
			return res.toString();
		}
	}
	
	private static Square merge(Square sq, int size) {
		int len = size * POLY_SIZE;
		Square toAdd = new Square(sq.color, size);
		for (Polygon poly: sq.polys) {
			toAdd.polys.add(poly);
			Polygon right = new Polygon(poly.c, 1);
			for (Point p: poly.points) {
				right.points.add(new Point(len - p.x, p.y));
			}
			Polygon down = new Polygon(poly.c, 2);
			for (Point p: poly.points) {
				down.points.add(new Point(p.x, len - p.y));
			}
			Polygon downright = new Polygon(poly.c, 3);
			for (Point p: poly.points) {
				downright.points.add(new Point(len - p.x, len - p.y));
			}
			toAdd.polys.add(right);
			toAdd.polys.add(down);
			toAdd.polys.add(downright);
		}
		return toAdd;
	}
	
	private static Square rec(int size, Random r, boolean first) {
		if (size == 1) {
			return new Square(randomPoly(r, 0), randomColor(r), 1);
		}
		if (size == 2 && first) {
			return merge(new Square(randomPoly(r, 0), randomColor(r), 1), 2);
		} else if (size == 2) {
			Square res = new Square(randomColor(r), 2);
			res.polys.add(randomPoly(r, 0));
			res.polys.add(randomPoly(r, 1));
			res.polys.add(randomPoly(r, 2));
			res.polys.add(randomPoly(r, 3));
			return res;
		}
		return merge(rec((size + 1) / 2, r, false), size);
	}
	
	private static Polygon randomPoly(Random r, int pos) {
		Polygon res = new Polygon(randomColor(r));
		int sz = r.nextBoolean() ? 3: 4;
		int dx = ((pos & 1) == 1 ? POLY_SIZE: 0);
		int dy = (pos > 1 ? POLY_SIZE: 0);
		for (int i = 0; i < sz; ++i) {
			switch (i % 4) {
				case 0:
					res.points.add(new Point(dx + r.nextInt(POLY_SIZE + 1), dy));
					break;
				case 1:
					res.points.add(new Point(dx + POLY_SIZE, dy + r.nextInt(POLY_SIZE + 1)));
					break;
				case 2:
					res.points.add(new Point(dx + r.nextInt(POLY_SIZE + 1), dy + POLY_SIZE));
					break;
				default:
					res.points.add(new Point(dx, dy + r.nextInt(POLY_SIZE + 1)));
			}
		}
		return res;
	}

	private static String randomColor(Random r) {
		return hexColor(r.nextInt(256), r.nextInt(256), r.nextInt(256));
	}

	public static String genIdenticon(int size, String source) {
		Random r = new Random(source.hashCode());
		Square sq = rec(size, r, true);
		return sq.toString();
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		PrintWriter out = new PrintWriter("out.html");
		Random r = new Random();
		for (int i = 0; i < 100; ++i) {
			out.print(genIdenticon(r.nextInt(4) + 1, String.valueOf(i)));
			out.print("</br>\n");
		}
		out.close();
	}

}
