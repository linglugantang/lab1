package demo;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MyGraph extends JFrame {

    private Graph graph;
    private Viewer viewer;
    private ViewPanel viewPanel;

    public MyGraph(String filePath) {
        super("Text to Graph");

        // 创建图对象
        graph = new SingleGraph("Text Graph");

        try {
            readTextFile(filePath); // 从指定文件路径读取文本数据并生成图
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        // 设置节点样式
        for (org.graphstream.graph.Node node : graph) {
            node.setAttribute("ui.label", node.getId()); // 设置节点内显示为节点文本信息
            node.setAttribute("ui.style", "shape:circle;size:30px;fill-color:blue; text-color:black; text-size:16px;"); // 设置节点样式，增大字体大小
        }

        // 设置边的样式
        for (org.graphstream.graph.Edge edge : graph.getEachEdge()) {
            int weight = edge.getAttribute("ui.label"); // 获取边的权重

            // 设置边的权重字体大小和颜色
            edge.setAttribute("ui.style", "text-size:" + (12 + weight) + "px; text-color:black;");
        }
    }

    private void readTextFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String content;
        String previousWord = null;

        StringBuilder stringBuilder = new StringBuilder();

        while ((content = br.readLine()) != null) {
            stringBuilder.append(content);
        }

        br.close();
        content = stringBuilder.toString().toLowerCase().replaceAll("[^a-z ]", " ").trim();
        String[] words = content.split(" ");

        String[] filteredWords = Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .toArray(String[]::new);

        for (String word : filteredWords) {
            if (graph.getNode(word) == null) {
                graph.addNode(word);
            }
        }

        for (String word : words) {
            if (word.isEmpty()) continue;
            if (previousWord != null) {
                String edgeId = previousWord + "->" + word;
                if (graph.getEdge(edgeId) == null) {
                    graph.addEdge(edgeId, previousWord, word, true).setAttribute("ui.label", 1);
                } else {
                    int weight = Integer.parseInt(graph.getEdge(edgeId).getAttribute("ui.label").toString());
                    graph.getEdge(edgeId).setAttribute("ui.label", weight + 1);
                }
            }
            previousWord = word;
        }
    }

    public void showDirectedGraph() {
        if (viewer == null) {
            viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
            viewer.enableAutoLayout();
            viewPanel = viewer.addDefaultView(false);
            viewPanel.setPreferredSize(new Dimension(800, 600));
            getContentPane().add(viewPanel, BorderLayout.CENTER);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
        }
        setVisible(true);
    }

    public String queryBridgeWords(String word1, String word2) {
        if (graph.getNode(word1) == null || graph.getNode(word2) == null) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridgeWords = new ArrayList<>();
        for (org.graphstream.graph.Edge edge : graph.getNode(word1).getEdgeSet()) {
            String bridgeWord = edge.getOpposite(graph.getNode(word1)).getId();
            if (graph.getNode(bridgeWord).hasEdgeBetween(word2)) {
                bridgeWords.add(bridgeWord);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"";
        } else {
            return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " + String.join(", ", bridgeWords) + ".";
        }
    }

    public String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().replaceAll("[^a-z ]", " ").trim().split("\\s+");
        if (words.length < 2) {
            return inputText; // 如果少于两个单词，直接返回输入文本
        }

        StringBuilder newText = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]).append(" ");
            String word1 = words[i];
            String word2 = words[i + 1];

            if (graph.getNode(word1) == null || graph.getNode(word2) == null) {
                continue; // 如果图中不存在其中一个单词，跳过
            }

            List<String> bridgeWords = new ArrayList<>();
            for (org.graphstream.graph.Edge edge : graph.getNode(word1).getEdgeSet()) {
                String bridgeWord = edge.getOpposite(graph.getNode(word1)).getId();
                if (graph.getNode(bridgeWord).hasEdgeBetween(word2)) {
                    bridgeWords.add(bridgeWord);
                }
            }

            if (!bridgeWords.isEmpty()) {
                String bridgeWord = bridgeWords.get(rand.nextInt(bridgeWords.size()));
                newText.append(bridgeWord).append(" ");
            }
        }
        newText.append(words[words.length - 1]); // 添加最后一个单词

        return newText.toString();
    }

    public String calcShortestPath(String word1, String word2) {
        // 生成图复位
        for (org.graphstream.graph.Edge edge : graph.getEachEdge()) {
            int weight = edge.getAttribute("ui.label");
            edge.setAttribute("ui.style", "text-size:" + (12 + weight) + "px; text-color:black; fill-color:black; size:1px;");
        }

        if (word1.equals(""))
        {
            if(!word2.equals("")&&graph.getNode(word2)!=null)
            {
                for(org.graphstream.graph.Node node : graph)
                {

                    String nodeName = node.getId();
                    if(word2.equals(nodeName))
                        continue;
                    String path = calcShortestPath(word2,nodeName);
                    System.out.println(path);
                }
                return "";
            }
            else
                return "No word1: " + word1 + " or word2: " + word2 + " in the graph!";

        }

        if (word2.equals(""))
        {
            if(!word1.equals("")&&graph.getNode(word1)!=null)
            {
                for(org.graphstream.graph.Node node : graph)
                {
                    String nodeName = node.getId();
                    if(word1.equals(nodeName))
                        continue;
                    String path = calcShortestPath(word1,nodeName );
                    System.out.println(path);
                }
                return "";
            }
            else
                return "No word1: " + word1 + " or word2: " + word2 + " in the graph!";
        }

        if ((!word2.equals(""))&&!word2.equals("")&&graph.getNode(word2) == null&&graph.getNode(word1) == null) {
            return "No " + word1 + " or " + word2 + " in the graph!";

        }

        Map<String, Integer> distances = new HashMap<>();//每个节点到起始节点的距离
        Map<String, String> previousNodes = new HashMap<>();//每个节点在最短路径中的前一个节点
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (org.graphstream.graph.Node node : graph) {
            distances.put(node.getId(), Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        queue.add(word1);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDistance = distances.get(current);

            if (current.equals(word2)) {
                break;
            }

            for (org.graphstream.graph.Edge edge : graph.getNode(current).getLeavingEdgeSet()) { // Traverse leaving edges
                String neighbor = edge.getTargetNode().getId(); // Get the target node of the edge
                int weight = edge.getAttribute("ui.label");
                int newDistance = currentDistance + weight;

                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!previousNodes.containsKey(word2)) {
            return "No path found from \"" + word1 + "\" to \"" + word2 + "\"";
        }

        List<String> path = new ArrayList<>();
        for (String at = word2; at != null; at = previousNodes.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        // Highlight the shortest path
        for (int i = 0; i < path.size() - 1; i++) {
            String edgeId = path.get(i) + "->" + path.get(i + 1);
            org.graphstream.graph.Edge edge = graph.getEdge(edgeId);
            if (edge != null) {
                edge.setAttribute("ui.style", "fill-color:red;size:3px;");
            }
        }

        int totalWeight = distances.get(word2);

        return "The shortest path from \"" + word1 + "\" to \"" + word2 + "\" is: " + String.join(" -> ", path) + " with a total weight of " + totalWeight;
    }


    public String randomWalk() {
        List<String> visitedNodes = new ArrayList<>(); // 用于存储访问过的节点的列表
        Set<String> visitedEdges = new HashSet<>(); // 用于存储访问过的边的集合
        Random rand = new Random();
        Scanner scanner = new Scanner(System.in);

        // 随机选择一个起点
        org.graphstream.graph.Node currentNode = (org.graphstream.graph.Node) graph.getNodeSet().toArray()[rand.nextInt(graph.getNodeCount())];
        visitedNodes.add(currentNode.getId());
        System.out.println("Starting node: " + currentNode.getId());

        while (true) {
            System.out.print("Continue walking? (yes/no): ");
            String response = scanner.nextLine().trim();

            if (response.equalsIgnoreCase("no")) {
                break; // 用户选择停止遍历
            } else if (!response.equalsIgnoreCase("yes")) {
                System.out.println("Invalid input, please enter 'yes' or 'no'.");
                continue; // 重新提示用户输入
            }

            List<org.graphstream.graph.Edge> leavingEdges = new ArrayList<>(currentNode.getLeavingEdgeSet());

            if (leavingEdges.isEmpty()) {
                System.out.println("The current node has no outgoing edges, ending the walk.");
                break; // 当前节点没有出边，结束遍历
            }

            org.graphstream.graph.Edge edge = leavingEdges.get(rand.nextInt(leavingEdges.size()));
            String edgeId = edge.getId();

            // 检查是否已经访问过该边
            if (visitedEdges.contains(edgeId)) {
                System.out.println("Encountered a repeated edge, ending the walk.");
                break; // 如果出现重复边，结束遍历
            }

            visitedEdges.add(edgeId);
            currentNode = edge.getTargetNode();
            visitedNodes.add(currentNode.getId());

            // 打印当前经过的所有结点
            System.out.println("Current path: " + String.join(" -> ", visitedNodes));
        }

        // 将遍历结果保存到文件
        StringBuilder result = new StringBuilder();
        for (String nodeId : visitedNodes) {
            result.append(nodeId).append(" ");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\STUDY\\软工\\random_walk.txt"))) {
            writer.write(result.toString().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Random walk completed. Nodes visited: " + String.join(" -> ", visitedNodes);
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String filePath = "F:\\STUDY\\软工\\lab1.txt";  // Modified to take user input for file path

        MyGraph myGraph = new MyGraph(filePath);

        while (true) {
            System.out.println("Please select an option:");
            System.out.println("1. Display Graph");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate Text with Bridge Words");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Random Walk");
            System.out.println("6. Exit");
            String choice = scanner.nextLine().trim();

            if (choice.equals("6")) {
                System.out.println("Exiting program.");
                break;
            }

            switch (choice) {
                case "1":
                    myGraph.showDirectedGraph();
                    break;
                case "2":
                    System.out.print("Enter the first word: ");
                    String word1 = scanner.nextLine().trim();
                    System.out.print("Enter the second word: ");
                    String word2 = scanner.nextLine().trim();
                    String result = myGraph.queryBridgeWords(word1, word2);
                    System.out.println(result);
                    break;
                case "3":
                    System.out.print("Enter a line of new text: ");
                    String inputText = scanner.nextLine().trim();
                    String newText = myGraph.generateNewText(inputText);
                    System.out.println("Generated text: " + newText);
                    break;
                case "4":
                    System.out.print("Enter the first word: ");
                    word1 = scanner.nextLine().trim();
                    System.out.print("Enter the second word: ");
                    word2 = scanner.nextLine().trim();
                    String path = myGraph.calcShortestPath(word1, word2);
                    System.out.println(path);
                    break;
                case "5":
                    String walkResult = myGraph.randomWalk();
                    System.out.println(walkResult);
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
                    break;
            }
        }
       System.out.print("C4");
        scanner.close();
    }
}



