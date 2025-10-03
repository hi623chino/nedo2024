package nedo2024;

import java.util.Objects;

public class Coordinate {
	int x = 0;// horizontal (left to right)
	int y = 0;// vertical (up to down)

	Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		String s = "[" + x + "," + y + "]";
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Coordinate that = (Coordinate) obj;
		return x == that.x && y == that.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
}
