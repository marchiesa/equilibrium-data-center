package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import model.Flow;
import model.util.StartTimeComparator;

public class FlowReader {

	private ConfigReader config;
	private List<Flow> allFlowsSortedByStartTime;

	public FlowReader(ConfigReader config){
		this.config = config;
		this.allFlowsSortedByStartTime = new LinkedList<Flow>();
	}

	public void readFlows() throws NumberFormatException, IOException{
		String file = config.getFlowsFile();
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			Integer numberOfFlows = Integer.parseInt(br.readLine());

			String line="";
			for(int i=0;i<numberOfFlows;i++){
				line = br.readLine();
				Double startTime = Double.parseDouble(line.split(" ")[0]);
				Double size = Double.parseDouble(line.split(" ")[1]);
				Integer sourceId = Integer.parseInt(line.split(" ")[2]);
				Integer destinationId = Integer.parseInt(line.split(" ")[3]);
				
				Flow flow= new Flow();
				flow.setId(i);
				flow.setSize(size);
				flow.setCurrentRate(config.getInitialFlowRate());
				flow.setDestinationId(destinationId);
				flow.setSourceId(sourceId);
				flow.setStartTime(startTime);
				
				this.allFlowsSortedByStartTime.add(flow);
			}
		} finally {
			br.close();
		}
		
		Collections.sort(this.allFlowsSortedByStartTime,new StartTimeComparator());
	}

	public List<Flow> getAllFlowsSortedByStartTime() {
		return allFlowsSortedByStartTime;
	}

	public void setAllFlowsSortedByStartTime(List<Flow> allFlowsSortedByStartTime) {
		this.allFlowsSortedByStartTime = allFlowsSortedByStartTime;
	}

	

}
