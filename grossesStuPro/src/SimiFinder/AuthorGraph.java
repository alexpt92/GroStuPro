package SimiFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuthorGraph {
	Map<Node, ArrayList<Edge>> graph;
	GraphLookup lookupGraph;

	AuthorGraph() {		
		this.lookupGraph = new GraphLookup();
		this.graph = new HashMap<Node, ArrayList<Edge>>();
	}
	
	Node getNode(String str){
		if (lookupGraph.map.containsKey(str)){
			return lookupGraph.map.get(str);
		}		
		return null;		
	}
	
	void addNode(String str) {
		//kann beim durchlaufen der dblp ausgefuehrt werden
		Node tmp = new Node(str);
		graph.put(tmp, new ArrayList<Edge>());
		lookupGraph.map.put(str, tmp);
	}

	void addEdge(String str, Double d){	
		//darf nur ausgefuehrt werden, wenn alles geparst ist
		
		
	}
	Edge[] getEdges(String stream) {
		int index = 0;
		if (graph.get(lookupGraph.map.get(stream)) != null) {

			Edge[] returnArray = new Edge[graph
					.get(lookupGraph.map.get(stream)).size()];
			for (Edge e : graph.get(lookupGraph.map.get(stream))) {
				returnArray[index++] = e;
			}

			return returnArray;
		}
		return null;
	}

}

class Node {
	String name;

	Node(String str) {
		this.name = str;
	}
}

class Edge {
	Node target;
	Double strength;

	Edge(Node a, Double d) {
		this.target = a;
		this.strength = d;

	}
}

class GraphLookup {
	Map<String, Node> map = new HashMap<String, Node>();
}