package com.yx.hkdemo;

import com.yx.hkdemo.config.GetHCNetSdk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HkdemoApplication {

    public static void main(String[] args) {

        SpringApplication.run(HkdemoApplication.class, args);
        GetHCNetSdk getHCNetSdk = new GetHCNetSdk();
        System.out.println("进入main：准备初始化---------");
        HCNetSDK hcNetSDK = GetHCNetSdk.hcNetSDK;
        System.out.println("hcNetSdk: "+hcNetSDK);
        HCNetTools tools = new HCNetTools(hcNetSDK);

//
        System.out.println("初始化开始--------");
        int i = tools.initDevices();
        System.out.println("初始化："+i);
        System.out.println("注册开始----------");
        int i1 = tools.deviceRegist("admin", "yykj12345", "113.204.57.202", "8000");
        System.out.println("注册："+i1);
        System.out.println("获取通道号开始------------");
        int channelNumber = tools.getChannelNumber();
        System.out.println("通道号："+channelNumber);

        int signalStatus = tools.getSignalStatus(channelNumber);
        System.out.println("信道状态："+signalStatus);

//        boolean b = tools.takePic();
//        System.out.println("抓拍："+b);

        tools.shutDownDev();
    }

}
