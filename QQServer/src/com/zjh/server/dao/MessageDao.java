package com.zjh.server.dao;

import com.zjh.common.Message;
import com.zjh.common.MessageType;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.zjh.server.utils.MyDsUtils.queryRunner;

/**
 * @author 张俊鸿
 * @description: 消息记录存入、读出、修改的dao类
 * @since 2022-05-12 16:09
 */
public class MessageDao {
    public static void main(String[] args) {
//        Message message = new Message();
//        message.setSenderId("zjh");
//        message.setGetterId("a");
//        message.setContent("我是张俊鸿");
//        message.setMsgType(MessageType.MESSAGE_COMMON_MSG);
//        message.setSendTime(new Date());
//        System.out.println(new MessageDao().insertMsg(message, true));
//        System.out.println(new MessageDao().updateMsg(7));
//        List<Message> allMsg = new MessageDao().getAllMsg("a", "zjh");
////        List<Message> allMsg = new MessageDao().getOffLineMsg("a");
//        for (Message message : allMsg) {
//            System.out.println(message);
//        }
//        new MessageDao().clearMsg("a","zjh");
    }

    /**
     * 插入消息记录
     *
     * @param msg     消息
     * @param isSuccess 成功
     * @return boolean
     */
    public boolean insertMsg(Message msg,boolean isSuccess){
        boolean flag = false;
        Object[] params = {msg.getSenderId(),msg.getGetterId(),msg.getGroupId(),msg.getContent(),msg.getMsgType(),msg.getSendTime(),
                            msg.getFileName(),isSuccess};
        String sql = "INSERT INTO `message` (`sender_id`,`getter_id`,`group_id`,`content`,`type`,`send_time`,`file_name`,`success`) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try {
            int update = queryRunner.update(sql, params);
            if(update > 0) flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 更新消息状态
     * @param msgId 消息id
     * @return boolean
     */
    public boolean updateMsg(int msgId){
        boolean flag = false;
        String sql = "UPDATE `message` SET `success` = 1 WHERE `msg_id` = ?";
        try {
            int update = queryRunner.update(sql, msgId);
            if(update > 0) flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 私聊获取聊天记录 普通消息 文件消息 群发消息
     * 过滤掉被用户删除的聊天记录
     *
     * @param myId     用户id
     * @param friendId 聊天朋友id
     * @return {@link List}<{@link Message}>
     */
    public List<Message> getAllMsg(String myId,String friendId){
        List<Message> list = new ArrayList<>();
        Object[] params = {myId,friendId,myId,friendId};
        String sql = "SELECT `msg_id` AS msgId,`sender_id` AS senderId, `getter_id` AS getterId,`group_id` AS groupId,\n" +
                "`content` AS content,`send_time` AS sendTime,`type` AS msgType,`file_name` AS fileName\n" +
                " FROM `message` WHERE (`sender_id` = ? AND `getter_id` = ? AND `sender_del` = 0) OR(`getter_id` = ? AND `sender_id` = ? AND `getter_del` = 0)  " +
                "HAVING `type`  IN (4,8,10)";
        try {
            list = queryRunner.query(sql,new BeanListHandler<>(Message.class),params);
        } catch (SQLException e) {
            e.printStackTrace();
            throw  new DaoException("获取消息记录异常");
        }
        return list;
    }

    /**
     * 获取用户的离线留言
     *
     * @param myId 用户id
     * @return {@link List}<{@link Message}>
     */
    public List<Message> getOffLineMsg(String myId){
        List<Message> list = new ArrayList<>();
        String sql = "SELECT `msg_id` AS msgId,`sender_id` AS senderId, `getter_id` AS getterId,`group_id` AS groupId,\n" +
                "`content` AS content,`send_time` AS sendTime,`type` AS msgType,`file_name` AS fileName\n" +
                "FROM `message` WHERE `getter_id` = ? AND `success` = 0";
        try {
            list = queryRunner.query(sql,new BeanListHandler<>(Message.class),myId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw  new DaoException("获取消息记录异常");
        }
        return list;
    }

    /**
     * 清空消息记录
     * 单删是不会删除聊天记录的，回删就删除聊天记录
     *
     * @param myId     用户id
     * @param friendId 朋友id
     * @return boolean
     */
    public boolean clearMsg(String myId,String friendId){
        boolean flag = false;
        Object[] params = {myId,friendId,myId,friendId};
        String sql = "DELETE FROM `message` WHERE (`sender_id` = ? AND `getter_id` = ?) OR(`getter_id` = ? AND `sender_id` = ?) ";
        try {
            int update = queryRunner.update(sql, params);
            if(update > 0){
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw  new DaoException("清空消息记录异常");
        }
        return flag;
    }

    /**
     * 单删，用户不可见聊天记录，更新状态
     * 就算再加回来，之前的聊天记录也没了
     *
     * @param myId     我身份证
     * @param friendId 朋友id
     * @return boolean
     */
    public boolean updateDel(String myId,String friendId){
        boolean flag = false;
        Object[] params = {myId,friendId};
        String sql1 = "UPDATE `message` SET `sender_del` = 1 WHERE `sender_id` = ? AND `getter_id` = ?;";
        String sql2 = "UPDATE `message` SET `getter_del` = 1 WHERE `getter_id` = ? AND `sender_id` = ?";
        try {
            int update1 = queryRunner.update(sql1, params);
            int update2 = queryRunner.update(sql2, params);
            if(update1 > 0 && update2 > 0){
                flag = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
