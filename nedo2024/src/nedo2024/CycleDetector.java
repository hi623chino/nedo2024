package nedo2024;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CycleDetector {
	private static Set<Node> visited = new HashSet<>();
	private static Deque<Node> stack = new ArrayDeque<>();
	private static List<Integer> cycleIds = new ArrayList<>();

	public static List<Integer> detectCycle(Node startNode) {
		// 初期化
		visited.clear();
		stack.clear();
		cycleIds.clear();

		if (dfs(startNode)) {
			return cycleIds;
		}
		return Collections.emptyList(); // サイクルが見つからない場合は空のリスト
	}

	private static boolean dfs(Node node) {
		if (stack.contains(node)) {
			// サイクル検出時にスタックの内容をリストに収集
			boolean collecting = false;
			for (Iterator<Node> it = stack.descendingIterator(); it.hasNext();) {
				Node n = it.next();
				if (n == node) {
					collecting = true; // サイクルの開始点に到達
				}
				if (collecting) {
					cycleIds.add(n.getId());
				}
			}
			return true;
		}
		if (visited.contains(node)) {
			return false;
		}

		visited.add(node);
		stack.push(node);

		for (Node child : node.getChildren()) {
			if (dfs(child)) {
				return true; // サイクルを見つけた場合
			}
		}

		stack.pop();
		return false;
	}
}
