package nedo2024;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

public class VisualMain {

	public static void main(String args[]) throws IOException {
		 String tempFileName = "G:\\nedo\\temp4/g3_857479813.xml";// xml file
//		String tempFileName = "G://2025/best_7.74766.xml";// xml file
		visualize_physical(tempFileName);
		visualize_logical(tempFileName);
	}

	public static void visualize(Tree tree) throws IOException {
		String tempFileName = "C://temp/tree.xml";
		Util.saveXml(tree, tempFileName);
		visualize(tempFileName);
		new File(tempFileName).deleteOnExit();
	}

	public static void visualize(String xmlFileName) throws IOException {
		visualize_physical(xmlFileName);
		visualize_logical(xmlFileName);
	}

	public static void visualize_physical(String xmlFileName) throws IOException {

		System.setProperty("org.graphstream.ui", "swing");
		Graph graph = new SingleGraph("TreeVisualization");
		graph.setAttribute("ui.stylesheet",
				"node { size: 50px; text-size: 24; text-color: black; fill-color: lightblue; }"
						+ "edge { arrow-shape: arrow; arrow-size: 10px, 3px; fill-color: black;shape: cubic-curve;  }");

		File inputFile = new File(xmlFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile.getAbsolutePath())));
		int r = -1;
		int c = -1;

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.contains("<conf r")) {
				String ss[] = line.split("\"");
				r = Integer.parseInt(ss[1]);
				c = Integer.parseInt(ss[3]);

				for (int x = 1; x <= c; x++) {
					for (int y = 1; y <= r; y++) {
						int nodeId = y + r * (x - 1);

						org.graphstream.graph.Node node = graph.addNode("" + nodeId);
						node.setAttribute("xyz", x + (r - y) * 0.1, r - y + x * 0.1, 0);
						node.setAttribute("ui.label", "" + nodeId);
					}
				}

			}
			if (line.contains("seg")) {
				String ss[] = line.split("\"");
				String fromNode = ss[1];
				String toNode = ss[3];
				graph.addEdge(fromNode + "-" + toNode, fromNode, toNode, true);
			}
		}

		for (org.graphstream.graph.Node node : graph) {
			node.setAttribute("ui.frozen");
		}

		Viewer viewer = graph.display();
		viewer.disableAutoLayout();

	}

	public static void visualize_logical(Tree tree) throws IOException {
		String xmlFileName = "C://temp/tree" + Math.random() + ".xml";
		Util.saveXml(tree, xmlFileName);
		visualize_logical(xmlFileName);

		new File(xmlFileName).deleteOnExit();

	}

	public static void visualize_physical(Tree tree) throws IOException {
		String xmlFileName = "C://temp/tree" + Math.random() + ".xml";
		Util.saveXml(tree, xmlFileName);
		visualize_physical(xmlFileName);

		new File(xmlFileName).deleteOnExit();

	}

	public static void visualize_logical(String xmlFileName) throws IOException {

		File inputFile = new File(xmlFileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile.getAbsolutePath())));
		String tempFileName1 = inputFile.getAbsolutePath().split("\\.xml")[0] + "_1.dot";
		BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFileName1)));
		bw1.write("digraph graph_name{");
		bw1.newLine();

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.contains("seg")) {
				String ss[] = line.split("\"");
				String fromNode = ss[1];
				String toNode = ss[3];
				bw1.write(fromNode + " -> " + toNode + ";");
				bw1.newLine();
			}
		}

		bw1.write("}");
		bw1.close();
		br.close();

		String pngFileName1 = inputFile.getAbsolutePath().split("\\.xml")[0] + ".png";
		String command1 = "dot -T png " + new File(tempFileName1).getAbsolutePath() + " -o " + pngFileName1;

		Runtime runtime = Runtime.getRuntime();
		Process p = null;
		try {
			p = runtime.exec(command1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 最大待機時間（ミリ秒）
		long timeout = 10000; // 10秒
		long startTime = System.currentTimeMillis();

		while (!new File(pngFileName1).exists()) {
			if (System.currentTimeMillis() - startTime > timeout) {
				System.out.println("Timeout: File not found.");
				return;
			}

			try {
				Thread.sleep(100); // 100ミリ秒待機（負荷軽減）
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}

		try {
			p = runtime.exec(new String[] { "cmd.exe", "/c", "start", pngFileName1 });

		} catch (IOException e) {
			e.printStackTrace();
		}

		new File(tempFileName1).deleteOnExit();

	}

}
