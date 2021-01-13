package com.yx.hkdemo;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;

//import org.apache.commons.lang.StringUtils;

//import cc.eguid.FFmpegCommandManager.FFmpegManager;
//import cc.eguid.FFmpegCommandManager.FFmpegManagerImpl;
//import com.dfzx.common.util.CommonKit;
@Data
@AllArgsConstructor
public class HCNetTools {
    HCNetSDK hCNetSDK = null;
    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息
    HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg;//IP参数
    HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;//用户参数
    boolean bRealPlay;//是否在预览.
    String m_sDeviceIP;//已登录设备的IP地址
    NativeLong lUserID;//用户句柄
    NativeLong lPreviewHandle;//预览句柄
    NativeLongByReference m_lPort;//回调预览时播放库端口指针

    public HCNetTools(HCNetSDK sdk) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);//防止被播放窗口(AWT组件)覆盖
        lUserID = new NativeLong(-1);
        lPreviewHandle = new NativeLong(-1);
        m_lPort = new NativeLongByReference(new NativeLong(-1));
        this.hCNetSDK = sdk;
    }


//    FFmpegManager manager;//rstp转rmtp工具


    //FRealDataCallBack fRealDataCallBack;//预览回调函数实现

    public HCNetTools() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);//防止被播放窗口(AWT组件)覆盖
        lUserID = new NativeLong(-1);
        lPreviewHandle = new NativeLong(-1);
        m_lPort = new NativeLongByReference(new NativeLong(-1));
        //fRealDataCallBack= new FRealDataCallBack();
    }

    /**
     * 初始化资源配置
     */
    public int initDevices() {
        if (!hCNetSDK.NET_DVR_Init()) return 1;//初始化失败
        return 0;
    }

    /**
     * 设备注册
     *
     * @param name     设备用户名
     * @param password 设备登录密码
     * @param ip       IP地址
     * @param port     端口
     * @return 结果
     */
    public int deviceRegist(String name, String password, String ip, String port) {
        if (bRealPlay) {//判断当前是否在预览
            return 2;//"注册新用户请先停止当前预览";
        }
        if (lUserID.longValue() > -1) {//先注销,在登录
            hCNetSDK.NET_DVR_Logout_V30(lUserID);
            lUserID = new NativeLong(-1);
        }
        System.out.println("注册里面:lUserId:"+lUserID);
        //注册(既登录设备)开始
        m_sDeviceIP = ip;
        m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();//获取设备参数结构
        System.out.println("注册里面：ip:"+m_sDeviceIP+"     info:"+m_strDeviceInfo);
        lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP, (short) Integer.parseInt("8000"), name, password, m_strDeviceInfo);//登录设备
//        NativeLong lUserID, int dwCommand, NativeLong lChannel, Pointer lpInBuffer, int dwInBufferSize
        System.out.println("登录后的id"+lUserID);
        int i = hCNetSDK.NET_DVR_GetLastError();

        System.out.println("登录错误代码："+i);
        m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
        boolean b = hCNetSDK.NET_DVR_SetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_IPPARACFG, new NativeLong(0), lpIpParaConfig, m_strIpparaCfg.size());
        int seti = hCNetSDK.NET_DVR_GetLastError();
        System.out.println("setConfig"+seti);
        System.out.println("b:"+b);
        long userID = lUserID.longValue();
        System.out.println("注册里面：userId:"+userID);
        if (userID == -1) {
            m_sDeviceIP = "";//登录未成功,IP置为空
            return 3;//"注册失败";
        }

        return 0;
    }

    /**
     * 获取设备通道
     */
    public int getChannelNumber() {
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        boolean bRet = false;
        int iChannelNum = -1;

        m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
        m_strIpparaCfg.write();
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();

        bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_IPPARACFG, new NativeLong(0), lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
        int geti = hCNetSDK.NET_DVR_GetLastError();
        System.out.println("geti:"+geti);
        m_strIpparaCfg.read();

        String devices = "";
        System.out.println("bRet:"+bRet);
        if (!bRet) {
            //设备不支持,则表示没有IP通道
            System.out.println("C:"+m_strDeviceInfo.byChanNum);
            for (int iChannum = 0; iChannum < m_strDeviceInfo.byChanNum; iChannum++) {
                devices = "Camera" + (iChannum + m_strDeviceInfo.byStartChan);
            }
        } else {
            System.out.println("IP"+HCNetSDK.MAX_IP_CHANNEL);
            for (int iChannum = 0; iChannum < HCNetSDK.MAX_IP_CHANNEL; iChannum++) {
                if (m_strIpparaCfg.struIPChanInfo[iChannum].byEnable == 1) {
                    devices = "IPCamera" + (iChannum + m_strDeviceInfo.byStartChan);
                }
            }
        }
        System.out.println("通道："+devices);
        if (devices != null && devices != "") {
            if (devices.charAt(0) == 'C') {//Camara开头表示模拟通道
                //子字符串中获取通道号
                System.out.println("C");
                iChannelNum = Integer.parseInt(devices.substring(6));
            } else {
                if (devices.charAt(0) == 'I') {//IPCamara开头表示IP通道
                    System.out.println("I");
                    //子字符创中获取通道号,IP通道号要加32
                    iChannelNum = Integer.parseInt(devices.substring(8)) + 32;
                } else {
                    return 4;
                }
            }
        }
        return iChannelNum;
    }

    /**
     * 获取通道信号状态
     *
     * @param channum
     * @return
     */
    public int getSignalStatus(int channum) {
        //获取设备状态
        HCNetSDK.NET_DVR_WORKSTATE_V30 devwork = new HCNetSDK.NET_DVR_WORKSTATE_V30();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState_V30(lUserID, devwork)) {
            //返回Boolean值，判断是否获取设备能力
            System.out.println("返回设备状态失败");
        }
        return devwork.struChanStatic[channum].bySignalStatic;
    }

    /**
     * 拍照
     *
     * @return
     */
    public boolean takePic() {
        //拍照
        HCNetSDK.NET_DVR_JPEGPARA strJpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        strJpeg.wPicQuality = 1; //图像参数
        strJpeg.wPicSize = 2;
        String filePath = "E:\\123q.jpg";
        lPreviewHandle.setValue(m_strDeviceInfo.byStartChan + 32);
        boolean b = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, lPreviewHandle, strJpeg, filePath);//尝试用NET_DVR_CaptureJPEGPicture_NEW方法，但不是报43就是JDK崩溃....
        if (!b) {//单帧数据捕获图片
            System.out.println("抓拍失败!" + " err: " + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("抓拍成功");
        }
        return b;
    }

    /**
     * 释放SDK
     */
    public void shutDownDev() {
        //如果已经注册,注销
        if (lUserID.longValue() > -1) {
            hCNetSDK.NET_DVR_Logout_V30(lUserID);
        }
        hCNetSDK.NET_DVR_Cleanup();
    }

}
