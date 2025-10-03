package nedo2024;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import gp2.Mutation;

public class Util {

	public static void main(String args[]) throws IOException {
		// boolean constraints[] = { false, true, true, true, true, true, true, false,
		// true, true, true };

		System.setProperty("APP_MODE", "debug");
		for (int turn = 0; turn < 100; turn++) {
			boolean constraints[] = new boolean[11];
			constraints[0] = false;
			constraints[1] = true;
			constraints[2] = true;
			for (int i = 3; i < 11; i++) {
				if (Math.random() < 0.5) {
					constraints[i] = true;
				} else {
					constraints[i] = false;
				}
			}
			ConstraintChecker checker = new ConstraintChecker(3, 1, 3, 3, 1, constraints);
			Tree tree = initialTree(12, 3, checker);
		}
	}

	public static List<Tree> getPreparedTrees(String folderName, ConstraintChecker checker) throws IOException {
		List<Tree> trees = new ArrayList<Tree>();
		File folder = new File(folderName);
		File files[] = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				String fileName = file.getAbsolutePath();
				Tree tree = Tree.getTreeFromInterface(fileName, checker);
				trees.add(tree);
			}
			System.err.println("*** Prepared " + trees.size() + " trees are set. ***");
		} else {
			System.err.println("*** Xml files don't exsit at the specified folder.");
		}

		return trees;
	}

	public static Tree initialTree(int yNum, int xNum, ConstraintChecker checker, Set<Tree> existings)
			throws IOException {
		Tree tree = null;
		int testCount = 0;
		while (true) {
			tree = initialTree(yNum, xNum, checker);
			if (!existings.contains(tree)) {
				String mode = System.getProperty("DEBUG_MODE");
				if (mode != null && mode.equals("debug")) {
					System.out.println("generate new tree trial: " + testCount);
				}
				break;
			}
			if (testCount > 100) {
				System.out.println("Warning coce: 2001");
				return initialTree(yNum, xNum, checker);
			}
			testCount++;
		}

		return tree;

	}

	public static Tree initialTree(int yNum, int xNum, ConstraintChecker checker) throws IOException {

		boolean isOk = false;
		Tree tree = null;

		int testCount = 0;

		while (!isOk) {
			testCount++;
			if (testCount > 100) {
//				System.out.println("Can't create tree error. Please contact Sei.");
			}
			tree = new Tree(yNum, xNum, checker);

			for (int x = 1; x <= xNum; x++) {
				for (int y = 1; y <= yNum; y++) {
					Node n = new Node(x, y, tree);
					tree.add(n);
				}
			}

			boolean constraints[] = checker.getConstraints();

			ArrayList<ArrayList<Node>> nodeGroups = getNodeGroups(tree, constraints);
			randomConnect(nodeGroups, constraints);

			// VisualMain.visualize_logical(tree);

			if (!constraints[7]) {
				double connectProb = 0.5 + Math.random() * 0.5;
				List<Node> nodes1 = Util.getShuffledList(tree);
				List<Node> nodes2 = Util.getShuffledList(tree);
				for (Node node1 : nodes1) {
					for (Node node2 : nodes2) {
						if (node1 != node2) {
							if (Math.random() < connectProb) {
								node1.addChild(node2, true, true);
							}
						}
					}
				}
			}

			int maxChallengeNum = 10;
			int count = 0;
			while (true) {
				count++;
				isOk = tree.fixAll();
				Tree tree2 = null;
				if (isOk) {
					break;
				} else {
					tree2 = Mutation.changePosition(tree, null);
//					tree2 = Mutation.changePosition(tree, false);
				}
				if (tree2 != null) {
					tree = tree2;
				}
				if (tree2 == null || count >= maxChallengeNum) {
					String mode = System.getProperty("DEBUG_MODE");
					if (mode != null && mode.equals("debug")) {
						// System.out.println("Warning code: 1001");
						// VisualMain.visualize(tree);
						// tree.fixAll();
					}
					break;
				}

			}

			if (tree.checker.allCheck(tree, false)) {
				isOk = true;
			} else {
				isOk = false;
			}

			if (isOk) {
				for (int i = 0; i < 10; i++) {
					if (tree.getMinLogicalDistance() > 1) {
						break;
					}
					tree = adjustLength(tree);
				}
				if (tree.getMinLogicalDistance() == 1) {
					for (int i = 0; i < 100; i++) {
						tree = adjustLength(tree);
						Tree tree2 = Mutation.changePosition(tree, null);
						if (tree2 == null) {
							break;
						} else {
							tree = tree2;
						}

//						tree = Mutation.changePosition(tree, false);
						if (tree.getMinLogicalDistance() > 1) {
							break;
						}
					}
				}

				int count2 = 0;
				while (true) {
					count2++;
					boolean isOk0 = tree.fix(0);
					boolean isOkAll = tree.fixAll();
					if (isOk0 && isOkAll) {
						break;
					}
					if (count2 >= 10) {
						break;
					}
				}
			}

			if (tree.checker.allCheck(tree, false)) {
				isOk = true;
			} else {
				isOk = false;
			}

		}

		return tree;

	}

	// logical distanceが1～2のものを他のノードにくっつけて調整したい。
	public static Tree adjustLength(Tree treeTemp) throws IOException {

		Tree tree = treeTemp.copy();

		Tree copyTree = tree.copy();

		for (Node node : tree) {
			boolean isOk = false;
			if (node.isLeaf() && node.getLogicalDistancesFromInlets().first() == 1) {

				if (node.getParents().size() >= 2) {
					int parentCount = node.getParents().size();
					// 親が複数あるなら，one stepでinletからつながっているところ削除して良いのでは．
					Set<Node> parents = new HashSet<Node>();
					parents.addAll(node.getParents());
					for (Node parent : parents) {
						if (parent.getLogicalDistance(node) == 1 && parent.getChildren().size() >= 2) {
							parent.removeChild(node, true);
							parentCount--;
							if (parentCount == 1) {
								break;
							}
							// debug
							for (Node n1 : tree) {
								if (n1.getParents().size() == 0 && n1.getChildren().size() == 0) {
									System.out.println("Warning code: teltki");
								}
							}

						}
					}
				} else {
					// inletからnodeにつながっていて、inletがnodeの唯一のparent。
					// inletから強制removeして、ほかのどこかにくっつけたい
					// inletもlonlyNodeになるような場合は、ここでは何もしない。
					Node inlet = node.getParents().iterator().next();
					if (inlet.getChildren().size() > 1) {
						inlet.removeChild(node, false);

						boolean isTempOk = randomConnect(tree, node, inlet);

						if (!isTempOk) {
							// TODO 一旦断念するけどなんとかしたい。
							inlet.addChild(node, false, false);
						}
					}

				}

				if (tree.getInlets().size() == 1) {

					ArrayList<Node> parentCandidates = new ArrayList<Node>();
					parentCandidates.addAll(tree);
					Collections.shuffle(parentCandidates);

					for (Node parentCandidate : parentCandidates) {
						if (parentCandidate == node || tree.getInlets().contains(parentCandidate)) {
							continue;
						}
						isOk = parentCandidate.addChild(node, true, false);
						if (isOk) {
							if (tree.getInlets().iterator().next().removeChild(node, true)) {
								break;
							} else {
								isOk = false;
							}
						}
					}
				} else {

					ArrayList<Node> parents = new ArrayList<Node>();
					parents.addAll(node.getParents());
					Collections.shuffle(parents);

					for (Node parent : parents) {
						if (parent.getLogicalDistance(node) == 1) {

							ArrayList<Node> parentParentCandidates = new ArrayList<Node>();
							parentParentCandidates.addAll(tree);
							Collections.shuffle(parentParentCandidates);

							for (Node parentParentCandidate : parentParentCandidates) {
								if (parentParentCandidate == node || parentParentCandidate == parent
										|| tree.getInlets().contains(parentParentCandidate)) {
									continue;
								}

								isOk = parentParentCandidate.addChild(parent, true, false);
								if (isOk) {
									break;
								}

							}
						}
						if (isOk) {
							break;
						} else {

							Node inletOfNode = null;
							for (Node inlet : tree.getInlets()) {
								if (node.getParents().contains(inlet)) {
									inletOfNode = inlet;
									break;
								}
							}

							if (inletOfNode != null) {
								if (node.getParents().size() > 1) {
									inletOfNode.removeChild(node, false);
									if (inletOfNode.isLeaf()) {
										boolean isTempOk = randomConnect(tree, inletOfNode, node);
										if (!isTempOk) {
											// TODO 一旦断念するけどなんとかしたい。
											inletOfNode.addChild(node, false, false);
										}
									}
								}
							}
						}
					}
				}

			}
		}

		if (!tree.checker.allCheck(tree, false)) {
			// ダメになる理由がわからないんだけど、とりあえず戻しておく
			tree = copyTree;
		} else {
			copyTree = tree.copy();
		}

		for (Node node : tree) {
			if (tree.getInlets().contains(node) && node.getChildren().size() == 1) {
				Node child = node.getChildren().iterator().next();
				if (child.isLeaf()) {
					ArrayList<Node> targets = new ArrayList<Node>();
					targets.addAll(tree);
					Collections.shuffle(targets);

					node.removeChild(child, false);
					boolean isOk = false;

					for (Node parentCandidate : targets) {
						isOk = parentCandidate.addChild(node, true, false);
						if (isOk) {
							break;
						}
					}
					if (isOk) {
						break;
					}
					for (Node childCandidate : targets) {
						isOk = node.addChild(childCandidate, true, false);
						if (isOk) {
							break;
						}
					}

					if (isOk) {
						break;
					} else {
						// 断念
						node.addChild(child, false, false);
					}

				}
			}

		}

		if (!tree.checker.allCheck(tree, false)) {
			// ダメになる理由がわからないんだけど、とりあえず戻しておく
			tree = copyTree;
		} else {
			copyTree = tree.copy();
		}

		// length=1のleaf
		// node（inletから直接つながっている）でparentsがそのinletのみのノードが2個以上存在し、かつ、そのinletたちは複数の子がいる状態。このとき、これらのleaf
		// nodeが物理的に近い状態にいてaddできそうならつないで、どこかのノードにつなげる。
		Set<Node> cands = new HashSet<Node>();
		for (Node inlet : tree.getInlets()) {
			for (Node outlet : tree.getOutlets()) {
				// とりあえずinlet->outletになっていて、親がその1つだけのoutletを全て収集。
				if (inlet.getLogicalDistance(outlet) == 1 && outlet.getParents().size() == 1) {
					cands.add(outlet);
				}
			}
		}
		// 収集したノードたちしか子にない場合、ランダムに1つのノードを対象から除外。
		for (Node inlet : tree.getInlets()) {
			Set<Node> temp = new HashSet<Node>();
			temp.addAll(inlet.getChildren());
			temp.removeAll(cands);
			if (temp.size() == 0) {
				cands.remove(inlet.getChildren().iterator().next());
			}
		}
		if (cands.size() >= 2) {
			Set<Node> groups = connectNodes(cands);
			for (Node node : groups) {
				ArrayList<Node> inlets = new ArrayList<Node>();
				ArrayList<Node> nodes = new ArrayList<Node>();

				nodes.add(node);
				inlets.add(node.getParents().iterator().next());
				for (Node n1 : node.getDescendants()) {
					nodes.add(n1);
					for (Node parent : n1.getParents()) {
						if (!node.equals(parent) && tree.getInlets().contains(parent)) {
							inlets.add(parent);
						}
					}
				}

				for (int i = 0; i < inlets.size(); i++) {
					inlets.get(i).removeChild(nodes.get(i), false);
				}

				boolean isTempOk = randomConnect(tree, node, null);
				if (!isTempOk) {
					// 断念
					for (int i = 0; i < inlets.size(); i++) {
						inlets.get(i).addChild(nodes.get(i), false, false);
					}
				}

			}
		}

		if (!tree.checker.allCheck(tree, false)) {
			// ダメになる理由がわからないんだけど、とりあえず戻しておく
			tree = copyTree;
		}

		return tree;

	}

	/**
	 * cands に含まれるノードを、isNear の関係でできるだけ長い直列チェインにまとめ、 それぞれのチェインの先頭ノードを返す。
	 * ただしチェイン長が1（子も親も持たない孤立ノード）のものは出力しない。
	 *
	 * @param cands 候補ノードの集合
	 * @return 直列につながった各グループの「先頭ノード」集合
	 * @throws IOException
	 */
	public static Set<Node> connectNodes(Set<Node> cands) throws IOException {
		// 子ノードに選ばれたノードを記録
		Set<Node> hasParent = new HashSet<>();
		// 最終的に返却する、チェイン先頭ノードの集合
		Set<Node> headNodes = new HashSet<>();

		for (Node node : cands) {
			// すでに誰かの子になっていたら先頭にはできない
			if (hasParent.contains(node))
				continue;

			Node current = node;
			boolean linked = false; // 子ノードを1つでも追加できたか

			// 現在の current からさらに近傍ノードを探して直列チェインを伸ばす
			while (true) {
				Node next = null;
				for (Node cand : cands) {
					// まだ子にされておらず、自分自身ではなく、かつ近ければ選択
					if (!hasParent.contains(cand) && cand != current && current.isNear(cand)) {
						next = cand;
						break;
					}
				}
				if (next != null) {
					current.addChild(next, false, false);
					hasParent.add(next);
					linked = true;
					current = next;
				} else {
					break; // これ以上伸ばせない
				}
			}

			// １つでも子をつなげたチェインなら、先頭として結果に含める
			if (linked) {
				headNodes.add(node);
			}
		}

		return headNodes;
	}

	// inletからのaddChildは不可とする。。
	public static boolean randomConnect(Tree tree, Node node, Node excludingNode) throws IOException {
		ArrayList<Node> targets = new ArrayList<Node>();
		targets.addAll(tree);
		if (excludingNode != null) {
			targets.remove(excludingNode);
		}
		Collections.shuffle(targets);

		for (Node target : targets) {
			if (tree.getInlets().contains(target)) {
				continue;
			}
			if (target.addChild(node, true, false)) {
				return true;
			}
		}

		for (Node target : targets) {
			if (node.addChild(target, true, false)) {
				return true;
			}
		}
		return false;

	}

	public static void randomConnect(ArrayList<ArrayList<Node>> nodeGroups, boolean constraints[]) throws IOException {
		// TODO determine the probability
		double connectProb = 0.9;

		int size = nodeGroups.size();
		Random rand = new Random();

		Collections.shuffle(nodeGroups);

		if (constraints[7]) {
			for (int i = 0; i < size; i++) {
				if (Math.random() > connectProb) {
					continue;
				}

				ArrayList<Node> sourceGroup = nodeGroups.get(i);
				Node sourceNode = sourceGroup.get(sourceGroup.size() - 1);

				for (ArrayList<Node> nodeGroup : nodeGroups) {
					if (sourceGroup == nodeGroup) {
						continue;
					}
					if (Math.random() > connectProb) {
						continue;
					}
					Node targetNode = nodeGroup.get(0);
					boolean isConnected = sourceNode.addChild(targetNode, true, true);
				}

				// if (rand.nextDouble() < connectProb) {
				// for (ArrayList<Node> nodeGroup : nodeGroups) {
				// if (sourceGroup == nodeGroup) {
				// continue;
				// }
				// Node targetNode = nodeGroup.get(0);
				// boolean isConnected = sourceNode.addChild(targetNode, true, true);
				//
				// }
				// }
			}

		} else {
			for (int i = 0; i < size; i++) {
				if (Math.random() > connectProb) {
					continue;
				}
				ArrayList<Node> sourceGroup = nodeGroups.get(i);
				Node sourceNode = sourceGroup.get(sourceGroup.size() - 1);
				if (rand.nextDouble() < connectProb) {
					int randomIndex = rand.nextInt(size - 1);
					int targetGroupId = (randomIndex >= i) ? randomIndex + 1 : randomIndex;
					ArrayList<Node> targetGroup = nodeGroups.get(targetGroupId);
					Node targetNode = targetGroup.get(0);
					if (constraints[7]) {
						boolean isConnected = sourceNode.addChild(targetNode, true, false);
					} else {
						boolean isConnected = sourceNode.addChild(targetNode, true, true);
					}
				}
			}
		}

	}

	public static ArrayList<ArrayList<Node>> getNodeGroups(Tree tree, boolean constraints[]) throws IOException {
		ArrayList<ArrayList<Node>> groups = new ArrayList<ArrayList<Node>>();
		Random random = new Random();
		if (constraints[7]) {
			for (int i = 0; i < tree.size(); i += 2) {
				ArrayList<Node> nodes = new ArrayList<Node>();
				if (random.nextBoolean()) {
					tree.get(i).addChild(tree.get(i + 1), false, false);
					nodes.add(tree.get(i));
					nodes.add(tree.get(i + 1));
				} else {
					tree.get(i + 1).addChild(tree.get(i), false, false);
					nodes.add(tree.get(i + 1));
					nodes.add(tree.get(i));
				}
				groups.add(nodes);
			}
			return groups;
		}

		if (constraints[3] || !constraints[3]) {
			HashSet<Integer> remainingIds = new HashSet<Integer>();
			for (int i = 0; i < tree.size(); i++) {
				remainingIds.add(i);
			}

			for (int i = 0; i < tree.size() / 2; i++) {
				ArrayList<Node> nodes = new ArrayList<Node>();
				// Randomly get two integers.
				List<Integer> list = new ArrayList<>(remainingIds);
				int index1 = random.nextInt(list.size());
				int index2;
				do {
					index2 = random.nextInt(list.size());
				} while (index1 == index2);

				int num1 = list.get(index1);
				int num2 = list.get(index2);
				remainingIds.remove(num1);
				remainingIds.remove(num2);

				tree.get(num1).addChild(tree.get(num2), true, true);
				nodes.add(tree.get(num1));
				nodes.add(tree.get(num2));

				groups.add(nodes);
			}
			return groups;
		}

		return groups;
	}

	public static void saveXml(Tree tree, String fileName) throws IOException {

		int rowNum = tree.getYNum();
		int columnNum = tree.getXNum();
		String xmlOther = tree.getOther();

		String xml = "<hex>\n";
		xml += "<conf r=\"" + rowNum + "\" c=\"" + columnNum + "\"/>\n";

		for (Node node : tree) {
			for (Node child : node.getChildren()) {
				xml += "<seg in=\"" + node.getId() + "\" out=\"" + child.getId() + "\"/>\n";
			}
		}

		if (xmlOther != null && !xmlOther.equals("")) {
			xml += xmlOther;
		} else {
			xml += "<ref mode=\"CSV\" name=\"R410A.CSV\"/><air mode=\"CSV\" name=\"AIR.CSV\"/><hx_type type=\"2\" /><correlation type=\"1\" /><param type=\"double\" name=\"tube_D_o\" val=\"0.0085\"/><param type=\"double\" name=\"tube_D_i\" val=\"0.006722\"/><param type=\"double\" name=\"tube_L\" val=\"0.5\"/><param type=\"double\" name=\"tube_T\" val=\"0.000889\"/><param type=\"double\" name=\"tube_Hspace\" val=\"0.0173\"/><param type=\"double\" name=\"tube_Vspace\" val=\"0.02\"/><param type=\"double\" name=\"tube_beta\" val=\"0.0\"/><param type=\"double\" name=\"tube_k\" val=\"0.385\"/><param type=\"double\" name=\"fin_FPM\" val=\"0.0\"/><param type=\"double\" name=\"fin_P\" val=\"0.0\"/><param type=\"double\" name=\"fin_S\" val=\"0.0011\"/><param type=\"double\" name=\"fin_T\" val=\"0.0001\"/><param type=\"double\" name=\"fin_k\" val=\"0.205\"/><param type=\"double\" name=\"G_a_total\" val=\"0.0\"/><param type=\"double\" name=\"T_a_inlet\" val=\"26.0\"/><param type=\"double\" name=\"P_a_inlet\" val=\"101.325\"/><param type=\"double\" name=\"V_a_inlet\" val=\"0.0\"/><param type=\"double\" name=\"VFR_a_inlet\" val=\"0.6\"/><param type=\"double\" name=\"G_r_total\" val=\"0.025\"/><param type=\"double\" name=\"P_r_inlet\" val=\"1035.84\"/><param type=\"double\" name=\"P_r_outlet\" val=\"259.14\"/><param type=\"double\" name=\"h_r_inlet\" val=\"240.68\"/><param type=\"double\" name=\"h_r_outlet\" val=\"230.0\"/><param type=\"double\" name=\"T_r_cond\" val=\"40.0\"/>";
			xml += "</hex>";

		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		bw.write(xml);
		bw.close();
	}

	// Setをランダムに並び替えたリストを取得するメソッド
	public static List<Node> getShuffledList(Set<Node> nodes) {
		List<Node> nodeList = new ArrayList<>(nodes); // Setからリストに変換
		Collections.shuffle(nodeList); // リストをシャッフル
		return nodeList;
	}

	// シャッフル後のリストを返すメソッド
	public static List<Node> getShuffledList(List<Node> originalList) {
		List<Node> copiedList = new ArrayList<>(originalList); // リストをコピー
		Collections.shuffle(copiedList); // コピーしたリストをシャッフル
		return copiedList;
	}

	public static Set<Node> getRandomNodes(Set<Node> nodes, int size) {
		if (size <= 0 || nodes.isEmpty()) {
			return Collections.emptySet();
		}

		List<Node> nodeList = new ArrayList<>(nodes);
		Collections.shuffle(nodeList, new Random());

		return new HashSet<>(nodeList.subList(0, Math.min(size, nodeList.size())));
	}

	// public static void extractContent(Node node, StringBuilder result,
	// Set<String> excludeTags) {
	// if (node.getNodeType() == Node.ELEMENT_NODE) {
	// String nodeName = node.getNodeName();
	//
	// // 除外するタグの場合は処理しない
	// if (excludeTags.contains(nodeName)) {
	// return;
	// }
	//
	// // タグ名と属性情報を追加
	// result.append("<").append(nodeName);
	// NamedNodeMap attributes = node.getAttributes();
	// for (int i = 0; i < attributes.getLength(); i++) {
	// Node attr = attributes.item(i);
	// result.append("
	// ").append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append("\"");
	// }
	// result.append("/>\n");
	//
	// // 子ノードの処理
	// NodeList childNodes = node.getChildNodes();
	// for (int i = 0; i < childNodes.getLength(); i++) {
	// extractContent(childNodes.item(i), result, excludeTags);
	// }
	// }
	// }
}
