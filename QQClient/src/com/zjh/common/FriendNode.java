package com.zjh.common;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author 张俊鸿
 * @description: 好友列表好友结点
 * @since 2022-05-22 16:35
 */
public class FriendNode extends DefaultMutableTreeNode {
    private Friend friend;

    public FriendNode(Friend friend) {
        //文本框放昵称和个性签名
        super(friend.getFriendName() + friend.getSignature());
        this.friend = friend;
    }
    public ImageIcon getImageIcon() {
        if(friend.isOnLine()){
            return new ImageIcon(friend.getAvatar());
        }else {
            return getGrayImage(new ImageIcon(friend.getAvatar()));
        }
    }
    //图片灰度化处理
    public ImageIcon getGrayImage(ImageIcon icon){
        int w=icon.getIconWidth();
        int h=icon.getIconHeight();
        BufferedImage buff=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics g=buff.getGraphics();
        g.drawImage(icon.getImage(),0,0,null);
        for (int i=0;i<w;i++){
            for(int j=0;j<h;j++){
                int red,green,blue;
                int pixel=buff.getRGB(i,j);
                red=(pixel>>16)&0xFF;
                green=(pixel>>8)&0xFF;
                blue=(pixel>>0)&0xFF;
                int sum=(red+green+blue)/3;
                g.setColor(new Color(sum,sum,sum));
                g.fillRect(i,j,1,1);
            }
        }
        return new ImageIcon(buff);
    }
}