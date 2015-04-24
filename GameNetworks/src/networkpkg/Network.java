package networkpkg;
import java.util.Iterator;

import static networkpkg.AdjMatrixGraph.Status;

public class Network {
	
	private Status[] tempStatuses;

	/**Run one 'round' of the network algorithm on the given graph,
	 * modifying in place.
	 */
	public void simulateRound(AdjMatrixGraph G) {
		int numVertices = G.V();
		tempStatuses = new Status[numVertices];
		
		for (int v = 0; v < numVertices; v++) {
			tempStatuses[v] = calculateVertexStatus(G, v);
		}
		
		for (int v = 0; v < numVertices; v++) {
			G.vertexStatuses[v] = tempStatuses[v];
		}
		
		printNodes(G);
		printLinks(G);
		
	}
	
	
	public void printNodes(AdjMatrixGraph G) {
		System.out.println("{\"nodes\":[");
		
		String s;
		
		int group = 0;
		
		for (int i = 0; i < G.V(); i++) {
			switch(G.vertexStatuses[i]) {
				case NONPLAYER:
					group = 0;
					break;
				case LEAGUE:
					group = 1;
					break;
				case DOTA:
					group = 2;
					break;
				case BOTH:
					group = 3;
					break;
				default:
					break;
			}
			s = "\t{\"name\":\""+i+"\", \"group\":" + group + "}";
			if (i < G.V() - 1) {
				s += ",";
			}
			System.out.println(s);
		}
		
		System.out.println("],\n\n");
	}
	
	public void printLinks(AdjMatrixGraph G) {
		
		System.out.println("\"links\":[");
		
		String s = "";
		
		for (int i = 0; i < G.V(); i++) {
			for (int j = i; j < G.V(); j++) {
				if (G.adj[i][j]) {
					s += "\t{\"source\":\""+i+",\"target\":"+j+",\"value\":1},\n";
				}
			}
			
		}
		
		System.out.println(s.substring(0, s.length()-2));
		System.out.println("]}\n\n");
	}
	
	
	/**Modify tempStatuses to contain the new status (NONPLAYER, LEAGUE, DOTA, BOTH)
	 * of the given vertex based on the status of its neighbors
	 */
	private Status calculateVertexStatus(AdjMatrixGraph G, int vertex) {
		Status currentStatus = G.vertexStatuses[vertex];
		Status newStatus = G.vertexStatuses[vertex];
		float numLeague = 0;
		float numDotA = 0;
		float fracDotA = 0;
		float fracLeague = 0;
		float totalNeighbors = 0;
		float percentDotA = 0;
		float percentLeague = 0;
		boolean caughtDotA = false;
		boolean caughtLeague = false;
		
		//Count neighbor statuses
		Iterator<Integer> itr = G.adj(vertex).iterator();
		while(itr.hasNext()) {
			int neighbor = itr.next();
			Status neighborStatus = G.vertexStatuses[neighbor];
			totalNeighbors++;
			
			switch(neighborStatus) {
				case NONPLAYER:
					break;
				case LEAGUE:
					numLeague++;
					break;
				case DOTA:
					numDotA++;
					break;
				case BOTH:
					numDotA++;
					numLeague++;
					break;
				default:
					break;
			}
		}
		
		if (totalNeighbors == 0) {
			return currentStatus;
		}
		fracDotA = numDotA/totalNeighbors;
		fracLeague = numLeague/totalNeighbors;
		
		
		//Calculate new status based on neighbors
		switch(currentStatus) {
			case NONPLAYER:
				caughtDotA = fracDotA > Constants.NONPLAYER_TO_DOTA_THRESHOLD;
				caughtLeague = fracLeague > Constants.NONPLAYER_TO_LEAGUE_THRESHOLD;
				
				if (caughtDotA && caughtLeague) {
					if (fracDotA == fracLeague) {
						newStatus = (Math.random() > 0.5? Status.DOTA : Status.LEAGUE);
					} else {
						newStatus = (fracDotA > fracLeague ? Status.DOTA : Status.LEAGUE);
					}
					break;
				} else if (caughtDotA) {
					newStatus = Status.DOTA;
					break;
				} else if (caughtLeague) {
					newStatus = Status.LEAGUE;
					break;
				}
				
				//Didn't adopt either game (didn't meet thresholds)
				//Check whether to adopt based on 'infection'
				percentDotA = 1f - (float)Math.pow(Constants.DOTA_INFLUENCE, numDotA);
				percentLeague = 1f - (float)Math.pow(Constants.LEAGUE_INFLUENCE, numLeague);
				
				caughtDotA = Math.random() < percentDotA;
				caughtLeague = Math.random() < percentLeague;

				if (caughtDotA && caughtLeague) {
					if (percentDotA == percentLeague) {
						newStatus = (Math.random() > 0.5? Status.DOTA : Status.LEAGUE);
					} else {
						newStatus = (percentDotA > percentLeague ? Status.DOTA : Status.LEAGUE);
					}
				} else if (caughtDotA) {
					newStatus = Status.DOTA;
				} else if (caughtLeague) {
					newStatus = Status.LEAGUE;
				}
				break;
			case LEAGUE:
				if (Constants.LEAGUE_BASE_VALUE + numLeague*Constants.LEAGUE_FRIEND_VALUE <
						-Constants.DOTA_BASE_VALUE + numDotA*Constants.DOTA_FRIEND_VALUE)
				{
					newStatus = Status.DOTA;
				}
				break;
			case DOTA:
				if (Constants.DOTA_BASE_VALUE + numDotA*Constants.DOTA_FRIEND_VALUE <
						-Constants.LEAGUE_BASE_VALUE + numLeague*Constants.LEAGUE_FRIEND_VALUE)
				{
					newStatus = Status.LEAGUE;
				}
				break;
			case BOTH:
				break;
			default:
				break;
		}
		
		return newStatus;
	}
	
    //Test client - adjust parameters in Constants class
    public static void main(String[] args) {

        AdjMatrixGraph G = new AdjMatrixGraph(Constants.NUM_VERTICES, Constants.NUM_EDGES);
        G.vertexStatuses[0] = Status.LEAGUE;
        G.vertexStatuses[1] = Status.DOTA;
        G.vertexStatuses[2] = Status.LEAGUE;
        
        Network network = new Network();
        
        for (int i = 0; i < Constants.NUM_ROUNDS; i++) {
        	network.simulateRound(G);
        }
        
//        String vertices = "";
//        for (Status s : G.vertexStatuses) {
//        	vertices += " | " + s;
//        }
//        System.out.println(vertices + " | ");
    }

}
