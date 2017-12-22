package com.khelm.dungeons;

import java.io.Serializable;

/**
 * class defines a block in a theme
 * 
 * @author IraqiMilitant
 *
 */
public class ThemeBlock implements Serializable{
	

	private static final long serialVersionUID = 90740398254774195L;
	private int id;//id of the block
	private boolean vein;//whether or not it can generate in veins
	private int weight;//how many entries in the block placement draw
	
	public ThemeBlock(int id,boolean vein,int weight){
		this.id=id;
		this.vein=vein;
		this.weight=weight;
	}
	

	
	public int getId(){
		return id;
	}
	
	public boolean getVein(){
		return vein;
	}
	
	public int getWeight(){
		return weight;
	}
	

}
