//=======================================================================================
// This file is part of: ZigBee Localizer - LocationService
// A localization system for ZigBee networks, based on the analysis of RSSI.
// It estimates the positions of the mobile nodes. This Java program produces
// results in a accessible webservlet where the user can manage the WSN network.
//
// Author         : Alberto Realis-Luc <alberto.realisluc@gmail.com>
// Since          : May 2008
// Web            : http://www.alus.it/pubs/LocationService/
// Git Repository : https://github.com/alus-it/ZigBeeLocalizer.git
// Version        : 1.0
// Copyright      : © 2008-2009 Alberto Realis-Luc
// License        : GPL
// Last change    : September 2009
//=======================================================================================

package it.ismb.locationService.servlet;

import it.ismb.locationService.Converter;
import it.ismb.locationService.PacketDecoder;
import it.ismb.locationService.Room;
import it.ismb.locationService.Nodes.Node;
import it.ismb.locationService.Nodes.Sensor;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RefNode extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public void init( ServletConfig config ) throws ServletException{
		super.init( config );
	}
       
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
    	res.setContentType("text/html");
    	ServletOutputStream out = res.getOutputStream();
    	out.println(Html.header);
    	out.println(Html.defaultMenu);
    	out.println("&gt;<a href=\"RefNodes\">RefNodes</a>"+
    	"&gt;<a href=\"#\" class=\"current\">EditRefNode</a>"+
    	"</div><div id=\"content\">");
    	out.println("<h1>RefNode</h1>");
    	HttpSession sessione=req.getSession();
    	Node refNode=null;
    	int NTWaddr=1;
    	if(req.getParameterMap().containsKey("selected")) {
    		NTWaddr=Integer.valueOf(req.getParameter("selected")).intValue();
    		refNode=PacketDecoder.getRefNode(NTWaddr);
    		sessione.setAttribute("updatingNode",refNode);
    	} else {
    		refNode=(Node)sessione.getAttribute("updatingNode");
    		NTWaddr=refNode.getNTWaddr();
    	}
    	if(refNode!=null) {
    		sessione.setAttribute("updatingNode",refNode);
    		out.println("<form action=\"RefNode\" method=\"post\">" );
    		out.println("<table><tr><td>Name:</td><td><input name=\"name\" size=\"20\" value=\""+
    				refNode.getName()+"\" /></td><td rowspan=\"4\" valign=\"bottom\">"+
    				"<input type=\"submit\" value=\"Update RefNode\"></td></tr>");
    		out.println("<tr><td>IEEE address:</td><td>"+Converter.Vector2HexStringNoSpaces(refNode.getIEEEaddr())+"</td></tr>");
    		out.println("<tr><td>Short address:</td><td>"+refNode.getNTWaddr()+"</td></tr>");
    		out.println("<tr><td>Auto mode:</td><td><input name=\"AutoMode\" type=\"radio\" ");
    		if(!refNode.isAutoMode()) out.print("checked=\"checked\" ");
    		out.println("value=\"0\" />Polled - <input name=\"AutoMode\" type=\"radio\" ");
    		if(refNode.isAutoMode()) out.print("checked=\"checked\" ");
    		out.println("value=\"1\" />Automatic</td></tr>");
    		out.println("<tr><td>Auto TX Sens time (sec):</td><td><input name=\"cycle\" size=\"6\" value=\""
    				+refNode.getCycle()+"\" /></td></tr></table>");
    		out.println("<h2>Location Settings</h2><table>");
    		out.println("<tr><td>Room:</td><td><select name=\"room\" size=\"1\">");
    		int actualRoom=refNode.getRoomId();
    		Collection<Room> rooms=PacketDecoder.getAllRooms().values();
    		Iterator<Room> itr=rooms.iterator();
    		Room room;
    		while(itr.hasNext()) {
    			room=itr.next();
    			if(room.getId()!=actualRoom)
    				out.println("<option value=\""+room.getId()+"\">"+room.getName()+"</option>");
    			else out.println("<option selected=\"selected\" value=\""+room.getId()+"\">"+room.getName()+"</option>");
    		}
    		out.println("</select><td/><td rowspan=\"3\" valign=\"bottom\">"+
    				"<input type=\"submit\" value=\"Update RefNode\"></td></tr>");
    		out.println("<tr><td>X pos (m):</td><td><input name=\"xpos\" size=\"6\" value=\""+
    				refNode.getXpos()+"\" /></td></tr>");
    		out.println("<tr><td>Y pos (m):</td><td><input name=\"ypos\" size=\"6\" value=\""+
    				refNode.getYpos()+"\" /></td></tr></table></form>");
    		out.println("<h2>Sensors</h2><table>");
    		int numsens=refNode.getNumOfSens();
    		out.println("<tr><td>Sensors on node:</td><td>"+numsens+"</td><td><form action=\"RequestPollSens\" method=\"get\">"+
    				"<input type=\"submit\" value=\"Request sensors values now\"></form></td></tr>");
    		for(Sensor s:refNode.getSensors().values())
    			out.println("<tr><td>"+s.getSensorName()+":</td><td>"+String.format("%.2f",s.getValue())+" "+s.getSensorUnitHTML()+
    				"</td><td><form action=\"HistorySensor\" method=\"get\">"+
    				"<input type=\"hidden\" name=\"sensId\" value=\""+s.getId()+"\">"+
    				"<input type=\"submit\" value=\"Values History\"></form></td></tr>");
    		out.println("</table><h2>Diagnostic</h2><form action=\"RequestDiagnostic\" method=\"get\"><table>");
    		out.println("<tr><td>Battery voltage:</td><td>"+String.format("%.2f",refNode.getVbatt())+
			" V</td><td rowspan=\"3\" valign=\"bottom\"><input type=\"submit\" value=\"Request diagnostic now\"></td></tr>");
    		out.println("<tr><td>Parent RSSI:</td><td>"+refNode.getParentRSSI()+" dBm</td></tr>");
    		out.println("<tr><td>Received last packet at:</td><td>"+Converter.TimeMillisToString(refNode.getTimestamp())+"</td></tr></table>");
    		out.println("</table></form>");
    		out.println("<h2>Delete node</h2><form action=\"DeleteNode\" method=\"get\">"+
    			"<p>Ensure that the node is switched off before deleting.</p>"+
    			"<input type=\"submit\" value=\"Delete node\" "+
    		"onClick=\"return confirm('Are you sure? Please, switch off the node before confirm.');\" /></form><br/>");
    	} else out.println("<h3>Error: requested node not found!</h3>");
    	out.println(Html.footer);
    }
    
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
    	res.setContentType("text/html");
    	ServletOutputStream out = res.getOutputStream();
    	HttpSession sessione=req.getSession();
    	out.println(Html.header);
    	out.println(Html.defaultMenu);
    	out.println("&gt;<a href=\"RefNodes\">RefNodes</a>"+
    	"&gt;<a href=\"#\" class=\"current\">UpdateRefNode</a>"+
    	"</div><div id=\"content\">");
    	out.println("<h1>UpdateRefNode</h1>");
    	Node refNode=(Node)sessione.getAttribute("updatingNode");
    	String name=req.getParameter("name");
    	double x=Double.valueOf(req.getParameter("xpos"));
    	double y=Double.valueOf(req.getParameter("ypos"));
    	int roomID=Integer.valueOf(req.getParameter("room"));
    	boolean automode;
    	if(Integer.valueOf(req.getParameter("AutoMode")).intValue()==1) automode=true;
    	else automode=false;
    	double cycle=Double.valueOf(req.getParameter("cycle"));
    	if(x!=refNode.getXpos() || y!=refNode.getYpos() || roomID!=refNode.getRoomId() ||
    			automode!=refNode.isAutoMode() || cycle!=refNode.getCycle())
    		PacketDecoder.configureRefNode(refNode.getNTWaddr(),x,y,roomID,automode,cycle,true);
    	if(refNode.getName()==null) PacketDecoder.updateNodeName(refNode.getNTWaddr(),true,name);
    	else if(refNode.getName().compareTo(name)!=0)
    		PacketDecoder.updateNodeName(refNode.getNTWaddr(),true,name);
    	out.println("<h3>Update RefNode done!</h3>");
    	out.println("<br/><form action=\"RefNode\" method=\"get\">");
    	out.println("<input type=\"submit\" value=\"Back to node\"></form>"+Html.footer);
    }
}
