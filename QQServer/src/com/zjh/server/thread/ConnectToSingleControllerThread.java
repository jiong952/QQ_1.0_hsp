package com.zjh.server.thread;

import com.zjh.common.*;
import com.zjh.server.manage.ManageServerConnectClientThread;
import com.zjh.server.service.FriendService;
import com.zjh.server.service.MangeOffMsgService;
import com.zjh.server.service.MessageService;
import com.zjh.server.service.UserService;
import com.zjh.server.utils.FileUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这是服务器与某个用户的通讯线程
 * 在这里处理的用户请求，相当于controller控制层
 * 例如登录请求，返回好友列表，注册，删除好友，等
 * @author 张俊鸿
 * @date 2022/05/08
 **/
public class ConnectToSingleControllerThread {
    private FriendService friendService = new FriendService();
    private UserService userService = new UserService();
    private MessageService messageService = new MessageService();
    private ServerSocket serverSocket = null;
    //创建一个集合模拟用户数据库 ,key是userId,value是user
    //使用ConcurrentHashMap可以处理并发问题，线程安全
    private static ConcurrentHashMap<String,User> userHashMap = new ConcurrentHashMap<>();
    static {
        //静态代码块 初始化 validUsers
        userHashMap.put("admin",new User("admin","a"));
        userHashMap.put("张俊鸿",new User("张俊鸿","a"));
        userHashMap.put("com.zjh.server.utils.zjh",new User("com.zjh.server.utils.zjh","a"));
        userHashMap.put("a",new User("a","a"));
    }


    /**
     * 返回所有用户
     * @return {@link List}<{@link String}>
     */
    public static List<String> getAllUser(){
        // TODO: 2022-05-12 修改为真实数据库的数据
        List<String> users = new ArrayList<>();
        Iterator<String> iterator = userHashMap.keySet().iterator();
        while (iterator.hasNext()){
            users.add(iterator.next());
        }
        return users;
    }



