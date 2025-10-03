package nedo2024;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConstraintChecker {

	private int maxLengthForConst4;
	private int max_numForConst9_merge;
	private int max_numForConst9_split;
	private double maxDiffLengthForConst10;
	private int min_distanceForConst0;
	private boolean constraints[];
	private boolean noremovingconstraints[];

	// どのリンクを追加してもconstraints違反が解消されないものをtrue。***DO NOT CHANGE THESE VALUES!!!***
	private final static boolean beforeCheckConstraints[] = { false, false, true, false, true, false, true, true, true,
			true, false };

	public ConstraintChecker(int maxLengthForConst4, int max_numForConst9_merge, int max_numForConst9_split,
			double maxDiffLengthForConst10, int min_distanceForConst0, boolean constraints[]) {
		this.maxLengthForConst4 = maxLengthForConst4;
		this.max_numForConst9_merge = max_numForConst9_merge;
		this.max_numForConst9_split = max_numForConst9_split;
		this.maxDiffLengthForConst10 = maxDiffLengthForConst10;
		this.min_distanceForConst0 = min_distanceForConst0;

		this.constraints = constraints;

		noremovingconstraints = new boolean[constraints.length];
		for (int i = 0; i < constraints.length; i++) {
			noremovingconstraints[i] = constraints[i] & beforeCheckConstraints[i];
		}
	}

	public int getMin_distanceForConst0() {
		return min_distanceForConst0;
	}

	public int getMaxLengthForConst4() {
		return maxLengthForConst4;
	}

	public int getMaxNumForConst9_merge() {
		return max_numForConst9_merge;
	}

	public int getMaxNumForConst9_split() {
		return max_numForConst9_split;
	}

	public boolean[] getConstraints() {
		return constraints;
	}

	public boolean isConst(Tree tree, int id, boolean ignoreLonlyNodes) throws IOException {
		if (id != 2) {// cycle detection
			tree.reCalculateLogicalTurn();
		}

		switch (id) {
		case 0:
			return isConst0(tree);
		case 1:
			if (!ignoreLonlyNodes) {
				return isConst1(tree);
			} else {
				return true;
			}
		case 2:
			return isConst2(tree);
		case 3:
			return isConst3(tree);
		case 4:
			return isConst4(tree);
		case 5:
			return isConst5(tree, ignoreLonlyNodes);
		case 6:
			return isConst6(tree);
		case 7:
			return isConst7(tree, ignoreLonlyNodes);
		case 8:
			return isConst8(tree);
		case 9:
			return isConst9(tree, ignoreLonlyNodes);
		case 10:
			return isConst10(tree);
		default:
			System.err.println("Undefined constraint!! in ConstraintChecker#isConst");
			return true;
		}

	}

	public boolean allCheckIgnoringLonlyNodes(Tree tree, boolean isBeforeCheckOnly) throws IOException {

		boolean targetConstraints[] = null;
		if (isBeforeCheckOnly) {
			targetConstraints = noremovingconstraints;
		} else {
			targetConstraints = constraints;
		}

		if (targetConstraints[2]) {
			if (!isConst(tree, 2, false)) {
				return false;
			}
		}
		if (targetConstraints[3]) {
			if (!isConst(tree, 3, false)) {
				return false;
			}
		}
		if (targetConstraints[4]) {
			if (!isConst(tree, 4, false)) {
				return false;
			}
		}
		if (targetConstraints[5]) {
			if (!isConst(tree, 5, true)) {
				return false;
			}
		}
		if (targetConstraints[6]) {
			if (!isConst(tree, 6, false)) {
				return false;
			}
		}
		if (targetConstraints[7]) {
			if (!isConst(tree, 7, true)) {
				return false;
			}
		}
		if (targetConstraints[8]) {
			if (!isConst(tree, 8, false)) {
				return false;
			}
		}
		if (targetConstraints[9]) {
			if (!isConst(tree, 9, true)) {
				return false;
			}
		}
		if (targetConstraints[10]) {
			if (!isConst(tree, 10, false)) {
				return false;
			}
		}
		if (targetConstraints[0]) {
			if (!isConst(tree, 0, false)) {
				return false;
			}
		}
		return true;
	}

	public boolean allCheck(Tree tree, boolean isBeforeCheckOnly) throws IOException {
		boolean targetConstraints[] = null;
		if (isBeforeCheckOnly) {
			targetConstraints = noremovingconstraints;
		} else {
			targetConstraints = constraints;
		}

		if (targetConstraints[1]) {
			if (!isConst(tree, 1, false)) {
				return false;
			}
		}
		if (targetConstraints[2]) {
			if (!isConst(tree, 2, false)) {
				return false;
			}
		}
		if (targetConstraints[3]) {
			if (!isConst(tree, 3, false)) {
				return false;
			}
		}
		if (targetConstraints[4]) {
			if (!isConst(tree, 4, false)) {
				return false;
			}
		}
		if (targetConstraints[5]) {
			if (!isConst(tree, 5, false)) {
				return false;
			}
		}
		if (targetConstraints[6]) {
			if (!isConst(tree, 6, false)) {
				return false;
			}
		}
		if (targetConstraints[7]) {
			if (!isConst(tree, 7, false)) {
				return false;
			}
		}
		if (targetConstraints[8]) {
			if (!isConst(tree, 8, false)) {
				return false;
			}
		}
		if (targetConstraints[9]) {
			if (!isConst(tree, 9, false)) {
				return false;
			}
		}
		if (targetConstraints[10]) {
			if (!isConst(tree, 10, false)) {
				return false;
			}
		}
		if (targetConstraints[0]) {
			if (!isConst(tree, 0, false)) {
				return false;
			}
		}
		return true;
	}

	// Ensure that no unconnected piping occurs.
	public boolean isConst1(Tree tree) {

		if (tree.getLonlyNodes().size() >= 1) {
			return false;
		}
		// TreeSet<Node> inlets = tree.getInlets();
		// for (Node node : tree) {
		// if (node.getParents().size() == 0 && node.getChildren().size() == 0) {
		// return false;
		// }
		// Set<Node> ancestors = node.getAncestors();
		// boolean isAncestorsIncludedInInlets = false;
		// for (Node ancestor : ancestors) {
		// if (inlets.contains(ancestor)) {
		// isAncestorsIncludedInInlets = true;
		// }
		// }
		//
		// if (!isAncestorsIncludedInInlets) {
		// return false;
		// }
		// }
		return true;
	}

	// Ensure no loop
	public boolean isConst2(Tree tree) {
		for (Node node : tree) {
			Set<Node> ancestors = node.getAncestors();
			if (ancestors.contains(node)) {
				return false;
			}
		}
		return true;
	}

	// Ensure not merging before or after the piping
	public boolean isConst3(Tree tree) {
		for (Node node : tree) {
			Set<Integer> allDistances = new HashSet<Integer>();
			for (Node inlet : tree.getInlets()) {
				Set<Integer> distances = inlet.getLogicalDistances(node);
				allDistances.addAll(distances);
			}
			if (!isAllOddOrEven(allDistances)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isAllOddOrEven(Set<Integer> set) {
		// 空集合に対しては allMatch は true を返すので、そのまま要件を満たします
		return set.stream().allMatch(n -> n % 2 == 0) || set.stream().allMatch(n -> n % 2 != 0);
	}

	// Ensure the length of each pipe is less than or equal to maxLength. "Not
	// considering shifted by half a unit"
	public boolean isConst4(Tree tree) {
		for (Node node : tree) {
			for (Node child : node.getChildren()) {
				int distance = node.getPhysicalDistance(child);
				if (distance > maxLengthForConst4) {
					return false;
				}
			}
		}
		return true;
	}

	// Ensure the direction of each inlet and the direction of each outlet is not
	// the same
	public boolean isConst5(Tree tree, boolean ignoreLonlyNodes) {

		// leafLodeで、inletからのlogicalDistanceが整合しているかどうかのチェックをまず実施。
		Set<Integer> allDistances = new HashSet<Integer>();
		for (Node outlet : tree.getOutlets()) {
			for (Node inlet : tree.getInlets()) {

				if (ignoreLonlyNodes) {
					if (inlet == outlet) {
						continue;
					}
				}

				Set<Integer> distances = inlet.getLogicalDistances(outlet);
				allDistances.addAll(distances);
			}

		}
		if (!ConstraintChecker.isAllOddOrEven(allDistances)) {
			return false;
		}

		// for (Node node : tree) {
		// if (node.isLeaf() && node.isLogicalOdd()) {
		// if (ignoreLonlyNodes && node.getParents().size() == 0) {
		//
		// } else {
		// return false;
		// }
		// }
		// }
		return true;
	}

	// Ensure each pipe is not crossing. Considering only verticals because pipes
	// are staggered.
	public boolean isConst6(Tree tree) {
		HashSet<Link> links = new HashSet<Link>();
		for (Node node : tree) {
			for (Node child : node.getChildren()) {
				Link link = new Link(node.getCoordinate(), child.getCoordinate());
				links.add(link);
			}
		}

		for (Link link1 : links) {
			for (Link link2 : links) {
				Coordinate start1 = link1.getStart();
				Coordinate end1 = link1.getEnd();

				Coordinate start2 = link2.getStart();
				Coordinate end2 = link2.getEnd();

				if (start1.x == end1.x && start2.x == end2.x && start1.x == start2.x) {
					if (start1.y < start2.y && start2.y < end1.y && end1.y < end2.y) {
						return false;
					}
					if (start2.y < start1.y && start1.y < end2.y && end2.y < end1.y) {
						return false;
					}
				}

			}
		}
		return true;
	}

	// Tube Bends on the Rear Side of HX
	public boolean isConst7(Tree tree, boolean isIgnoreLonlyNodes) throws IOException {
		tree.reCalculateLogicalTurn();

		for (Node node : tree) {
			if (node.getParents().size() == 0 && node.getChildren().size() == 0) {
				continue;
			}
			if (node.isLogicalOdd()) {
				if (node.getChildren().size() != 1) {
					return false;
				}
				Node child = node.getChildren().iterator().next();
				if (node.getX() != child.getX()) {
					return false;
				}

				if (node.isPhysicalOdd()) {
					if (node.getY() + 1 != child.getY()) {
						return false;
					}
				} else {
					if (node.getY() - 1 != child.getY()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// Branching is only allowed from bottom to top. Merging is only allowed from
	// top to bottom.
	public boolean isConst8(Tree tree) {
		for (Node node : tree) {
			int physicalHeight = node.getY();
			Set<Node> children = node.getChildren();
			if (children.size() > 1) {
				for (Node child : children) {
					int childPhysicalHeight = child.getY();
					if (physicalHeight <= childPhysicalHeight) {
						return false;
					}

				}
			}
			Set<Node> parents = node.getParents();
			if (parents.size() > 1) {
				for (Node parent : parents) {
					int parentPhysicalHeight = parent.getY();
					if (physicalHeight <= parentPhysicalHeight) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// Branching is limited to up to max_num. Merging is limited to up to max_num.
	public boolean isConst9(Tree tree, boolean ignoreLonlyNodes) {
		for (Node node : tree) {
			if (node.getChildren().size() > max_numForConst9_split
					|| node.getParents().size() > max_numForConst9_merge) {
				return false;
			}
		}
		return true;
	}

	public boolean isConst10(Tree tree) {

		for (Node node : tree) {
			Set<Node> descendants = node.getDescendants();
			if (descendants.size() > 1) {
				int minLogicalDistance = Integer.MAX_VALUE;
				int maxLogicalDistance = -1;
				for (Node descendant : descendants) {
					if (descendant.isLeaf()) {
						int distance = node.getLogicalDistance(descendant);
						if (distance != -1) {
							if (distance < minLogicalDistance) {
								minLogicalDistance = distance;
							}
							if (distance > maxLogicalDistance) {
								maxLogicalDistance = distance;
							}
						}
					}
				}
				if (maxLogicalDistance > maxDiffLengthForConst10 * minLogicalDistance) {
					return false;
				}
			}

		}

		return true;
	}

	public double getMaxDiffLengthForConst10() {
		return maxDiffLengthForConst10;
	}

	// The distance between inlet and outlet should be larger than min_distance
	public boolean isConst0(Tree tree) {
		Set<Node> inlets = tree.getInlets();
		Set<Node> outlets = tree.getOutlets();

		for (Node inlet : inlets) {
			for (Node outlet : outlets) {
				int distance = inlet.getPhysicalDistance(outlet);
				if (distance < min_distanceForConst0) {
					return false;
				}
			}
		}
		return true;

	}

	public static boolean checkConst3forAddingNode(Node parentCandidate, Node childCandidate) throws IOException {
		// nodeの子孫(shison、ただし自分自身も含む)全てに対して以下をチェック：各parentsの先祖で共通のノード(sameSosen)を抽出（無ければOK）。logicalDistance(sameSosen,
		// parent)の偶数・奇数が一致。
		// これを満たさなければ、addChildはできない。

		boolean isOk = true;
		parentCandidate.addChild(childCandidate, false, false);

		Set<Node> targets = childCandidate.getDescendants();
		targets.add(childCandidate);

		for (Node shison : targets) {
			Set<Node> parents = shison.getParents();
			if (parents.size() <= 1) {
				continue;
			}
			ArrayList<Set<Node>> sosens = new ArrayList<Set<Node>>();
			for (Node parent : parents) {
				sosens.add(parent.getAncestors());
			}
			Set<Node> multis = multiOccurance(sosens);

			for (Node commonSosen : multis) {

				Node parent1 = parents.iterator().next();

				int logicalDistance = commonSosen.getLogicalDistance(parent1);// 必ずリンクはあるはず。なので、-1は有りえない。
				if (logicalDistance == -1) {
					System.out.println("Warning code: 1533u");
				}

				boolean isOdd = true;
				if (logicalDistance % 2 == 0) {
					isOdd = false;
				}

				for (Node parent : parents) {
					logicalDistance = commonSosen.getLogicalDistance(parent);
					if (logicalDistance % 2 == 0 && isOdd) {
						isOk = false;
						break;
					} else if (logicalDistance % 2 == 1 && !isOdd) {
						isOk = false;
						break;
					}
				}

				if (!isOk) {
					break;
				}

			}
		}

		if (!isOk) {
			parentCandidate.removeChild(childCandidate, false);
			return false;
		} else {
			return true;
		}
	}

	private static Set<Node> multiOccurance(ArrayList<Set<Node>> sosens) {
		// 出現回数をカウントするマップ
		Map<Node, Integer> freq = new HashMap<>();

		// 各祖先集合を走査してカウントアップ
		for (Set<Node> ancestors : sosens) {
			for (Node n : ancestors) {
				freq.put(n, freq.getOrDefault(n, 0) + 1);
			}
		}

		// 出現回数 >= 2 のものだけを multis に追加
		Set<Node> multis = new HashSet<>();
		for (Map.Entry<Node, Integer> e : freq.entrySet()) {
			if (e.getValue() >= 2) {
				multis.add(e.getKey());
			}
		}

		return multis;
	}
}
