package nedo2024;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Tree extends ArrayList<Node> implements Serializable {
	// Add node in turn from top-left to bottom-right.
	private double val = 0;
	private int yNum = 0;
	private int xNum = 0;
	private static String xmlOther = "";
	ConstraintChecker checker = null;

	public Tree(int yNum_, int xNum_, ConstraintChecker checker) {
		yNum = yNum_;
		xNum = xNum_;
		this.checker = checker;
	}

	@Override
	public Node get(int i) {
		return super.get(i);
	}

	public ConstraintChecker getConstraintChecker() {
		return checker;
	}

	public void reCalculateLogicalTurn() throws IOException {

		String mode = System.getProperty("DEBUG_MODE");
		if (mode != null && mode.equals("debug")) {
			boolean isOK = isParentChildCheck();
			if (!isOK) {
				System.out.println("Error code: 3o5iu");
			}
		}

		boolean cycleCheck = checker.isConst2(this);
		if (!cycleCheck) {
			fix(2);
		}

		if (!checker.isConst2(this)) {
			VisualMain.visualize_logical(this);
			fix(2);
		}

		mode = System.getProperty("DEBUG_MODE");
		if (mode != null && mode.equals("debug")) {
			boolean isOK = isParentChildCheck();
			if (!isOK) {
				System.out.println("Error code: sete");
			}
		}

		TreeSet<Node> inlets = getInlets();
		for (Node node : inlets) {
			node.setLogicalOdd(true, 0);
		}
	}

	public TreeSet<Node> getInlets() {
		TreeSet<Node> inlets = new TreeSet<Node>();
		for (Node node : this) {
			if (node.getParents().size() == 0) {
				inlets.add(node);
			}
		}
		return inlets;
	}

	public TreeSet<Node> getOutlets() {
		TreeSet<Node> outlets = new TreeSet<Node>();
		for (Node node : this) {
			if (node.isLeaf()) {
				outlets.add(node);
			}
		}
		return outlets;
	}

	public int getYNum() {
		return yNum;
	}

	public int getXNum() {
		return xNum;
	}

	public void setVal(double v) {
		val = v;
	}

	public double getVal() {
		return val;
	}

	public static void setOther(String s) {
		xmlOther = s;
	}

	public static String getOther() {
		return xmlOther;
	}

	public String toString() {
		String s = val + "\n";
		for (Node n : this) {
			s += n.toString() + ":";
			for (Node child : n.getChildren()) {
				s += child + ",";
			}
			s = s.substring(0, s.length() - 1) + "\n";
		}
		return s;
	}

	public Tree copy() throws IOException {
		Tree tree2 = new Tree(yNum, xNum, checker);
		tree2.setVal(this.getVal());
		for (int i = 1; i <= this.size(); i++) {
			Node n = getNode(i);
			Node n_new = new Node(n.getX(), n.getY(), tree2);
			tree2.add(n_new);
		}

		for (int i = 1; i <= this.size(); i++) {
			Set<Node> parents = getNode(i).getParents();
			for (Node parent : parents) {
				tree2.getNode(parent.getId()).addChild(tree2.getNode(i), false, false);
			}
		}
		return tree2;
	}

	/**
	 * このtreeのn1の子孫（isDescendents=trueのばあい）の構造を、tree2のn2の子孫の構造に設定する。子孫のsizeは一致していることが保証されちえる。
	 * 
	 * @param tree2
	 * @param n1
	 * @param n2
	 * @return
	 * @throws IOException
	 */
	public Tree copy(Tree tree2, Node n1, Node n2, boolean isDescendants) throws IOException {
		Tree copyTree = new Tree(yNum, xNum, checker);
		copyTree.setVal(this.getVal());
		for (int i = 1; i <= this.size(); i++) {
			Node n = getNode(i);
			Node n_new = new Node(n.getX(), n.getY(), copyTree);
			copyTree.add(n_new);
		}

		// このtreeの対象部分木のノードIDとtree2の対象部分木のノードIDの対応表を作る。ランダム。
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		ArrayList<Integer> set1 = new ArrayList<Integer>();
		set1.add(n1.getId());
		ArrayList<Integer> set2 = new ArrayList<Integer>();
		set2.add(n2.getId());
		if (isDescendants) {
			for (Node node : n1.getDescendants()) {
				set1.add(node.getId());
			}
			for (Node node : n2.getDescendants()) {
				set2.add(node.getId());
			}
		} else {
			for (Node node : n1.getAncestors()) {
				set1.add(node.getId());
			}
			for (Node node : n2.getAncestors()) {
				set2.add(node.getId());
			}
		}
		for (int i = 0; i < set1.size(); i++) {
			map.put(set2.get(i), set1.get(i));
		}

		if (isDescendants) {
			for (int i = 1; i <= this.size(); i++) {
				Set<Node> parents = getNode(i).getParents();
				for (Node parent : parents) {
					if (!set1.contains(parent.getId())) {
						copyTree.getNode(parent.getId()).addChild(copyTree.getNode(i), false, false);
					}
				}
			}

			for (int i = 1; i <= this.size(); i++) {
				Set<Node> parents = tree2.getNode(i).getParents();
				for (Node parent : parents) {
					if (set2.contains(parent.getId())) {
						copyTree.getNode(map.get(parent.getId())).addChild(copyTree.getNode(map.get(i)), false, false);
					}
				}
			}
		} else {
			for (int i = 1; i <= this.size(); i++) {
				if (!set1.contains(i)) {
					Set<Node> parents = getNode(i).getParents();
					for (Node parent : parents) {
						copyTree.getNode(parent.getId()).addChild(copyTree.getNode(i), false, false);
					}
				}
			}

			for (int i = 1; i <= this.size(); i++) {

				if (set2.contains(i)) {
					Set<Node> parents = tree2.getNode(i).getParents();
					for (Node parent : parents) {
						copyTree.getNode(map.get(parent.getId())).addChild(copyTree.getNode(map.get(i)), false, false);

					}
				}
			}
		}
		return copyTree;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Tree other = (Tree) obj;

		for (int i = 0; i < this.size(); i++) {
			Node n1 = this.get(i);
			Node n2 = other.get(i);
			TreeSet<Node> children1 = n1.getChildren();
			ArrayList<Node> list1 = new ArrayList<>(children1);
			TreeSet<Node> children2 = n2.getChildren();
			ArrayList<Node> list2 = new ArrayList<>(children2);

			if (list1.size() != list2.size()) {
				return false;
			}
			for (int j = 0; j < list1.size(); j++) {
				if (!list1.get(j).equals(list2.get(j))) {
					return false;
				}
			}
		}
		return true;

	}

	@Override
	public int hashCode() {
		int result = 1;
		for (Node node : this) {
			TreeSet<Node> children = node.getChildren();
			List<Node> childList = new ArrayList<>(children);
			result = 31 * result + childList.hashCode();
		}
		return result;
	}

	public static Tree getTreeFromInterface(String interfaceFileName, ConstraintChecker checker) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(interfaceFileName)));
		int r = 0;
		int c = 0;
		String line = "";
		String other = "";
		HashSet<Node> hasParentNodes = new HashSet<Node>();
		while ((line = br.readLine()) != null) {
			if (line.contains("conf")) {
				r = Integer.parseInt(line.split("\"")[1]);
				c = Integer.parseInt(line.split("\"")[3]);
			}

			if (!line.contains("hex") && !line.contains("seg") && !line.contains("conf")) {
				other += line;
			}

		}
		other += "</hex>";
		if (!other.equals("</hex>")) {
			Tree.setOther(other);
		}

		br.close();

		Tree tree = new Tree(r, c, checker);
		for (int j = 1; j <= c; j++) {
			for (int i = 1; i <= r; i++) {
				Node n = new Node(j, i, tree);
				tree.add(n);
			}
		}

		br = new BufferedReader(new InputStreamReader(new FileInputStream(interfaceFileName)));
		while ((line = br.readLine()) != null) {
			if (line.contains("seg")) {
				Node startNode = tree.get(Integer.parseInt(line.split("\"")[1]) - 1);
				Node endNode = tree.get(Integer.parseInt(line.split("\"")[3]) - 1);
				startNode.addChild(endNode, false, false);
				hasParentNodes.add(endNode);
			}
		}

		for (Node node : tree) {
			if (!hasParentNodes.contains(node)) {
				tree.getInlets().add(node);
			}
		}

		return tree;

	}

	public boolean fixAll() throws IOException {
		Boolean test[] = new Boolean[11];
		int turns[] = { 1, 2, 3, 4, 6, 7, 8, 9, 10, 5 };// 5が調整難しいのと、修正は簡単なので。
		for (int i : turns) {

			if (checker.getConstraints()[i]) {
				boolean check = fix(i);
				test[i] = check;

				// if (!checker.isConst1(this)) {
				// System.out.println("warning code: asdf98");
				// }

			} else {
				test[i] = null;
			}

		}

		return checker.allCheck(this, false);
	}

	public boolean fix(int constraintId) throws IOException {

		Tree treeCopy = this.copy();

		if (constraintId != 2) {
			// cycle detection. In this case, logical turn is not used.
			reCalculateLogicalTurn();
		}

		if (checker.isConst(this, constraintId, false)) {
			return true;
		}

		int depth = Thread.currentThread().getStackTrace().length;
		if (depth > 90) {
			// String mode = System.getProperty("DEBUG_MODE");
			// if (mode != null && mode.equals("debug")) {
			// VisualMain.visualize_logical(this);
			// System.out.println("fix recursive check!");
			// }
			if (depth > 100) {
				return false;
			}
		}

		switch (constraintId) {
		case 0:

			int count0 = 0;
			while (true) {
				count0++;
				if (count0 > getInlets().size() + getOutlets().size()) {
					String mode = System.getProperty("DEBUG_MODE");
					if (mode != null && mode.equals("debug")) {
						// VisualMain.visualize_logical(this);
						// System.out.println("const0 in Tree.fix");
					}
					return false;
				}

				List<Node> inletsList = Util.getShuffledList(getInlets());
				List<Node> outletList = Util.getShuffledList(getOutlets());

				Node targetInlet = null;
				Node targetOutlet = null;
				for (Node inlet : inletsList) {
					for (Node outlet : outletList) {
						int distance = inlet.getPhysicalDistance(outlet);
						if (distance < checker.getMin_distanceForConst0()) {
							targetInlet = inlet;
							targetOutlet = outlet;
							break;
						}
					}
					if (targetInlet != null) {
						break;
					}
				}

				if (targetInlet != null) {
					List<Node> candidates = Util.getShuffledList(targetOutlet.getFarNodesForConst0());
					candidates.removeAll(targetInlet.getDescendants());
					candidates.remove(targetInlet);

					for (Node candidate : candidates) {
						if (!candidate.isPhysicalOdd()) {
							boolean isOk = candidate.addChild(targetInlet, true, false);
							if (isOk) {
								break;
							}
						}
					}

				} else {
					return true;
				}
			}
			// break;
		case 1:

			// lonlyNodeが偶数なら、2個組で対応してみる。
			List<Node> lonlyNodes1 = Util.getShuffledList(getLonlyNodes());
			List<Node> lonlyNodes2 = Util.getShuffledList(getLonlyNodes());
			for (Node node1 : lonlyNodes1) {
				if (node1.getChildren().size() >= 1) {
					continue;
				}
				boolean isOk = false;
				for (Node node2 : lonlyNodes2) {
					if (checker.getConstraints()[7]) {
						if (node1.isPhysicalOdd()) {
							if (node1.getY() + 1 == node2.getY()) {
								isOk = node1.addChild(node2, true, true);
							}
						} else {
							if (node1.getY() - 1 == node2.getY()) {
								isOk = node1.addChild(node2, true, true);
							}
						}
						if (isOk) {
							break;
						}

					} else {

						isOk = node1.addChild(node2, true, true);
						if (isOk) {
							break;
						}
					}
				}
			}

			Set<Node> lonlyNodes = getLonlyNodes();
			List<Node> shuffledNodes = Util.getShuffledList(this);

			for (Node lonlyNode : lonlyNodes) {

				shuffledNodes = Util.getShuffledList(this);

				// ループの中で、lonlyNodeではなくなっているかもしれないので、そうだったらskip
				if (lonlyNode.getParents().size() >= 1 || lonlyNode.getChildren().size() >= 1) {
					continue;
				}

				// まずは他全ての制約を満たす範囲でランダムに接続 (lonlyNode -> anotherNode)
				boolean isOk = false;
				for (Node node : shuffledNodes) {
					isOk = lonlyNode.addChild(node, true, false);
					if (isOk) {
						break;
					}
				}
				// 他全ての制約を満たす範囲でランダムに接続 (anotherNode -> lonlyNode)
				if (!isOk) {
					for (Node node : shuffledNodes) {
						isOk = node.addChild(lonlyNode, true, false);
						if (isOk) {
							break;
						}
					}
				}
			}

			// 緩和
			for (Node lonlyNode : lonlyNodes) {
				shuffledNodes = Util.getShuffledList(this);

				// ループの中で、lonlyNodeではなくなっているかもしれないので、そうだったらskip
				if (lonlyNode.getParents().size() >= 1 || lonlyNode.getChildren().size() >= 1) {
					continue;
				}

				boolean isOk = false;
				for (Node node : shuffledNodes) {
					isOk = lonlyNode.addChild(node, true, true);
					if (isOk) {
						break;
					}
				}
				if (!isOk) {
					for (Node node : shuffledNodes) {
						isOk = node.addChild(lonlyNode, true, true);
						if (isOk) {
							break;
						}
					}
				}

				// さらに緩和
				if (!isOk) {
					if (Math.random() < 0.5) {
						while (true) {
							int targetId = (int) (Math.random() * shuffledNodes.size()) + 1;
							if (lonlyNode.getId() == targetId) {
								continue;
							} else {
								isOk = lonlyNode.addChild(getNode(targetId), false, false);
								break;
							}
						}
					} else {
						while (true) {
							int targetId = (int) (Math.random() * shuffledNodes.size()) + 1;
							if (lonlyNode.getId() == targetId) {
								continue;
							} else {
								isOk = getNode(targetId).addChild(lonlyNode, false, false);
								break;
							}
						}
					}

				}

				if (!isOk) {
					String mode = System.getProperty("DEBUG_MODE");
					if (mode != null && mode.equals("debug")) {
						System.out.println("const 1!!!");
						VisualMain.visualize(this);
						fix(1);
					}
					return false;
				}

			}

			break;
		case 2:

			while (!checker.isConst2(this)) {
				// cyecleを1ずつ対応するので・・
				List<Node> cycleNodes = getOneOfCycleNodes();
				if (cycleNodes.size() <= 1) {
					System.out.println("YO check");
					getOneOfCycleNodes();// testのためだけの
				}
				shuffledNodes = Util.getShuffledList(cycleNodes);
				boolean isRemoved = false;
				// 制約を満たすなら、サイクルの中の1リンクを削除
				for (int i = 0; i < cycleNodes.size(); i++) {
					Node startNode = shuffledNodes.get(i);
					Node endNode = shuffledNodes.get((i + 1) % cycleNodes.size());
					isRemoved = startNode.removeChild(endNode, true);
					if (isRemoved) {
						break;
					}
				}

				// 制約を満たす範囲で削除できなかった場合、強制削除
				if (!isRemoved) {
					shuffledNodes.get(0).removeChild(shuffledNodes.get(1), false);
				}
			}

			break;
		case 3:

			TreeSet<Node> inlets = getInlets();

			for (Node node : this) {
				Set<Node> parents = new HashSet<Node>();
				parents.addAll(node.getParents());
				if (parents.size() <= 1) {
					continue;
				}

				// ノードにつながっている親ノード集合を確認。奇数と偶数の一致をチェック。
				int count = 0;
				int parentOddCount = 0;
				for (Node parent : parents) {
					for (Node inlet : inlets) {
						Set<Integer> distances = inlet.getLogicalDistances(parent);
						for (int distance : distances) {
							count++;
							if (distance % 2 == 1) {
								parentOddCount++;
							}
						}
					}
				}

				// 全部奇数、または全部偶数ならスキップ。
				if (parentOddCount == 0 || parentOddCount == count) {
					continue;
				}

				// そうでない場合は、少ないほうのリンクを削除。
				if (parentOddCount > (count - parentOddCount)) {
					for (Node parent : parents) {
						for (Node inlet : inlets) {

							Set<Integer> distances = inlet.getLogicalDistances(parent);
							for (int distance : distances) {
								if (distance % 2 == 0) {
									parent.removeChild(node, false);
								}
							}
						}
					}
				} else {
					for (Node parent : parents) {
						for (Node inlet : inlets) {
							Set<Integer> distances = inlet.getLogicalDistances(parent);
							for (int distance : distances) {
								if (distance % 2 == 1) {
									parent.removeChild(node, false);
								}
							}
						}
					}
				}
			}

			break;
		case 4:
			int maxLength = checker.getMaxLengthForConst4();
			for (Node parentNode : this) {
				Set<Node> children = new HashSet<Node>();
				children.addAll(parentNode.getChildren());
				for (Node childNode : children) {
					int physicalDistance = parentNode.getPhysicalDistance(childNode);
					if (physicalDistance > maxLength) {

						parentNode.removeChild(childNode, false);

						// if (parentNode.isLonlyNode()) {
						//
						// }
						// if (childNode.isLonlyNode()) {
						//
						// }
					}
				}
			}
			break;
		case 5:

			inlets = getInlets();
			TreeSet<Node> outlets = getOutlets();
			Set<Node> removedNodes = new HashSet<Node>();
			for (Node inlet : inlets) {
				for (Node outlet : outlets) {
					if (inlet == outlet) {
						removedNodes.add(inlet);
						break;
					}
					int distance = inlet.getLogicalDistance(outlet);
					// inletからoutletまでが奇数
					if (distance != -1 && distance % 2 == 0) {
						// 最後のノードを切り捨てて、lonlynodeにして、ループを抜けた後にlolyNodes対応をする。
						for (Node parent : outlet.getParents()) {
							parent.removeChild(outlet, false);
							removedNodes.add(outlet);
							break;
						}
					}
				}
			}

			shuffledNodes = Util.getShuffledList(this);

			for (Node removedNode : removedNodes) {
				boolean isOk = false;
				for (Node node : shuffledNodes) {
					if (Math.random() < 0.5) {
						isOk = node.addChild(removedNode, true, false);
						if (isOk) {
							break;
						} else {
							isOk = removedNode.addChild(node, true, false);
						}
					} else {
						isOk = removedNode.addChild(node, true, false);
						if (isOk) {
							break;
						} else {
							isOk = node.addChild(removedNode, true, false);
						}
					}
					if (isOk) {
						break;
					}
				}
				if (!isOk) {

				}
			}

			// lonly node対応
			fix(1);

			break;
		case 6:
			ArrayList<Link> links = new ArrayList<Link>();
			ArrayList<Node> nodes = new ArrayList<Node>();
			shuffledNodes = Util.getShuffledList(this);
			for (Node node : shuffledNodes) {
				for (Node child : node.getChildren()) {
					Link link = new Link(node.getCoordinate(), child.getCoordinate());
					links.add(link);
					nodes.add(node);
					nodes.add(child);
				}
			}

			for (int i = 0; i < links.size(); i++) {
				Link link1 = links.get(i);
				for (int j = 0; j < i; j++) {
					Link link2 = links.get(j);
					Coordinate start1 = link1.getStart();
					Coordinate end1 = link1.getEnd();

					Coordinate start2 = link2.getStart();
					Coordinate end2 = link2.getEnd();

					if ((start1.x == end1.x && start2.x == end2.x && start1.x == start2.x)
							&& (start1.y < start2.y && start2.y < end1.y && end1.y < end2.y
									|| start2.y < start1.y && start1.y < end2.y && end2.y < end1.y)) {
						Node parent = nodes.get(i * 2);
						Node child = nodes.get(i * 2 + 1);
						boolean isOk = parent.removeChild(child, true);
						if (!isOk) {
							parent = nodes.get(j * 2);
							child = nodes.get(j * 2 + 1);
							isOk = parent.removeChild(child, false);
						}
					}
				}
			}
			break;
		case 7:
			for (Node node : this) {
				if (node.getParents().size() == 0 && node.getChildren().size() == 0) {
					continue;
				}
				if (node.isLogicalOdd()) {
					if (node.getChildren().size() > 1) {
						Set<Node> children = node.getChildren();
						Set<Node> targets = Util.getRandomNodes(children, children.size() - 1);
						for (Node target : targets) {
							node.removeChild(target, false);
						}
						Node child = node.getChildren().iterator().next();
						if (node.getX() != child.getX()) {
							node.removeChild(child, false);
						}

						if (node.isPhysicalOdd()) {
							if (node.getY() + 1 != child.getY()) {
								node.removeChild(child, false);
							}
						} else {
							if (node.getY() - 1 != child.getY()) {
								node.removeChild(child, false);
							}
						}
					} else {
						List<Node> targets = Util.getShuffledList(this);
						boolean isOk = false;
						for (Node target : targets) {
							isOk = node.addChild(target, true, false);
							if (isOk) {
								break;
							}
						}
						if (!isOk) {
							for (Node target : targets) {
								isOk = node.addChild(target, true, true);
								if (isOk) {
									break;
								}
							}
						}

					}
				}
			}

			break;
		case 8:

			shuffledNodes = Util.getShuffledList(this);
			for (Node node : shuffledNodes) {
				Set<Node> children = new HashSet<Node>();
				children.addAll(node.getChildren());
				int count = 0;
				if (children.size() >= 2) {
					int yPosition = node.getY();
					for (Node child : children) {
						if (yPosition < child.getY()) {
							node.removeChild(child, false);
							fix(1);
							count++;
							if (count >= children.size() - 1) {
								break;
							}
						}
					}
				}
			}

			for (Node node : shuffledNodes) {
				Set<Node> parents = node.getParents();
				int count = 0;
				if (parents.size() >= 2) {
					int yPosition = node.getY();
					Set<Node> parentCopy = new HashSet<Node>();
					parentCopy.addAll(parents);
					for (Node parent : parentCopy) {
						if (yPosition > parent.getY()) {
							parent.removeChild(node, false);
							fix(1);
							count++;
							if (count >= parents.size() - 1) {
								break;
							}
						}
					}
				}
			}

			break;
		case 9:

			int maxNum_merge = checker.getMaxNumForConst9_merge();
			int maxNum_split = checker.getMaxNumForConst9_split();
			shuffledNodes = Util.getShuffledList(this);
			for (Node node : shuffledNodes) {
				Set<Node> children = new HashSet<Node>();
				children.addAll(node.getChildren());
				int count = 0;
				if (children.size() > maxNum_split) {
					for (Node child : children) {
						boolean isOk = node.removeChild(child, true);
						if (isOk) {
							count++;
						}
						if (children.size() - count <= maxNum_split) {
							break;
						}
					}
				}

				count = 0;
				if (children.size() > maxNum_split) {
					for (Node child : children) {
						node.removeChild(child, false);
						count++;
						if (children.size() - count <= maxNum_split) {
							break;
						}
					}
				}
			}

			for (Node node : shuffledNodes) {
				Set<Node> parents = node.getParents();
				int count = 0;
				if (parents.size() > maxNum_merge) {
					Set<Node> parentCopy = new HashSet<Node>();
					parentCopy.addAll(parents);
					for (Node parent : parentCopy) {
						boolean isOk = parent.removeChild(node, true);
						// boolean isOk = node.removeParent(parent, true);
						if (isOk) {
							count++;
						}
						if (parents.size() - count <= maxNum_merge) {
							break;
						}
					}
				}

				count = 0;
				if (parents.size() > maxNum_merge) {
					Set<Node> parentCopy = new HashSet<Node>();
					parentCopy.addAll(parents);
					for (Node parent : parentCopy) {
						parent.removeChild(node, false);
						// node.removeParent(parent, false);
						count++;
						if (parents.size() - count <= maxNum_merge) {
							break;
						}
					}
				}
			}
			fix(1);
			break;

		case 10:

			// VisualMain.visualize_logical(this);

			double maxDiff = checker.getMaxDiffLengthForConst10();

			int trial = 0;

			while (trial < 10) {
				trial++;
				for (Node node : this) {
					Set<Node> descendants = node.getDescendants();
					if (descendants.size() > 1) {
						int minLogicalDistance = Integer.MAX_VALUE;
						int maxLogicalDistance = -1;
						Node minNode = null;
						for (Node descendant : descendants) {
							if (descendant.isLeaf()) {
								int distance = node.getLogicalDistance(descendant);
								if (distance != -1) {
									if (distance < minLogicalDistance) {
										minLogicalDistance = distance;
										minNode = descendant;
									}
									if (distance > maxLogicalDistance) {
										maxLogicalDistance = distance;
									}
								}
							}
						}
						if (maxLogicalDistance > maxDiff * minLogicalDistance) {
							boolean allParentsHaveSomeChildren = true;
							for (Node parent : minNode.getParents()) {
								if (parent.getChildren().size() == 1) {
									allParentsHaveSomeChildren = false;
								}
							}
							if (allParentsHaveSomeChildren) {
								removeParentsAndAddTry(minNode, this, true);
							}
						}
					}
				}
				if (checker.isConst10(this)) {
					break;
				}
			}

			// VisualMain.visualize_logical(this);

			if (checker.isConst10(this)) {
				break;
			}

			// 最も長いパスに対して、ランダムにpathを切るのを試みる。
			Node longestPathLeafNode = null;
			int longestDistance = -1;
			for (Node node1 : getInlets()) {
				for (Node node2 : getOutlets()) {
					int logicalDistance = node1.getLogicalDistance(node2);
					if (logicalDistance > longestDistance) {
						longestDistance = logicalDistance;
						longestPathLeafNode = node2;
					}
				}
			}
			boolean isOk = false;
			Node targetNode = null;
			if (longestPathLeafNode != null) {
				Set<Node> ancestors = longestPathLeafNode.getAncestors();
				shuffledNodes = Util.getShuffledList(ancestors);
				for (Node ancestor : shuffledNodes) {
					for (Node child : ancestor.getChildren()) {
						if (child.getDescendants().contains(longestPathLeafNode)) {
							isOk = ancestor.removeChild(child, true);
							targetNode = child;
							break;
						}
					}
					if (isOk) {
						break;
					} else {
						targetNode = null;
					}
				}

				for (Node ancestor : shuffledNodes) {
					for (Node child : ancestor.getChildren()) {
						if (child.getDescendants().contains(longestPathLeafNode)) {
							isOk = ancestor.removeChild(child, false);
							targetNode = child;
							break;
						}
					}
					if (isOk) {
						break;
					} else {
						targetNode = null;
					}
				}
			}

			if (targetNode != null) {
				removeParentsAndAddTry(targetNode, this, true);
			}

			// VisualMain.visualize_logical(this);

			if (!checker.isConst10(this)) {
				if (treeCopy.equals(this)) {// 変化がなかったということ：
					return false;
				} else {
					fix(10);
				}
			}

			break;
		}

		if (!checker.isConst(this, constraintId, false)) {
			// fix(constraintId);
			return false;
		}

		return true;
	}

	/**
	 * 全てのparentsからnodeを切り離し、このnodeを可能なところにaddする。
	 * 
	 * @param node
	 * @param parents
	 * @param tree
	 * @return
	 * @throws IOException
	 */
	private static boolean removeParentsAndAddTry(Node node, Tree tree, boolean isBeforeCheckOnly) throws IOException {

		Set<Node> parents = new HashSet<Node>();
		parents.addAll(node.getParents());

		for (Node parent : parents) {
			parent.removeChild(node, false);
		}
		ArrayList<Node> targets = new ArrayList<Node>();
		targets.addAll(tree);
		Collections.shuffle(targets);

		boolean isOk = false;
		for (Node parentCandidate : targets) {
			isOk = parentCandidate.addChild(node, true, isBeforeCheckOnly);
			if (isOk) {
				break;
			}
		}

		if (!isOk) {
			// 元に戻す
			for (Node parent : parents) {
				parent.addChild(node, false, false);
			}
		}
		return isOk;
	}

	private int getLongestPathLength() {
		TreeSet<Integer> lengths = new TreeSet<Integer>();
		for (Node outlet : getOutlets()) {
			lengths.addAll(outlet.getLogicalDistancesFromInlets());
		}
		return lengths.last();
	}

	public Set<Node> getLonlyNodes() {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Node node : this) {
			if (node.getParents().size() == 0 && node.getChildren().size() == 0) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public List<Node> getOneOfCycleNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (Node node : this) {
			List<Integer> list = CycleDetector.detectCycle(node);
			if (list.size() != 0) {
				for (int id : list) {
					nodes.add(getNode(id));
				}
				break;
			}
		}
		return nodes;
	}

	public Node getNode(int id) {
		return this.get(id - 1);
	}

	public void changeNodePosition(Node n1, Node n2) throws IOException {

		// Set<Integer> n1ParentIds = n1.getParentNodeIds();
		// Set<Integer> n1ChildIds = n1.getChildNodeIds();
		// Set<Integer> n2ParentIds = n2.getParentNodeIds();
		// Set<Integer> n2ChildIds = n2.getChildNodeIds();
		//
		// Set<Node> parents1 = new HashSet<Node>();
		// parents1.addAll(n1.getParents());
		// for (Node parent1 : parents1) {
		// parent1.removeChild(n1, false);
		// }
		// Set<Node> parents2 = new HashSet<Node>();
		// parents2.addAll(n2.getParents());
		// for (Node parent2 : parents2) {
		// parent2.removeChild(n2, false);
		// }
		// n1.removeChildrenAll();
		// n2.removeChildrenAll();
		//
		// for (int n2ParentId : n2ParentIds) {
		// getNode(n2ParentId).addChild(n1, false, false);
		// }
		// for (int n2ChildId : n2ChildIds) {
		// n1.addChild(getNode(n2ChildId), false, false);
		// }
		//
		// for (int n1ParentId : n1ParentIds) {
		// getNode(n1ParentId).addChild(n2, false, false);
		// }
		// for (int n1ChildId : n1ChildIds) {
		// n2.addChild(getNode(n1ChildId), false, false);
		// }

		Set<Node> n1Parents = new HashSet<Node>();
		n1Parents.addAll(n1.getParents());
		Set<Node> n1Children = new HashSet<Node>();
		n1Children.addAll(n1.getChildren());
		Set<Node> n2Parents = new HashSet<Node>();
		n2Parents.addAll(n2.getParents());
		Set<Node> n2Children = new HashSet<Node>();
		n2Children.addAll(n2.getChildren());

		boolean n1ToN2 = false;
		boolean n2ToN1 = false;
		if (n1Children.contains(n2)) {
			n1ToN2 = true;
		}
		if (n2Children.contains(n1)) {
			n2ToN1 = true;
		}

		for (Node parent1 : n1Parents) {
			parent1.removeChild(n1, false);
		}
		for (Node parent2 : n2Parents) {
			parent2.removeChild(n2, false);
		}
		n1.removeChildrenAll();
		n2.removeChildrenAll();

		for (Node n2Parent : n2Parents) {
			n2Parent.addChild(n1, false, false);

		}
		for (Node n2Child : n2Children) {
			n1.addChild(n2Child, false, false);
		}

		for (Node n1Parent : n1Parents) {
			n1Parent.addChild(n2, false, false);
		}
		for (Node n1Child : n1Children) {
			n2.addChild(n1Child, false, false);
		}

		if (n1ToN2) {
			n2.addChild(n1, false, false);
		} else if (n2ToN1) {
			n1.addChild(n2, false, false);
		}

	}

	public int getMinLogicalDistance() {
		int minLength = 10000;
		for (Node inlet : getInlets()) {
			for (Node node : this) {
				if (!node.isLeaf() || inlet == node || inlet.getLogicalDistance(node) == -1) {
					continue;
				}

				int length = inlet.getLogicalDistance(node);
				if (length < minLength) {
					minLength = length;
				}
			}
		}
		return minLength;
	}

	// for debug
	public boolean isParentChildCheck() {
		for (Node node : this) {
			for (Node child : node.getChildren()) {
				if (!child.getParents().contains(node)) {
					return false;
				}
			}
		}
		for (Node node : this) {
			for (Node parent : node.getParents()) {
				if (!parent.getChildren().contains(node)) {
					return false;
				}
			}
		}
		return true;
	}

	public int hashCodeOnlyStructure() {
		String structureSig = computeStructureSignature();
		return structureSig.hashCode();
	}

	/**
	 * 構造のみを見て this と other が同一か判定する。
	 * 
	 * これ用のhashCode()は、 String structureSig = computeStructureSignature(); return structureSig.hashCode(); とすれば良いかもしれない。
	 * 
	 */
	public boolean equalsOnlyStructure(Tree other) {
		// ノード数・辺数などの簡易チェック
		if (this.size() != other.size())
			return false;
		int edgesThis = this.stream().mapToInt(n -> n.getChildren().size()).sum();
		int edgesOther = other.stream().mapToInt(n -> n.getChildren().size()).sum();
		if (edgesThis != edgesOther)
			return false;

		// 各 DAG の“構造シグネチャ”を比較
		String sigThis = computeStructureSignature();
		String sigOther = other.computeStructureSignature();
		return sigThis.equals(sigOther);
	}

	/**
	 * DAG の構造に基づいて一意な文字列を生成するヘルパー。 子ノードのシグネチャをソートして括弧でくくることで、 部分グラフの形が同値なら同じ文字列になります。
	 */
	private String computeStructureSignature() {
		// 各ノード → シグネチャ のマップ
		Map<Node, String> sig = new HashMap<>();

		// まず sink（子なしノード）に基本シグネチャを割り当て
		for (Node leaf : getOutlets()) {
			sig.put(leaf, "()");
		}

		// DAG なので、全ノードにシグネチャが割り当たるまで反復
		while (sig.size() < this.size()) {
			boolean progressed = false;
			for (Node node : this) {
				if (sig.containsKey(node))
					continue;
				// 子すべてにシグネチャがあるか？
				List<String> childSigs = new ArrayList<>();
				boolean ready = true;
				for (Node c : node.getChildren()) {
					String cs = sig.get(c);
					if (cs == null) {
						ready = false;
						break;
					}
					childSigs.add(cs);
				}
				if (!ready)
					continue;

				// ソートして括弧化
				Collections.sort(childSigs);
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				childSigs.forEach(sb::append);
				sb.append(")");

				sig.put(node, sb.toString());
				progressed = true;
			}
			if (!progressed) {
				// もしここに来たら循環があるか、孤立ノードがあるはず
				throw new IllegalStateException("DAG ではないか、孤立ノードがあります");
			}
		}

		// 最後に根ノード（inlets）のシグネチャをまとめてソート
		List<String> rootSigs = new ArrayList<>();
		for (Node root : getInlets()) {
			rootSigs.add(sig.get(root));
		}
		Collections.sort(rootSigs);

		// グラフ全体のシグネチャ
		StringBuilder graphSig = new StringBuilder();
		graphSig.append("{");
		rootSigs.forEach(graphSig::append);
		graphSig.append("}");
		return graphSig.toString();
	}
}