    /**
     * 服务器通讯监听
     */
    public ConnectToSingleControllerThread() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //服务端在9999端口监听
        try {
            System.out.println("服务器端启动.....");
            System.out.println("服务端在"+StaticString.server_port+"端口监听");
            serverSocket = new ServerSocket(StaticString.server_port);
            //启动推送服务
            new SendNewsThread().start();
            //持续监听多用户
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("用户端"+socket);
                //获取客户端发来的user
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //接收请求
                RequestMsg requestMsg = (RequestMsg) ois.readObject();
                //输出流
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                switch (requestMsg.getContent()){
                    case "checkUser":
                        //登录验证
                        User user = (User) requestMsg.getParams()[0];
                        User flag = userService.checkUser(user.getUserId(), user.getPassword());
                        if(flag != null){
                            //登录成功
                            //检查是否已登录
                            if(ManageServerConnectClientThread.getThread(user.getUserId()) == null){
                                String time = sdf.format(new Date());
                                //响应成功
                                ResponseMsg responseMsg = new ResponseMsg();
                                responseMsg.setStateCode(StateCode.SUCCEED);
                                //同时返回从数据库中读出来的用户信息
                                responseMsg.setReturnValue(flag);
                                oos.writeObject(responseMsg);
                                //创建一个线程与登录客户端保持通信
                                ServerThread serverThread = new ServerThread(user.getUserId(), socket);
                                //开启线程
                                serverThread.start();
                                //把登录线程放进集合统一管理
                                ManageServerConnectClientThread.addThread(user.getUserId(), serverThread);
                                //增加一个notifyFriend方法，通知其他好友上线状态【发一个msg到监听线程，getterId是其他用户，senderId是上线用户】
                                //client收到消息后，在调用方法，更新好友列表字段
                                friendService.notifyOther(user.getUserId(),MessageType.NEW_ONLINE);
                                //查看是否有离线消息，有就发送给他
                                List<Message> offLineMsg = messageService.getOffLineMsg(user.getUserId());
                                if(offLineMsg != null){
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    //遍历消息发送
                                    for (Message msg : offLineMsg) {
                                        System.out.println("离线消息发送"+msg);
                                        ObjectOutputStream os = new ObjectOutputStream(ManageServerConnectClientThread.getThread(user.getUserId()).getSocket().getOutputStream());
                                        os.writeObject(msg);
                                    }
                                }
                                System.out.println("【"+time+"】"+user.getUserId()+ "登录成功" );
                                System.out.println("当前在线人数："+ManageServerConnectClientThread.onLineNum() + "人");
                            }else {
                                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String time = sdf.format(new Date());
                                //登录过了
                                System.out.println("【"+time+"】"+user.getUserId()+ "已登录过");
                                //响应已登录
                                ResponseMsg responseMsg = new ResponseMsg();
                                responseMsg.setStateCode(StateCode.HAS_LOGIN);
                                oos.writeObject(responseMsg);
                            }
                        }else {
                            //登录失败
                            //账号名或密码错误
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time = sdf.format(new Date());
                            System.out.println("【"+time+"】"+user.getUserId()+ "登录失败" );
                            //响应失败
                            ResponseMsg responseMsg = new ResponseMsg();
                            responseMsg.setStateCode(StateCode.FAIL);
                            oos.writeObject(responseMsg);
                            socket.close();
                        }
                        break;
                    case "findAllFriend":
                        //请求好友列表
                        String time = sdf.format(new Date());
                        System.out.println("【"+time+"】用户"+requestMsg.getRequesterId() + "请求好友列表");
                        //调用方法,将列表返回
                        List<Friend> list = friendService.findAllFriend((String) requestMsg.getParams()[0]);
                        //响应
                        ResponseMsg responseMsg = new ResponseMsg();
                        responseMsg.setReturnValue(list);
                        oos.writeObject(responseMsg);
                        break;
                    case "searchUserById":
                        //根据id模糊查询搜索用户
                        String time2 = sdf.format(new Date());
                        System.out.println("【"+time2+"】用户"+requestMsg.getRequesterId() + "搜索用户");
                        //到db搜索
                        List<User> list1 = userService.searchUserById((String) requestMsg.getParams()[0]);
                        //响应
                        ResponseMsg responseMsg2 = new ResponseMsg();
                        responseMsg2.setReturnValue(list1);
                        oos.writeObject(responseMsg2);
                        break;
                    case "checkFriend":
                        //添加好友前查看是否已经是好友
                        String myId0 = requestMsg.getRequesterId();
                        String friendId0 = (String)requestMsg.getParams()[0];
                        String time0 = sdf.format(new Date());
                        System.out.println("【"+time0+"】用户"+myId0+"查看和"+friendId0+"是否是好友");
                        //到db检索
                        boolean b = friendService.checkFriend(myId0, friendId0);
                        //响应回去
                        ResponseMsg responseMsg3 = new ResponseMsg();
                        responseMsg3.setReturnValue(b);
                        oos.writeObject(responseMsg3);
                        break;
                    case "askMakeFriend":
                        //用户发送好友申请
                        String myId = requestMsg.getRequesterId();
                        String friendId = (String)requestMsg.getParams()[0];
                        Date date1 = new Date();
                        String time3 = sdf.format(date1);
                        System.out.println("【"+time3+"】用户"+myId+"向"+friendId+"发送好友申请");
                        //转发好友申请消息
                        Message msg = new Message();
                        msg.setMsgType(MessageType.ASK_MAKE_FRIEND);
                        msg.setSenderId(myId);
                        msg.setGetterId(friendId);
                        msg.setContent("好友申请消息");
                        msg.setSendTime(date1);
                        //判断是否在线
                        ServerThread thread = ManageServerConnectClientThread.getThread(friendId);
                        if(thread != null){
                            //在线
                            ObjectOutputStream os = new ObjectOutputStream(ManageServerConnectClientThread.getThread(friendId).getSocket().getOutputStream());
                            os.writeObject(msg);
                        }else {
                            //离线
                            messageService.insertMsg(msg,false);
                        }
                        break;
                    case "searchUser":
                        String searchId = requestMsg.getRequesterId();
                        User user1 = userService.searchUser(searchId);
                        ResponseMsg responseMsg50 = new ResponseMsg();
                        responseMsg50.setReturnValue(user1);
                        oos.writeObject(responseMsg50);
                        break;
                    case "permitMakeFriend":
                        //用户同意好友请求
                        String myId2 = requestMsg.getRequesterId();
                        String askerId = (String)requestMsg.getParams()[0];
                        Date date = new Date();
                        String time4 = sdf.format(date);
                        System.out.println("【"+time4+"】用户"+myId2+"同意"+askerId+"的好友请求");
                        friendService.addFriend(myId2,askerId,date);
                        //通知申请人成功
                        Message askSuccessMsg = new Message();
                        askSuccessMsg.setMsgType(MessageType.SUCCESS_MAKE_FRIEND_TO_ASK);
                        askSuccessMsg.setGetterId(askerId);
                        askSuccessMsg.setSenderId(myId2);
                        ObjectOutputStream os1 = new ObjectOutputStream(ManageServerConnectClientThread.getThread(askerId).getSocket().getOutputStream());
                        os1.writeObject(askSuccessMsg);
                        //通知同意者成功
                        Message permitSuccessMsg = new Message();
                        permitSuccessMsg.setMsgType(MessageType.SUCCESS_MAKE_FRIEND_TO_PERMIT);
                        permitSuccessMsg.setGetterId(myId2);
                        permitSuccessMsg.setSenderId(askerId);
                        ObjectOutputStream os2 = new ObjectOutputStream(ManageServerConnectClientThread.getThread(myId2).getSocket().getOutputStream());
                        os2.writeObject(permitSuccessMsg);
                        break;
                    case "deleteFriend":
                        //用户单方面删除好友
                        String myId5 = requestMsg.getRequesterId();
                        String friendId5 = (String)requestMsg.getParams()[0];
                        String time5 = sdf.format(new Date());
                        System.out.println("【"+time5+"】用户"+myId5+"删除好友"+friendId5);
                        //调用方法
                        boolean b1 = friendService.deleteFriend(myId5, friendId5);
                        //响应
                        ResponseMsg responseMsg5 = new ResponseMsg();
                        responseMsg5.setReturnValue(b1);
                        oos.writeObject(responseMsg5);
                        break;
                    case "updateFriend":
                        //更新对好友的备注
                        String myId6 = requestMsg.getRequesterId();
                        String friendId6 = (String)requestMsg.getParams()[0];
                        String remark = (String)requestMsg.getParams()[1];
                        Boolean star = (Boolean)requestMsg.getParams()[2];
                        String time6 = sdf.format(new Date());
                        System.out.println("【"+time6+"】用户"+myId6+"更新好友"+friendId6);
                        //调用方法
                        boolean b2 = friendService.updateFriend(myId6, friendId6, remark, star);
                        //响应
                        ResponseMsg responseMsg6 = new ResponseMsg();
                        responseMsg6.setReturnValue(b2);
                        oos.writeObject(responseMsg6);
                        break;
                    case "getAllMsg":
                        //查找聊天记录
                        String myId7 = requestMsg.getRequesterId();
                        String friendId7 = (String)requestMsg.getParams()[0];
                        String time7 = sdf.format(new Date());
                        System.out.println("【"+time7+"】用户"+myId7+"查找和"+friendId7+"的聊天记录");
                        //调用方法
                        List<Message> allMsg = messageService.getAllMsg(myId7, friendId7);
                        //响应
                        ResponseMsg responseMsg7 = new ResponseMsg();
                        responseMsg7.setReturnValue(allMsg);
                        System.out.println(allMsg);
                        oos.writeObject(responseMsg7);
                        break;
                    case "updateDel":
                        //查找聊天记录
                        String myId8 = requestMsg.getRequesterId();
                        String friendId8 = (String)requestMsg.getParams()[0];
                        String time8 = sdf.format(new Date());
                        System.out.println("【"+time8+"】用户"+myId8+"删除和"+friendId8+"的聊天记录");
                        //调用方法
                        boolean b3 = messageService.updateDel(myId8, friendId8);
                        //响应
                        ResponseMsg responseMsg8 = new ResponseMsg();
                        responseMsg8.setReturnValue(b3);
                        oos.writeObject(responseMsg8);
                        break;
                    default:
                        //响应
                        ResponseMsg default_msg = new ResponseMsg();
                        default_msg.setStateCode(StateCode.NOT_FOUND);
                        oos.writeObject(default_msg);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
