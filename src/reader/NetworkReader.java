package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import model.Link;
import model.Network;
import model.Router;
import scheduler.Routing;

public class NetworkReader {

	private ConfigReader config;
	private Routing routing;

	public NetworkReader(ConfigReader config, Routing routing) {
		this.config = config;
		this.routing = routing;
	}

	public Network readNetwork() throws IOException{
		String file = config.getNetworkFile();
		Integer upperDegree = config.getNetworkParity()/2;
		int numberOfCoreSwitches = upperDegree*upperDegree; 
		int podSize=numberOfCoreSwitches+upperDegree*2;
		double linkBandwidth = config.getLinkBandwidth()/8;
		Network network = new Network(); 

		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			Integer numberOfNodes = Integer.parseInt(br.readLine());
			for(int i=0;i<numberOfNodes;i++){
				Router r = new Router();
				r.setId(i);
				if(i<numberOfCoreSwitches){
					r.setLayer(3);
					r.setPod(-1);
				}else{
					int pod = (i-numberOfCoreSwitches)/(numberOfCoreSwitches+upperDegree*2);

					if((i-numberOfCoreSwitches)%(numberOfCoreSwitches+upperDegree*2)<upperDegree){
						r.setLayer(2);
						r.setPod(pod);
					}
					else if((i-numberOfCoreSwitches)%(numberOfCoreSwitches+upperDegree*2)<upperDegree*2){
						r.setLayer(1);
						r.setPod(pod);
					}
					else{
						r.setLayer(0);
						r.setPod(pod);
					}

				}

				/*else if(i<numberOfCoreSwitches+upperDegree){
		    			r.setLayer(2);
		    			r.setPod(0);
		    		}
		    		else if(i<numberOfCoreSwitches+upperDegree*2){
		    			r.setLayer(1);
		    			r.setPod(0);
		    		}
		    		else if(i<numberOfCoreSwitches*2+upperDegree*2){
		    			r.setLayer(0);
		    			r.setPod(0);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize+upperDegree){
		    			r.setLayer(2);
		    			r.setPod(1);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize+upperDegree*2){
		    			r.setLayer(1);
		    			r.setPod(1);
		    		}
		    		else if(i<numberOfCoreSwitches*2+podSize+upperDegree*2){
		    			r.setLayer(0);
		    			r.setPod(1);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*2+upperDegree){
		    			r.setLayer(2);
		    			r.setPod(2);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*2+upperDegree*2){
		    			r.setLayer(1);
		    			r.setPod(2);
		    		}
		    		else if(i<numberOfCoreSwitches*2+podSize*2+upperDegree*2){
		    			r.setLayer(0);
		    			r.setPod(2);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*3+upperDegree){
		    			r.setLayer(2);
		    			r.setPod(3);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*3+upperDegree*2){
		    			r.setLayer(1);
		    			r.setPod(3);
		    		}
		    		else if(i<numberOfCoreSwitches*2+podSize*3+upperDegree*2){
		    			r.setLayer(0);
		    			r.setPod(3);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*4+upperDegree){
		    			r.setLayer(2);
		    			r.setPod(4);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*4+upperDegree*2){
		    			r.setLayer(1);
		    			r.setPod(4);
		    		}
		    		else if(i<numberOfCoreSwitches*2+podSize*4+upperDegree*2){
		    			r.setLayer(0);
		    			r.setPod(4);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*5+upperDegree){
		    			r.setLayer(2);
		    			r.setPod(5);
		    		}
		    		else if(i<numberOfCoreSwitches+podSize*5+upperDegree*2){
		    			r.setLayer(1);
		    			r.setPod(5);
		    		}
		    		else if(i<numberOfCoreSwitches*2+podSize*5+upperDegree*2){
		    			r.setLayer(0);
		    			r.setPod(5);
		    		}*/
				network.addRouter(r);
			}

			if(config.getSchedulerType().equals("non-blocking")){
				Router main = new Router();
				main.setId(0);
				main.setLayer(1);
				main.setPod(-1);
				network.addRouter(main);
				int i=0;
				for(Router router:network.getLayer2routers().get(0)){
					Link link1 = new Link();
					link1.setBandwidth(linkBandwidth);
					link1.setHeadRouter(network.getId2router().get(router.getId()));
					link1.setTailRouter(network.getId2router().get(main.getId()));
					link1.setId(2*i);

					Link link2 = new Link();
					link2.setBandwidth(linkBandwidth);
					link2.setHeadRouter(network.getId2router().get(main.getId()));
					link2.setTailRouter(network.getId2router().get(router.getId()));
					link2.setId(2*i+1);
					i++;
					
					int layer =1;
					link1.setLayer(layer);
					link2.setLayer(layer);
					if(link1.getHeadRouter().getLayer()>link1.getTailRouter().getLayer()){
						link1.setUpward(true);
						link2.setUpward(false);
					}else{
						link2.setUpward(true);
						link1.setUpward(false);
					}

					link1.getTailRouter().addLink(link1);
					link2.getTailRouter().addLink(link2);

					network.addLink(link1);
					network.addLink(link2);
				}
			}else{
				Integer numberOfLinks = Integer.parseInt(br.readLine());
				String line = "";
				for(int i=0;i<numberOfLinks;i++){
					line = br.readLine();
					//System.out.println("reading line: " + line);
					Integer idRouter1 = Integer.parseInt(line.split("\t" )[0]);
					Integer idRouter2 = Integer.parseInt(line.split("\t" )[1]);

					Link link1 = new Link();
					link1.setBandwidth(linkBandwidth);
					link1.setHeadRouter(network.getId2router().get(idRouter1));
					link1.setTailRouter(network.getId2router().get(idRouter2));
					link1.setId(2*i);

					Link link2 = new Link();
					link2.setBandwidth(linkBandwidth);
					link2.setHeadRouter(network.getId2router().get(idRouter2));
					link2.setTailRouter(network.getId2router().get(idRouter1));
					link2.setId(2*i+1);

					int layer = Math.max(link1.getHeadRouter().getLayer(), link2.getTailRouter().getLayer());
					link1.setLayer(layer);
					link2.setLayer(layer);
					if(link1.getHeadRouter().getLayer()>link1.getTailRouter().getLayer()){
						link1.setUpward(true);
						link2.setUpward(false);
					}else{
						link2.setUpward(true);
						link1.setUpward(false);
					}

					link1.getTailRouter().addLink(link1);
					link2.getTailRouter().addLink(link2);

					network.addLink(link1);
					network.addLink(link2);
				}
			}
		} finally {
			br.close();
		}
		return network;
	}

	public Routing getRouting() {
		return routing;
	}



}
