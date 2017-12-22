package com.iraqimilitant.dungeonslink;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Class used to track location data that is serializable as the Bukkit Location object is not serializable
 * due to it holding ref to the entire world
 * 
 * @author IraqiMilitant
 *
 */
public class SerializableLocation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2773292698489157048L;
	private double x,y,z;
	private float yaw=0;
	private float pitch=0;
    private String world;
    public SerializableLocation(Location loc) {
        x=loc.getX();
        y=loc.getY();
        z=loc.getZ();
        yaw=loc.getYaw();
        pitch=loc.getPitch();
        world=loc.getWorld().getName();
    }
    
    /**
     * gets the bukkit location equiv of the object
     * 
     * @return
     */
    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        if(w==null)
            return null;
        Location toRet = new Location(w,x,y,z);
        toRet.setPitch(pitch);
        toRet.setYaw(yaw);
        return toRet;
    }
    
    public int getX(){
    	return (int)x;
    }
    public int getY(){
    	return (int)y;
    }
    public int getZ(){
    	return (int)z;
    }
    public float getYaw(){
    	return yaw;
    }
    public float getPitch(){
    	return pitch;
    }
}
