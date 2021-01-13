package com.yx.hkdemo.config;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.yx.hkdemo.HCNetSDK;

import java.io.File;

/**
 * @author ：tangfan
 * @date ：Created in 2020/5/25 17:36
 * @description：
 * @modified By：
 */
public class GetHCNetSdk {
    public static HCNetSDK hcNetSDK = null;
    String PATH_WIN = System.getProperty("user.dir") + File.separator + "hkwinlib" + File.separator + "HCNetSDK";
    String PATH_LINUX = "/home/opt/hcnet/libhcnetsdk.so";
    private HCNetSDK.NET_DVR_DEVICEINFO_V30 deviceInfo;//设备信息
    private NativeLong lUserID;//用户句柄
    private NativeLong lAlarmHandle;//报警布防句柄
    //    private HCNetSDK.FMSGCallBack_V31 fMSFCallBack_V31;//报警回调函数实现
    private HCNetSDK.FMSGCallBack fMSFCallBack;//报警回调函数实现
    private String deviceIP;//已登录设备的IP地址
    private int devicePort;//设备端口号
    private String username;//设备用户名
    private String password;//设备登陆密码
    private boolean init_flag;//初始化识别标志
    private boolean reg_flag;//设备注册识别标志

    public GetHCNetSdk() {
        install();
    }

    private void install() {

        if (Platform.isWindows()) {
            System.out.println("windows开始");
            hcNetSDK = (HCNetSDK) Native.loadLibrary(PATH_WIN, HCNetSDK.class);
            System.out.println("windows结束");
        }
        if (Platform.isLinux()) {
            System.out.println("linux开始");
            hcNetSDK = (HCNetSDK) Native.loadLibrary(PATH_LINUX, HCNetSDK.class);
            //设置HCNetSDKCom组件库所在路径
//        /home/opt/hcnet/libhcnetsdk.so
//        /home/opt/hcnet
            String strPathCom = "/home/opt/hcnet";
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hcNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());

//设置libcrypto.so所在路径
            HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
            String strPathCrypto = "/home/opt/hcnet/libcrypto.so";
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());

//设置libssl.so所在路径
            HCNetSDK.BYTE_ARRAY ptrByteArraySsl = new HCNetSDK.BYTE_ARRAY(256);
            String strPathSsl = "/home/opt/hcnet/libssl.so";
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArraySsl.getPointer());
            System.out.println("linux结束");
        }
    }
}
