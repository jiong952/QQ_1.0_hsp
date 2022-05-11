package com.zjh.client.view;

import com.zjh.client.service.FileClientService;
import com.zjh.client.service.FriendService;
import com.zjh.client.service.MessageClientService;
import com.zjh.client.service.UserService;
import com.zjh.common.Friend;
import com.zjh.common.StateCode;
import com.zjh.utils.Utility;

import java.util.List;


/**
 * QQ界面
 *
 * @author 张俊鸿
 * @date 2022/05/08
 */
@SuppressWarnings("all")
public class QQView {
    private UserService userService = new UserService();
    private MessageClientService messageClientService = new MessageClientService();
    private FileClientService fileClientService = new FileClientService();
    private FriendService friendService = new FriendService();
    public static void main(String[] args) {
        new QQView().showMenu();
        System.out.println("客户端退出.......");
    }

    private boolean loop = true; //控制菜单是否显示
    private String key; //控制台指令

    /**
     * 显示菜单
     */
    //显示主菜单
    private void showMenu(){
        while (loop){
            System.out.println("======欢迎登录多用户通信系统======");
            System.out.println("\t\t 1 登录系统");
            System.out.println("\t\t 0 退出系统");
            System.out.println("请输入你的选择:");
            //输入一位指令,根据指令执行不同逻辑
            key = Utility.readString(1);
            switch (key){
                case "1":
                    System.out.print("请输入用户名：");
                    String userId = Utility.readString(20);
                    System.out.print("请输入密 码：");
                    String password = Utility.readString(20);
                    //登录验证
                    String stateCode = userService.checkUser(userId, password);
                    if(StateCode.SUCCEED.equals(stateCode)){
                        System.out.println("======欢迎用户("+userId+")======");
                        while (loop){
                            System.out.println("\n======多用户通信系统二级菜单======");
                            System.out.println("\t\t 1 显示所有在线用户列表");
                            System.out.println("\t\t 2 群发消息给所有好友");
                            System.out.println("\t\t 3 私发消息");
                            System.out.println("\t\t 4 发送文件");
                            System.out.println("\t\t 5 群聊功能");
                            System.out.println("\t\t 6 显示好友列表");
                            System.out.println("\t\t 7 添加好友|不用");
                            System.out.println("\t\t 8 删除好友");
                            System.out.println("\t\t 9 修改好友");
                            System.out.println("\t\t a 添加好友 | 搜索用户");
                            System.out.println("\t\t 0 退出系统");
                            System.out.println("请输入你的选择:");
                            //输入一位指令,根据指令执行不同逻辑
                            String command = Utility.readString(1);
                            switch (command){
                                case "1":
//                                    System.out.println("显示在线用户列表");
                                    userService.onLineFriendList();
                                    //主线程休眠一会，使得用户列表信息先显示
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "2":
                                    System.out.print("请输入你要群发的内容：");
                                    String toALLContent = Utility.readString(100); //聊天内容
                                    messageClientService.sendMsgToAll(toALLContent,userId);
                                    break;
                                case "3":
                                    List<Friend> allFriend0 = friendService.findAllFriend(userId);
                                    new FriendListView().showFriendList(allFriend0);
                                    System.out.print("请输入你要聊天的用户：");
                                    //这里目前只能在线用户通讯，后期使用数据库将消息存入数据库后就可以实现离线留言功能
                                    String getterId = Utility.readString(20); //接收者Id
                                    // TODO: 2022-05-12 之后在页面，设置一个按钮，一点击，携带所有参数进入ChatView页面
                                    new ChatView(userId,getterId).chat();

                                    break;
                                case "4":
                                    System.out.print("请输入你要发送文件的用户：");
                                    String file_getter = Utility.readString(20); //接收者Id
                                    System.out.print("请输入发送文件本地路径(如：D:\\pic.png)：");
                                    String src = Utility.readString(50);
                                    fileClientService.sendFile(src,userId,file_getter);
                                    break;
                                case "5":
                                    StringBuilder getters = new StringBuilder();
                                    System.out.println("\n=========群聊界面=========");
                                    System.out.print("请输入你要创建群聊的用户们（输入q结束）：");
                                    String getter = Utility.readString(20); //接收者Id
                                    while (!"q".equals(getter)){
                                        getters.append(getter + " ");
                                        System.out.print("请输入你要创建群聊的用户们（输入q结束）：");
                                        getter = Utility.readString(20); //接收者Id
                                    }
                                    System.out.print("请输入发送的内容：");
                                    String groupChatContent = Utility.readString(100); //聊天内容
                                    //这里目前只能在线用户通讯，后期使用数据库将消息存入数据库后就可以实现离线留言功能
                                    messageClientService.groupChat(groupChatContent,userId,getters.toString());
                                    break;
                                case "6":
                                    List<Friend> allFriend = friendService.findAllFriend(userId);
                                    new FriendListView().showFriendList(allFriend);
                                    break;
                                case "8":
                                    System.out.println("\n=========删除好友=========");
                                    // TODO: 2022-05-12 之后就在用户界面用button事件删除，可以加个确定框
                                    System.out.print("请输入你要删除的好友：");
                                    String del_friend = Utility.readString(20);
                                    boolean b = friendService.deleteFriend(userId, del_friend);
                                    if(b){
                                        System.out.println("删除好友"+del_friend+"成功");
                                    }else {
                                        System.out.println("删除失败");
                                    }
                                    break;
                                case "9":
                                    List<Friend> allFriend2 = friendService.findAllFriend(userId);
                                    new FriendListView().showFriendList(allFriend2);
                                    // TODO: 2022-05-12 之后在页面，设置一个按钮，一点击，携带所有参数进入UpdateFriendView页面
                                    System.out.print("请输入你要修改好友的信息：");
                                    String update_friend = Utility.readString(20);
                                    Friend updateFriend = null;
                                    for (Friend friend : allFriend2) {
                                        if(friend.getFriendId().equals(update_friend)){
                                            updateFriend = friend;
                                            break;
                                        }
                                    }
                                    new UpdateFriendView(userId).update(updateFriend);
                                    break;
                                case "a":
                                    new SearchUserView(userId).searchUserById();
                                    break;
                                case "0":
                                    System.out.println("退出系统成功！");
                                    //调用userClientService的退出方法
                                    userService.exit();
                                    loop = false;
                                    break;
                            }
                        }
                    }else if(StateCode.FAIL.equals(stateCode)){
                        System.out.println("登录失败 账号名或密码错误！");
                    }else if(StateCode.HAS_LOGIN.equals(stateCode)){
                        System.out.println("您已登录,请勿重复登录!");
                    }
                    break;
                case "0":
                    System.out.println("退出系统");
                    loop = false;
                    break;
            }

        }
    }
}
