package nedo2024;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author sei
 *
 */
public class Node implements Comparable<Node> {

	private TreeSet<Node> parents = new TreeSet<Node>();
	private TreeSet<Node> children = new TreeSet<Node>();
	private final Coordinate coordinate;// physical position
	private final int id;// determined based on the physical position.
	private Tree tree;
	private boolean isLogicalOdd;// Starting from one.
	ConstraintChecker checker = null;

	// start from one.
	Node(int x, int y, Tree tree) {
		coordinate = new Coordinate(x, y);
		this.tree = tree;

		int yNum = tree.getYNum();
		id = (x - 1) * yNum + y;

		checker = tree.getConstraintChecker();
	}

	public boolean isNear(Node node) {
		int maxLength = checker.getMaxLengthForConst4();
		if (maxLength == 0) {// no constraint;
			return true;
		}
		int distance = getPhysicalDistance(node);
		if (distance <= maxLength) {
			return true;
		} else {
			return false;
		}
	}

	public Set<Node> getNearNodes() {
		int maxLength = checker.getMaxLengthForConst4();
		Set<Node> nearNodes = new HashSet<Node>();
		nearNodes.addAll(tree);
		nearNodes.remove(this);
		if (maxLength == 0) {// no constraint;
			return nearNodes;
		} else {
			for (Node node : tree) {
				if (getPhysicalDistance(node) > maxLength) {
					nearNodes.remove(node);
				}
			}
			return nearNodes;
		}
	}

	public Set<Node> getFarNodesForConst4() {
		int maxLength = checker.getMaxLengthForConst4();
		Set<Node> farNodes = new HashSet<Node>();
		farNodes.addAll(tree);
		farNodes.remove(this);
		if (maxLength == 0) {// no constraint;
			farNodes.clear();
			return farNodes;
		} else {
			for (Node node : tree) {
				if (getPhysicalDistance(node) <= maxLength) {
					farNodes.remove(node);
				}
			}
			return farNodes;
		}
	}

	public Set<Node> getFarNodesForConst0() {
		int minLength = checker.getMin_distanceForConst0();
		Set<Node> nearNodes = new HashSet<Node>();
		nearNodes.addAll(tree);
		nearNodes.remove(this);

		for (Node node : tree) {
			if (getPhysicalDistance(node) < minLength) {
				nearNodes.remove(node);
			}
		}
		return nearNodes;
	}

	// !!!これを使う場合、treeの格納順序を修正の必要あり!!!
	// public void setPhysicalPosition(int x, int y) {
	// coordinate = new Coordinate(x, y);
	// int yNum = tree.getYNum();
	// id = (x - 1) * yNum + y;
	// }

