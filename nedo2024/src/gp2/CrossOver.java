package gp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import nedo2024.Node;
import nedo2024.Tree;
import nedo2024.VisualMain;

public class CrossOver {

	public static Tree[] crossMain(Tree tree1, Tree tree2) throws IOException {
		if (Math.random() < 0.5) {
			return cross(tree1, tree2);
		} else {
			return cross2(tree1, tree2);
		}
	}

	public static Tree[] cross3(Tree tree1temp, Tree tree2temp) throws IOException {
		Tree cTrees[] = new Tree[2];
		
		ArrayList<Node> t1 = new ArrayList<Node>();
		t1.addAll(tree1temp);
		Collections.shuffle(t1);
		ArrayList<Node> t2 = new ArrayList<Node>();
		t2.addAll(tree2temp);
		Collections.shuffle(t2);

		Set<Tree> crossedTreesSet = tree1temp.copy().copy2(tree2temp.copy());
		int count = 0;
		for(Tree t: crossedTreesSet){
			//System.out.println("Successful crossover "+t.hashCode());
			cTrees[count] = t;
			count++;
		}
		return cTrees;
		
	}

	public static Tree[] cross2(Tree tree1temp, Tree tree2temp) throws IOException {
		Tree cTrees[] = new Tree[2];
		Set<Tree> crossedTrees = new HashSet<Tree>();

		boolean isMerge = false;
		boolean isSplit = false;
		if (tree1temp.getConstraintChecker().getMaxNumForConst9_merge() > 1) {
			isMerge = true;
		}
		if (tree1temp.getConstraintChecker().getMaxNumForConst9_split() > 1) {
			isSplit = true;
		}

		ArrayList<Node> t1 = new ArrayList<Node>();
		t1.addAll(tree1temp);
		Collections.shuffle(t1);
		ArrayList<Node> t2 = new ArrayList<Node>();
		t2.addAll(tree2temp);
		Collections.shuffle(t2);

		for (Node n1 : t1) {
			if (isSplit) {
				if (n1.getDescendants().size() <= 4) {
					continue;
				}
			} else {
				if (n1.getAncestors().size() <= 4) {
					continue;
				}
			}

			for (Node n2 : t2) {
				if (isSplit) {
					if (n1.getDescendants().size() == n2.getDescendants().size()) {
						if (!n1.equalOnlyStructureDescendants(n2)) {
							// 構造を交換

							Tree tree1 = tree1temp.copy().copy(tree2temp.copy(), n1, n2, true);
							Tree tree2 = tree2temp.copy().copy(tree1temp.copy(), n2, n1, true);

							boolean isOk1 = tree1.fixAll();
							if (isOk1 && !(tree1temp.equals(tree1) || tree2temp.equals(tree1))) {
								tree1.setVal(0);
								crossedTrees.add(tree1);
							}

							if (crossedTrees.size() == 2) {
								int count = 0;
								for (Tree t : crossedTrees) {
									cTrees[count] = t;
									count++;
								}
								return cTrees;
							}

							boolean isOk2 = tree2.fixAll();
							if (isOk2 && !(tree1temp.equals(tree2) || tree2temp.equals(tree2))) {
								tree2.setVal(0);
								crossedTrees.add(tree2);
							}

							if (crossedTrees.size() == 2) {
								int count = 0;
								for (Tree t : crossedTrees) {
									cTrees[count] = t;
									count++;
								}
								return cTrees;
							}

						}
					}
				}
				if (isMerge) {
					if (n1.getAncestors().size() == n2.getAncestors().size() && !n1.equalOnlyStructureAncestors(n2)) {
						// 構造を交換

						Tree tree1 = tree1temp.copy(tree2temp.copy(), n1, n2, false);
						Tree tree2 = tree2temp.copy(tree1temp.copy(), n2, n1, false);

						boolean isOk1 = tree1.fixAll();
						if (isOk1 && !(tree1temp.equals(tree1) || tree2temp.equals(tree1))) {
							tree1.setVal(0);
							crossedTrees.add(tree1);
						}
						if (crossedTrees.size() == 2) {
							int count = 0;
							for (Tree t : crossedTrees) {
								cTrees[count] = t;
								count++;
							}
							return cTrees;
						}

						boolean isOk2 = tree2.fixAll();
						if (isOk2 && !(tree1temp.equals(tree2) || tree2temp.equals(tree2))) {
							tree2.setVal(0);
							crossedTrees.add(tree2);
						}
						if (crossedTrees.size() == 2) {
							int count = 0;
							for (Tree t : crossedTrees) {
								cTrees[count] = t;
								count++;
							}
							return cTrees;
						}
					}
				}
			}
		}

		return new Tree[] { tree1temp, tree2temp };

	}

