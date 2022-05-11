package com.zjh.client.service;

import com.zjh.common.Friend;
import com.zjh.common.RequestMsg;
import com.zjh.common.ResponseMsg;
import com.zjh.common.StateCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

/**
 * @author 张俊鸿
 * @description: 好友管理业务逻辑类
 * @since 2022-05-10 22:56
 */
@SuppressWarnings("all")
public class FriendService {
    public static void main(String[] args) {
        List<Friend> a = new FriendService().findAllFriend("a");
        System.out.println(a);
    }
    private Socket socket;

    /**
     * 向服务器请求好友列表
     *
     * @param userId 用户id
     * @return {@link List}<{@link Friend}>
     */
    public List<Friend> findAllFriend(String userId){
        List<Friend> list = null;
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 9998);
            //发送序列化用户对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            RequestMsg requestMsg = new RequestMsg();
            //方法名和参数
            requestMsg.setRequesterId(userId);
            requestMsg.setContent("findAllFriend");
            requestMsg.setParams(new Object[]{userId});
            oos.writeObject(requestMsg);
            //接收服务端响应的消息
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ResponseMsg responseMsg = (ResponseMsg) ois.readObject();
            list = (List<Friend>) responseMsg.getReturnValue();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查看是不是朋友
     *
     * @param myId     我身份证
     * @param friendId 朋友id
     * @return boolean
     */
    public boolean checkFriend(String myId,String friendId){
        boolean flag = false;
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 9998);
            //发送序列化用户对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            RequestMsg requestMsg = new RequestMsg();
            //方法名和参数
            requestMsg.setRequesterId(myId);
            requestMsg.setContent("checkFriend");
            requestMsg.setParams(new Object[]{friendId});
            oos.writeObject(requestMsg);
            //接收服务端响应的消息
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ResponseMsg responseMsg = (ResponseMsg) ois.readObject();
            flag = (boolean)responseMsg.getReturnValue();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return flag;
    }
    /**
     * 发出好友申请
     *
     * @param myId     用户id
     * @param friendId 朋友id
     */
    public void askMakeFriend(String myId,String friendId){
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 9998);
            //发送序列化用户对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            RequestMsg requestMsg = new RequestMsg();
            //方法名和参数
            requestMsg.setRequesterId(myId);
            requestMsg.setContent("askMakeFriend");
            requestMsg.setParams(new Object[]{friendId});
            oos.writeObject(requestMsg);
            System.out.println("向"+friendId+"发送好友申请成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同意好友请求
     *
     * @param myId    用户id
     * @param askerId 请求者的id
     */
    public void permitMakeFriend(String myId,String askerId){
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 9998);
            //发送序列化用户对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            RequestMsg requestMsg = new RequestMsg();
            //方法名和参数
            requestMsg.setRequesterId(myId);
            requestMsg.setContent("permitMakeFriend");
            requestMsg.setParams(new Object[]{askerId});
            oos.writeObject(requestMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单方面删除朋友关系
     *
     * @param myId     用户id
     * @param friendId 朋友id
     * @return boolean
     */
    public boolean deleteFriend(String myId,String friendId){
        boolean flag = false;
        try {
            socket = new Socket(InetAddress.getByName("127.0.0.1"), 9998);
            //发送序列化用户对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            RequestMsg requestMsg = new RequestMsg();
            //方法名和参数
            requestMsg.setRequesterId(myId);
            requestMsg.setContent("deleteFriend");
            requestMsg.setParams(new Object[]{friendId});
            oos.writeObject(requestMsg);
            //接收服务端响应的消息
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ResponseMsg responseMsg = (ResponseMsg) ois.readObject();
            flag = (boolean)responseMsg.getReturnValue();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
