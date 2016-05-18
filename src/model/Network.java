package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Network {

	private Map<Integer,Link> id2link;
	private Map<Integer,Router> id2router;
	private Map<Integer, List<Router>> layer2routers;
	private Map<Integer, List<Link>> layer2links;
	private Map<Integer,Set<Router>> podId2hosts;
	
	public Network(){
		this.id2link = new HashMap<Integer,Link>();
		this.id2router = new HashMap<Integer,Router>();
		this.podId2hosts = new HashMap<Integer,Set<Router>>();
		this.layer2routers = new HashMap<Integer, List<Router>>();
		this.layer2links = new HashMap<Integer, List<Link>>();
		for(int i=0;i<4;i++)
			this.layer2routers.put(i, new LinkedList<Router>());
		for(int i=1;i<4;i++)
			this.layer2links.put(i, new LinkedList<Link>());
	}
	
	public Map<Integer, Link> getId2link() {
		return id2link;
	}
	public void setId2link(Map<Integer, Link> id2link) {
		this.id2link = id2link;
	}
	public Map<Integer, Router> getId2router() {
		return id2router;
	}
	public void setId2router(Map<Integer, Router> id2router) {
		this.id2router = id2router;
	}
	public Map<Integer, List<Router>> getLayer2routers() {
		return layer2routers;
	}
	public void setLayer2routers(Map<Integer, List<Router>> layer2routers) {
		this.layer2routers = layer2routers;
	}
	public Map<Integer, List<Link>> getLayer2links() {
		return layer2links;
	}
	public void setLayer2links(Map<Integer, List<Link>> layer2links) {
		this.layer2links = layer2links;
	}
	
	public void addRouter(Router router){
		this.id2router.put(router.getId(), router);
		this.layer2routers.get(router.getLayer()).add(router);
		Set<Router> routers = this.podId2hosts.get(router.getPod());
		if(routers==null){
			routers= new TreeSet<Router>();
			this.podId2hosts.put(router.getPod(), routers);
		}
		routers.add(router);
	}
	
	public void addLink(Link link){
		this.id2link.put(link.getId(), link);
		this.layer2links.get(link.getLayer()).add(link);
	}
	
	public Link getReversedLink(Link link){
		if(link.getId() %2 ==0 )
			return this.getId2link().get(link.getId()+1);
		else
			return this.getId2link().get(link.getId()-1);
	}
	
	public List<Link> getReversedLinks(List<Link> links){
		List<Link> reversedList = new LinkedList<Link>();
		for(Link link : links)
			reversedList.add(this.getReversedLink(link));
		return reversedList;
	}

	public List<Link> getLinksFromIndicesForCoreRouter(List<Integer> linkIndices, Router router) {
		List<Link> links = new LinkedList<Link>();
		for(Integer index : linkIndices){
			Link link  = router.getBottomLinks().get(index);
			links.add(link);
			router= link.getHeadRouter();
		}
		return links;
	}
	
	public Map<Integer, Set<Router>> getPodId2hosts() {
		return podId2hosts;
	}

	public void setPodId2hosts(Map<Integer, Set<Router>> podId2hosts) {
		this.podId2hosts = podId2hosts;
	}

	public String toString(){
		String result="ROUTERS:\n";
		for(Router router: this.id2router.values())
			result+="  " +router+"\n";
		result+="LINKS:\n ";
		for(Link link: this.id2link.values())
			result+="  "+link+"\n";
		return result;
	}
}