	// Start from one.
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "" + id;
	}

	/**
	 * If there is no relationship, return -1;
	 * 
	 * @param node
	 * @return
	 */
	public int getLogicalDistance(Node node) {
		return findShortestPath(this, node);
	}

	/**
	 * If there is no relationship, return empty set;
	 */
	public Set<Integer> getLogicalDistances(Node node) {
		return findPaths(this, node);
	}

	private static int findShortestPath(Node start, Node end) {
		if (start == end)
			return 0;

		// Queue for BFS
		Queue<Node> queue = new LinkedList<>();
		// Set to keep track of visited nodes
		Set<Node> visited = new HashSet<>();

		queue.add(start);
		visited.add(start);

		int distance = 0;

		while (!queue.isEmpty()) {
			int size = queue.size();
			distance++;

			for (int i = 0; i < size; i++) {
				Node current = queue.poll();

				// Explore all neighbors (children and parents)
				Set<Node> neighbors = new HashSet<>(current.getChildren());
				neighbors.addAll(current.getParents());

				for (Node neighbor : neighbors) {
					if (neighbor == end) {
						return distance;
					}

					if (!visited.contains(neighbor)) {
						visited.add(neighbor);
						queue.add(neighbor);
					}
				}
			}
		}

		// Return -1 if no path exists
		return -1;
	}

	public static Set<Integer> findPaths(Node start, Node end) {
		Set<Integer> distances = new HashSet<>();
		// DFS を呼び出す際に空の visited 集合を渡す
		dfs(start, end, 0, distances, new HashSet<>());
		return distances;
	}

	private static void dfs(Node current, Node end, int dist, Set<Integer> distances, Set<Node> visited) {
		// 到達チェック
		if (current.equals(end)) {
			distances.add(dist);
			return;
		}

		// このノードを「訪問済み」に
		visited.add(current);

		// 子方向だけでなく、親方向にも移動したいなら
		// Set<Node> neighbors = new HashSet<>(current.getChildren());
		// neighbors.addAll(current.getParents());
		// for (Node neighbor : neighbors) { ... }

		for (Node child : current.getChildren()) {
			if (!visited.contains(child)) {
				dfs(child, end, dist + 1, distances, visited);
			}
		}
		// 探索を戻るときに訪問フラグを解除
		visited.remove(current);
	}

	public boolean isLogicalOdd() {
		return isLogicalOdd;
	}

	public void setLogicalOdd(boolean logicalOdd, int testCount) throws IOException {
		if (testCount == tree.size() + 1) {
			System.err.println("error code : iu5388");
			VisualMain.visualize_logical(tree);
			System.exit(-1);
		}
		isLogicalOdd = logicalOdd;
		testCount++;
		for (Node node : getChildren()) {
			node.setLogicalOdd(!logicalOdd, testCount);
		}
	}

	public boolean isPhysicalOdd() {
		if (getY() % 2 == 1) {
			return true;
		} else {
			return false;
		}
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public int getX() {
		return coordinate.x;
	}

	public int getY() {
		return coordinate.y;
	}

	private void addChild(Node node) {
		children.add(node);
		node.addParentInternal(this);
	}

	private void removeChild(Node node) {
		children.remove(node);
		node.removeParentInternal(this);
	}

	public boolean removeChild(Node node, boolean isCheck) throws IOException {
		if (node == this) {
			return false;
		}
		removeChild(node);
		if (isCheck) {
			if (checker.allCheckIgnoringLonlyNodes(tree, false)) {
				return true;
			} else {
				addChild(node);
				return false;
			}
		}
		return true;

	}

	public void removeChildren(Set<Node> nodes) {
		for (Node node : nodes) {
			removeChild(node);
		}
	}

	private void addParentInternal(Node node) {
		parents.add(node);
	}

	private void removeParentInternal(Node node) {
		parents.remove(node);
	}

	public boolean addChild(Node node, boolean isConstraintCheck, boolean isBeforeCheckOnly) throws IOException {
		if (this == node) {
			return false;
		}

		if (isConstraintCheck && checker.getConstraints()[3]) {
			boolean isOkTemp = ConstraintChecker.checkConst3forAddingNode(this, node);
			if (isOkTemp) {
				addChild(node);
			} else {
				return false;
			}
		} else {
			addChild(node);
		}

		if (isConstraintCheck) {
			if (checker.allCheckIgnoringLonlyNodes(tree, isBeforeCheckOnly)) {
				return true;
			} else {
				removeChild(node);
				return false;
			}
		}

		return true;
	}

	public boolean isLonlyNode() {
		if (getParents().size() == 0 && getChildren().size() == 0) {
			return true;
		}
		return false;
	}

	public TreeSet<Node> getParents() {
		return parents;
	}

	public TreeSet<Node> getChildren() {
		return children;
	}

	public Set<Node> getDescendants() {
		Set<Node> nodes = new HashSet<Node>();
		nodes.addAll(children);
		for (Node node : children) {
			node.getDescendantsInternal(nodes);
		}
		return nodes;
	}

	private void getDescendantsInternal(Set<Node> nodes) {

		int size = nodes.size();
		nodes.addAll(children);
		int size2 = nodes.size();

		if (size != size2) {
			for (Node node : children) {
				node.getDescendantsInternal(nodes);
			}
		}
	}

	public Set<Node> getAncestors() {
		Set<Node> nodes = new HashSet<Node>();
		nodes.addAll(parents);
		for (Node node : parents) {
			node.getAncestorsInternal(nodes);
		}
		return nodes;
	}

	private void getAncestorsInternal(Set<Node> nodes) {

		int size = nodes.size();
		nodes.addAll(parents);
		int size2 = nodes.size();

		if (size != size2) {
			for (Node node : parents) {
				node.getAncestorsInternal(nodes);
			}
		}
	}

	public boolean isLeaf() {
		if (children.size() == 0) {
			return true;
		}
		return false;
	}

	public boolean isInExist(int x1, int x2, int y1, int y2) {
		if (x1 <= getX() && getX() <= x2 && y1 <= getY() && getY() <= y2) {
			return true;
		} else {
			return false;
		}
	}

	public void removeChildrenAll() {
		Set<Node> childCopy = new HashSet<Node>();
		childCopy.addAll(getChildren());
		for (Node child : childCopy) {
			removeChild(child);
		}
	}

	/**
	 * @param node
	 * @return Manhattan distance
	 */
	public int getPhysicalDistance(Node node) {
		int horizontalDistance = Math.abs(this.getX() - node.getX());
		int verticalDistance = Math.abs(this.getY() - node.getY());
		return horizontalDistance + verticalDistance;
	}

	public TreeSet<Integer> getLogicalDistancesFromInlets() {
		TreeSet<Integer> distances = new TreeSet<Integer>();
		for (Node inlet : tree.getInlets()) {
			distances.addAll(inlet.getLogicalDistances(this));
		}
		return distances;
	}

	public boolean equalOnlyStructureDescendants(Node nodeOther) {
		String sigThis = computeStructureSignature(this, true);
		String sigOther = computeStructureSignature(nodeOther, true);
		return sigThis.equals(sigOther);
	}

	public boolean equalOnlyStructureAncestors(Node nodeOther) {
		String sigThis = computeStructureSignature(this, false);
		String sigOther = computeStructureSignature(nodeOther, false);
		return sigThis.equals(sigOther);
	}

	private String computeStructureSignature(Node node, boolean isDescendants) {
		Map<Node, String> sig = new HashMap<>();
		computeStructureSignatureRecursive(node, sig, isDescendants);
		return sig.get(node);
	}

	private void computeStructureSignatureRecursive(Node node, Map<Node, String> sig, boolean isDescendants) {
		if (sig.containsKey(node)) {
			return; // すでに処理済み
		}

		List<String> partialTreeSigs = new ArrayList<>();

		if (isDescendants) {
			for (Node child : node.getChildren()) {
				computeStructureSignatureRecursive(child, sig, isDescendants);
				partialTreeSigs.add(sig.get(child));
			}
		} else {
			for (Node parent : node.getParents()) {
				computeStructureSignatureRecursive(parent, sig, isDescendants);
				partialTreeSigs.add(sig.get(parent));
			}
		}

		Collections.sort(partialTreeSigs);
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		partialTreeSigs.forEach(sb::append);
		sb.append(")");

		sig.put(node, sb.toString());
	}

	// このNodeの子孫の中でleaf nodesを得る
	public Set<Node> getLeafNodes() {
		Set<Node> leafNodes = new HashSet<Node>();
		for (Node n : this.getDescendants()) {
			if (n.isLeaf()) {
				leafNodes.add(n);
			}
		}
		return leafNodes;
	}

	// このNodeの祖先の中でroot nodesを得る
	public Set<Node> getRootNodes() {
		Set<Node> rootNodes = new HashSet<Node>();
		for (Node n : this.getAncestors()) {
			if (n.isRoot()) {
				rootNodes.add(n);
			}
		}
		return rootNodes;
	}

	public boolean isRoot() {
		if (getParents().size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		return this.getY() == other.getY() && this.getX() == other.getX();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getY(), getX());
	}

	@Override
	public int compareTo(Node other) {
		String thisKey = this.getY() + "," + this.getX();
		String otherKey = other.getY() + "," + other.getX();
		return thisKey.compareTo(otherKey);
	}

	public Set<Integer> getParentNodeIds() {
		Set<Integer> ids = new HashSet<Integer>();
		for (Node parent : getParents()) {
			ids.add(parent.getId());
		}
		return ids;
	}

	public Set<Integer> getChildNodeIds() {
		Set<Integer> ids = new HashSet<Integer>();
		for (Node child : getChildren()) {
			ids.add(child.getId());
		}
		return ids;
	}

}
