package com.khuongnd.dexkids.game;

/** Build-time alpha>=128 crop geometry; no pixel scan or JNI calls during GL startup. */
public final class PassengerPoseBounds {
    private PassengerPoseBounds() {}
    public static int[][] parse(String text,String child,int width,int height) {
        if (text==null || text.length()>4096 || width<=0 || height<=0 || width%2!=0 || height%2!=0)
            throw new IllegalArgumentException("Passenger bounds input");
        int[][] result=new int[4][];
        for(String line:text.split("\n")) {
            if(line.isBlank())continue;
            String[] parts=line.split("\t");
            if(parts.length!=8)throw new IllegalArgumentException("Passenger bounds row");
            if(!parts[0].equals(child))continue;
            int pose=Integer.parseInt(parts[1]);
            if(pose<0 || pose>=4 || result[pose]!=null || Integer.parseInt(parts[2])!=width || Integer.parseInt(parts[3])!=height)
                throw new IllegalArgumentException("Passenger bounds identity");
            int x=Integer.parseInt(parts[4]), y=Integer.parseInt(parts[5]);
            int w=Integer.parseInt(parts[6]), h=Integer.parseInt(parts[7]);
            if(x<0 || y<0 || w<=0 || h<=0 || x>width/2-w || y>height/2-h)
                throw new IllegalArgumentException("Passenger bounds rectangle");
            result[pose]=new int[]{x,y,w,h};
        }
        for(int[] bounds:result)if(bounds==null)throw new IllegalArgumentException("Missing passenger bounds pose");
        return result;
    }
}
