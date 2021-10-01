package com.jb.vmeeting.mvp.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jianbin on 2016/4/21.
 */
public class Room extends BaseEntity implements Serializable {

    @Expose
    private String id;

    @Expose
    @SerializedName("roomName")
    private String name;

    @Expose
    @SerializedName("ownerName")
    private String ownerName;

    @Expose
    private long startTime = System.currentTimeMillis();

    @Expose
    private long endTime = System.currentTimeMillis();

    @Expose
    private User owner;

    @Expose
    private List<User> participator; // 房间的参与者

    @Expose
    private String describe;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<User> getParticipator() {
        return (participator == null ? participator = new ArrayList<User>() : participator);
    }

    public void setParticipator(List<User> participator) {
        this.participator = participator;
    }
}