	public static Tree[] cross(Tree tree1temp, Tree tree2temp) throws IOException {

		Tree tree1 = tree1temp.copy();
		Tree tree2 = tree2temp.copy();

		Random random = new Random();
		int y = tree1.getYNum();
		int x = tree1.getXNum();

		int challengeNum = 100;
		for (int i = 0; i < challengeNum; i++) {
			// System.out.println("challenge " + i);
			int targetX1 = random.nextInt(x);
			int targetX2 = x + random.nextInt(x - targetX1);

			int targetY1 = random.nextInt(y);
			int targetY2 = x + random.nextInt(y - targetY1);

			if ((targetX2 - targetX1) + (targetY2 - targetY1) == x + y - 2) {
				// no sense
				i++;
				continue;
			}

			Tree tree1copy = tree1.copy();
			Tree tree2copy = tree2.copy();

			for (Node node : tree1) {
				if (node.isInExist(targetX1, targetX2, targetY1, targetY2)) {
					node.removeChildrenAll();

					Set<Node> parents = new HashSet<Node>();
					parents.addAll(node.getParents());
					for (Node parent : parents) {
						parent.removeChild(node, false);
					}

				}
			}

			for (Node node : tree2) {
				if (node.isInExist(targetX1, targetX2, targetY1, targetY2)) {
					node.removeChildrenAll();

					Set<Node> parents = new HashSet<Node>();
					parents.addAll(node.getParents());
					for (Node parent : parents) {
						parent.removeChild(node, false);
					}
				}
			}

			for (Node node : tree1) {
				if (node.isInExist(targetX1, targetX2, targetY1, targetY2)) {
					Node correspondingNode = tree2copy.getNode(node.getId());
					for (Node correspondingPrent : correspondingNode.getParents()) {
						tree1.getNode(correspondingPrent.getId()).addChild(node, true, true);
					}
					for (Node correspondingChild : correspondingNode.getChildren()) {
						node.addChild(tree1.getNode(correspondingChild.getId()), true, true);
					}
				}
			}

			for (Node node : tree2) {
				if (node.isInExist(targetX1, targetX2, targetY1, targetY2)) {
					Node correspondingNode = tree1copy.getNode(node.getId());
					for (Node correspondingPrent : correspondingNode.getParents()) {
						tree2.getNode(correspondingPrent.getId()).addChild(node, true, true);
					}
					for (Node correspondingChild : correspondingNode.getChildren()) {
						node.addChild(tree2.getNode(correspondingChild.getId()), true, true);
					}
				}
			}

			boolean isOk1 = tree1.fixAll();
			boolean isOk2 = tree2.fixAll();

			if (isOk1 && isOk2 && !(tree1temp.equals(tree1) || tree2temp.equals(tree2))) {
				String mode = System.getProperty("DEBUG_MODE");
				if (mode != null && mode.equals("debug")) {
					System.out.println("Cross succeess in turn: " + i);
				}

				tree1.setVal(0);
				tree2.setVal(0);
				return new Tree[] { tree1, tree2 };

			}

		}

		String mode = System.getProperty("DEBUG_MODE");
		if (mode != null && mode.equals("debug")) {
			System.out.println("Cross failed");
		}

		return new Tree[] { tree1temp, tree2temp };
	}

}
