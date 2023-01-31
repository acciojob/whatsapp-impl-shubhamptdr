package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");

        }
        User user= new User(name,mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        int noOfMember = users.size();
        String groupName = "";

        if(noOfMember == 2){
            groupName = users.get(1).getName();
        }
        else {
            this.customGroupCount++;
            groupName = "Group "+this.customGroupCount;
        }

        User admin = users.get(0);
        Group group = new Group(groupName,noOfMember);
        adminMap.put(group,admin);
        groupUserMap.put(group,users);
        groupMessageMap.put(group,new ArrayList<Message>());
        return group;
    }

    public int createMessage(String content) {
        this.messageId++;
        Message message = new Message(this.messageId,content);
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }

        List<User> users = groupUserMap.get(group);
        if(!users.contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        if (groupMessageMap.containsKey(group)) {
            groupMessageMap.get(group).add(message);
        }

        senderMap.put(message,sender);

        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is
        // only one admin and the admin rights are transferred from approver to user.
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }

        if (!Objects.equals(adminMap.get(group),approver)){
            throw new Exception("Approver does not have rights");
        }

        boolean isFound = false;
        List<User> users = groupUserMap.get(group);
        for(User user1 : users){
            if(Objects.equals(user1,user)){
                isFound = true;
                break;
            }
        }
        if(!isFound){
            throw new Exception("User is not a participant");
        }
        adminMap.put(group,user);

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contains any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from
        // all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number
        // of messages in group + the updated number of overall messages)
        Group group2 = null;
        boolean isFound = false;
        for(Group group1 : groupUserMap.keySet()){
            for (User user1 : groupUserMap.get(group1)){
                if(Objects.equals(user1,user)){
                    group2 = group1;
                    isFound = true;
                    break;
                }
            }
        }
        if(!isFound){
            throw new Exception("User not found");
        }
        if(Objects.equals(adminMap.get(group2),user)){
            throw new Exception("Cannot remove admin");
        }
        List<User> users = groupUserMap.get(group2);
        for (User user2 : users){
            if(Objects.equals(user2,user)){
                users.remove(user2);
                break;
            }
        }
        groupUserMap.put(group2,users);
        for ( Message message : senderMap.keySet()){
            if(Objects.equals(senderMap.get(message),user)){
                senderMap.remove(message);
                List<Message> msgs = groupMessageMap.get(group2);
                for (Message msg : msgs){
                    if(Objects.equals(msg,message)){
                        msgs.remove(message);
                    }
                }
                groupMessageMap.put(group2,msgs);
            }
        }

        return groupMessageMap.get(group2).size() + groupUserMap.get(group2).size() + senderMap.size();
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messages = new ArrayList<>();
        for(Group group: groupMessageMap.keySet()){
            messages.addAll(groupMessageMap.get(group));
        }
        List<Message> filteredMessages = new ArrayList<>();
        for(Message message: messages){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                filteredMessages.add(message);
            }
        }
        if(filteredMessages.size() < k){
            throw new Exception("K is greater than the number of messages");
        }
        Collections.sort(filteredMessages, new Comparator<Message>(){
            public int compare(Message m1, Message m2){
                return m2.getTimestamp().compareTo(m1.getTimestamp());
            }
        });
        return filteredMessages.get(k-1).getContent();
    }
}
