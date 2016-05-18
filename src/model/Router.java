package model;

import java.util.LinkedList;
import java.util.List;

public class Router implements Comparable<Router>{
	
	private int id;
	private int layer;
	private int pod;
	private List<Link> links;
	private List<Link> upperLinks;
	private List<Link> bottomLinks;
	
	public Router(){
		this.links = new LinkedList<Link>();
		this.upperLinks = new LinkedList<Link>();
		this.bottomLinks = new LinkedList<Link>();
	}
	public int getPod() {
		return pod;
	}
	public void setPod(int pod) {
		this.pod = pod;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	public List<Link> getUpperLinks() {
		return upperLinks;
	}
	public void setUpperLinks(List<Link> upperLinks) {
		this.upperLinks = upperLinks;
	}
	public List<Link> getBottomLinks() {
		return bottomLinks;
	}
	public void setBottomLinks(List<Link> bottomLinks) {
		this.bottomLinks = bottomLinks;
	}
	public int getLayer() {
		return layer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}
	public void addLink(Link link){
		this.links.add(link);
		if(link.isUpward())
			this.upperLinks.add(link);
		else
			this.bottomLinks.add(link);
	}
	public Link getIndexLink(int index){
		return this.links.get(index);
	}
	public Link getIndexUpperLink(int index){
		return this.upperLinks.get(index);
	}
	public Link getIndexBottomLink(int index){
		return this.bottomLinks.get(index);
	}
	
	public String toString(){
		return "R("+this.id+",l="+this.layer+")";
	}
	
	@Override
	public int compareTo(Router o) {
		return o.getId() - this.getId();
	}
	
	public int hashcode(){
		return this.getId();
	}
	
	public boolean equals(Object o){
		Router r = (Router)o;
		return r.getId() == this.getId();
	}
	
}
