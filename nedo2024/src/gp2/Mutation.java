package gp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nedo2024.Node;
import nedo2024.Tree;
import nedo2024.Util;

public class Mutation {

	public static Tree mutate(Tree treeTemp) throws IOException {

		Tree tree = treeTemp.copy();
		Tree mutationSucceededTree = null;

		int successCount = 0;
		List<Node> shuffledNodes = Util.getShuffledList(tree);

		boolean isSucceed = false;

		for (Node node : shuffledNodes) {
			boolean isOk = false;

			if (Math.random() < 0.5) {

				for (Node parent : shuffledNodes) {
					if (parent != node) {
						isOk = parent.addChild(node, true, true);
					}
				}
			} else {
				for (Node child : shuffledNodes) {
					if (child != node) {
						isOk = node.addChild(child, true, true);
					}
				}
			}

			if (Math.random() < 0.5) {

				Set<Node> copySet = new HashSet<Node>();
				copySet.addAll(node.getParents());

				for (Node parent : copySet) {
					isOk = parent.removeChild(node, true);
				}
			} else {
				Set<Node> copySet = new HashSet<Node>();
				copySet.addAll(node.getChildren());
				for (Node child : copySet) {
					isOk = node.removeChild(child, true);
				}
			}

			if (isOk && tree.fixAll()) {
				successCount++;
				mutationSucceededTree = tree;
			}
		}

		if (successCount == 0) {
			tree = treeTemp.copy();
			shuffledNodes = Util.getShuffledList(tree);
			Node parent = shuffledNodes.get(0);
			Node child = shuffledNodes.get(1);
			parent.addChild(child, false, false);
			isSucceed = tree.fixAll();
			if (isSucceed) {
				mutationSucceededTree = tree;
				successCount++;
			}
		}

		if (!isSucceed) {
			tree = treeTemp.copy();
			Tree changedTree = changePosition(tree, null);
			if (changedTree==null) {
				isSucceed = false;
			} else if (changedTree.fixAll()) {
				isSucceed = true;
				mutationSucceededTree = changedTree;
			}
		}

		if (!isSucceed) {
			// tree = yobiTree;
			mutationSucceededTree = Util.initialTree(tree.getYNum(), tree.getXNum(), tree.getConstraintChecker());

			String mode = System.getProperty("DEBUG_MODE");
			if (mode != null && mode.equals("debug")) {
				System.out.println("Mutation failed");
			}
		} else {
			String mode = System.getProperty("DEBUG_MODE");
			if (mode != null && mode.equals("debug")) {
				System.out.println("Mutation succeeded: " + successCount);
			}

		}

		return mutationSucceededTree;

	}

	public static Tree changePosition(Tree treeTemp, Set<Tree> existing) throws IOException {

		Tree tree = treeTemp.copy();
		tree.setVal(0);

		int targetCount = 0;
		if (existing == null) {
			targetCount = 10;
		} else {
			targetCount = 100;
		}

		int n = tree.size() - 1;
		int count = 0;
		while (true) {
			count++;
			if (count >= targetCount) {
				break;
			}

			double r = new Random().nextDouble();
			int changeCount = (int) Math.ceil(100 * Math.sqrt(r));

			for (int i = 0; i < changeCount; i++) {

				int id1 = (int) (Math.random() * n) + 1;
				Node node1 = tree.getNode(id1);
				List<Node> cand1 = new ArrayList<Node>();
				cand1.addAll(tree);
				cand1.remove(node1);

				for (Node parent : node1.getParents()) {
					for (Node far : parent.getFarNodesForConst4()) {
						cand1.remove(far);
					}
				}
				for (Node child : node1.getChildren()) {
					for (Node far : child.getFarNodesForConst4()) {
						cand1.remove(far);
					}
				}

				Collections.shuffle(cand1);

				boolean isOk = false;
				for (Node node2 : cand1) {
					for (Node parent : node2.getParents()) {
						for (Node child : node2.getChildren()) {
							if (parent.getNearNodes().contains(node1) && child.getNearNodes().contains(node1)) {
								tree.changeNodePosition(node1, node2);
								isOk = true;
								break;
							}
						}
						if (isOk) {
							break;
						}
					}
					if (isOk) {
						break;
					}
				}

			}

			if (existing != null) {
				if (tree.getConstraintChecker().allCheck(tree, false) && !existing.contains(tree)) {
					return tree;
				}
			} else {
				if (tree.getConstraintChecker().allCheck(tree, false)) {
					return tree;
				}
			}

		}

		return null;// mutation failed
	}

	public static Tree changePosition(Tree treeTemp, boolean isStrict) throws IOException {

		Tree tree = treeTemp.copy();
		tree.setVal(0);

		int n = tree.size() - 1;

		// iteration check or not
		// boolean iteCheck = new Random().nextBoolean();
		boolean iteCheck = false;// 2パターン考えたけど後者だけのほうが良い気がしてきたので。

		if (iteCheck) {
			int count = 0;
			while (true) {
				count++;
				if (!isStrict) {
					if (count >= 100) {
						break;
					}
				} else {
					if (count >= 1000) {
						break;
					}
				}

				int changeCount = 0;

				for (int i = 0; i < 1 + (int) (Math.random() * (tree.getXNum() * tree.getYNum() * 2.0 / 3)); i++) {
					int id1 = (int) (Math.random() * n);
					int id2 = (id1 + 1 + (int) (Math.random() * (n - 1))) % n;

					id1++;
					id2++;

					Node node1 = tree.getNode(id1);
					Node node2 = tree.getNode(id2);

					boolean isOk = true;
					for (Node parent : node1.getParents()) {
						if (!parent.isNear(node2)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}
					for (Node child : node1.getChildren()) {
						if (!child.isNear(node2)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}
					for (Node parent : node2.getParents()) {
						if (!parent.isNear(node1)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}
					for (Node child : node2.getChildren()) {
						if (!child.isNear(node1)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}

					tree.changeNodePosition(node1, node2);
					if (!tree.getConstraintChecker().allCheck(tree, false)) {
						tree.changeNodePosition(node1, node2);
					} else {
						changeCount++;
					}
				}

				if (tree.getConstraintChecker().allCheck(tree, false) && !tree.equals(treeTemp)) {
					return tree;
				}
			}

		} else {

			int count = 0;
			while (true) {
				count++;
				if (!isStrict) {
					if (count >= 100) {
						break;
					}
				} else {
					if (count >= 1000) {
						break;
					}
				}

				int testCount = 0;
				double r = new Random().nextDouble();
				int changeCount = (int) Math.ceil(100 * Math.sqrt(r));
				for (int i = 0; i < changeCount; i++) {
					int id1 = (int) (Math.random() * n);
					int id2 = (id1 + 1 + (int) (Math.random() * (n - 1))) % n;

					id1++;
					id2++;

					Node node1 = tree.getNode(id1);
					Node node2 = tree.getNode(id2);

					boolean isOk = true;
					for (Node parent : node1.getParents()) {
						if (!parent.isNear(node2)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}
					for (Node child : node1.getChildren()) {
						if (!child.isNear(node2)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}
					for (Node parent : node2.getParents()) {
						if (!parent.isNear(node1)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}
					for (Node child : node2.getChildren()) {
						if (!child.isNear(node1)) {
							isOk = false;
							break;
						}
					}
					if (!isOk) {
						continue;
					}

					tree.changeNodePosition(node1, node2);
					testCount++;
				}

				if (tree.getConstraintChecker().allCheck(tree, false) && !tree.equals(treeTemp)) {
					return tree;
				}
			}
		}

		return null;
	}

}
