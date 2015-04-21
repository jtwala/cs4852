package networkpkg;
import java.util.Arrays;
import java.util.Iterator;

import static networkpkg.AdjMatrixGraph.Status;

public class Network {
	
	public final float DOTA_THRESHOLD = 0.6f;
	public final float LEAGUE_THRESHOLD = 0.4f;
	
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
		
	}
	
	/**Modify tempStatuses to contain the new status (NONPLAYER, LEAGUE, DOTA, BOTH)
	 * of the given vertex based on the status of its neighbors
	 */
	private Status calculateVertexStatus(AdjMatrixGraph G, int vertex) {
		Status currentStatus = G.vertexStatuses[vertex];
		Status newStatus = G.vertexStatuses[vertex];
		float numLeague = 0;
		float numDotA = 0;
		float numBoth = 0;
		float totalNeighbors = 0;
		
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
					numBoth++;
					break;
				default:
					break;
			}
		}
		
		//TODO: Calculate new status based on neighbors
		
		return newStatus;
	}
	
    //Test client
    public static void main(String[] args) {
        int V = 4;
        int E = 7;
        AdjMatrixGraph G = new AdjMatrixGraph(V, E);
        System.out.println(G);
    }

}
