package com.car.carsquad.carapp;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chatroom {
    private String name, msgTime, lastMsg, rideName, startPt, endPt, driverID;
    private Object profileImg;

    public String getStartPt(){ return startPt; }
    public void setStartPt(String startPt){ this.startPt = startPt; }

    public String getEndPt(){ return endPt; }
    public void setEndPt(String endPt){ this.endPt = endPt; }

    public String getDriverID(){ return driverID; }
    public void setDriverID(String driverID){ this.driverID = driverID; }

    public String getRideName() {return  rideName; }
    public void setRideName(String rideName) { this.rideName = rideName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMsgTime() { return msgTime; }
    public void setMsgTime(String msgTime) { this.msgTime = msgTime; }

    public String getLastMsg() { return lastMsg; }
    public void setLastMsg(String lastMsg) { this.lastMsg = lastMsg; }


    public Object getProfileImg() { return profileImg; }
    public void setProfileImg(Object profileImg) {
        this.profileImg = profileImg; }


}
