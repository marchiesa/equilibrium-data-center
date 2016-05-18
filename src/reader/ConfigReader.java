package reader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

	private Double updateInterval;
	private Double bandwidthIncrease;
	private String networkFile;
	private String flowsFile;
	private int networkParity;
	private double finishTime;
	private double initialFlowRate;
	private double linkBandwidth;
	private String outputFile;
	private String schedulerType;
	private Double simulatedAnnealingFrequency;

	public void readConfig() throws IOException{
		this.readConfig("simulator-parameters.cfg");
	}

	public boolean readConfig(String file) throws IOException{
		InputStream inputStream=null ;
		try{
			Properties prop = new Properties();

			inputStream = getClass().getClassLoader().getResourceAsStream(file);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + file + "' not found in the classpath");
			}

			// get the property value and print it out
			this.updateInterval = Double.parseDouble(prop.getProperty("update-interval"));
			this.bandwidthIncrease = Double.parseDouble(prop.getProperty("bandwidth-increase"));
			this.networkFile = prop.getProperty("network-file");
			this.networkParity = Integer.parseInt(prop.getProperty("network-parity"));
			this.flowsFile = prop.getProperty("flows-file");
			this.finishTime = Double.parseDouble(prop.getProperty("finish-time"));
			this.initialFlowRate = Double.parseDouble(prop.getProperty("initial-flow-rate"));
			this.linkBandwidth= Double.parseDouble(prop.getProperty("link-bandwidth"));
			this.outputFile = prop.getProperty("output-file");
			this.schedulerType = prop.getProperty("scheduler");
			this.simulatedAnnealingFrequency= Double.parseDouble(prop.getProperty("simulated-annealing-frequency"));
			this.networkFile = this.networkFile.replace("PARITY", ""+this.networkParity);
			this.flowsFile = this.flowsFile.replace("PARITY", ""+this.networkParity);
			String p = flowsFile.replace(".txt", "");
			p=p.replaceAll("/sam", "_sam");
			//System.out.println(p);
			p= p.substring(p.lastIndexOf("/")+1);
			//System.out.println(p);
			this.outputFile = "results/results-"+schedulerType+"-"+networkParity+"-"+p;
			//System.out.println(this.outputFile);
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			return false;
		} finally {
			inputStream.close();
		}
		//Integer a =null;a.intValue();
		return true;
	}

	public Double getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(Double updateInterval) {
		this.updateInterval = updateInterval;
	}

	public Double getBandwidthIncrease() {
		return bandwidthIncrease;
	}

	public void setBandwidthIncrease(Double bandwidthIncrease) {
		this.bandwidthIncrease = bandwidthIncrease;
	}

	public String getNetworkFile() {
		return networkFile;
	}

	public void setNetworkFile(String networkFile) {
		this.networkFile = networkFile;
	}

	public int getNetworkParity() {
		return networkParity;
	}

	public void setNetworkParity(int networkParity) {
		this.networkParity = networkParity;
	}

	public String getFlowsFile() {
		return flowsFile;
	}

	public void setFlowsFile(String flowsFile) {
		this.flowsFile = flowsFile;
	}

	public double getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(double finishTime) {
		this.finishTime = finishTime;
	}
	public double getInitialFlowRate() {
		return initialFlowRate;
	}
	public void setInitialFlowRate(double initialFlowRate) {
		this.initialFlowRate = initialFlowRate;
	}
	public double getLinkBandwidth() {
		return linkBandwidth;
	}
	public void setLinkBandwidth(double linkBandwidth) {
		this.linkBandwidth = linkBandwidth;
	}
	public String getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	public String getSchedulerType() {
		return schedulerType;
	}
	public void setSchedulerType(String schedulerType) {
		this.schedulerType = schedulerType;
	}
	public Double getSimulatedAnnealingFrequency() {
		return simulatedAnnealingFrequency;
	}
	public void setSimulatedAnnealingFrequency(Double simulatedAnnealingFrequency) {
		this.simulatedAnnealingFrequency = simulatedAnnealingFrequency;
	}
	
}
