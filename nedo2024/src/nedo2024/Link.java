package nedo2024;

import java.util.Objects;

public class Link {
	private Coordinate startPoint;
	private Coordinate endPoint;

	public Link(Coordinate start, Coordinate end) {
		startPoint = start;
		endPoint = end;
	}

	public Coordinate getStart() {
		return startPoint;
	}

	public Coordinate getEnd() {
		return endPoint;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Link link = (Link) obj;
		return Objects.equals(startPoint, link.startPoint) && Objects.equals(endPoint, link.endPoint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startPoint, endPoint);
	}

	@Override
	public String toString() {
		return "Link{" + "startPoint=" + startPoint + ", endPoint=" + endPoint + '}';
	}

}
