package cn.edu.uestc.wechat.bean;

import java.util.HashMap;

public class Wxxcx1 {

    private String appid;
    private String fwjsj;
    private String yhys;
    private String mcjl;
    private String fwlm;
    private String kfz;
    private String fwsm;
    private String zhysid;
    private String gxsj;
    private String fws;
    private String fwzc;

    private String other;

    public Wxxcx1(HashMap<String, String> map) {
        for (String key : map.keySet()) {
            String value = map.get(key);
            switch (key) {
                case "AppID":
                    this.appid = value;
                    continue;
                case "服务及数据":
                    this.fwjsj = value;
                    continue;
                case "用户隐私及数据提示":
                    this.yhys = value;
                    continue;
                case "名称记录":
                    this.mcjl = value;
                    continue;
                case "服务类目":
                    this.fwlm = value;
                    continue;
                case "开发者":
                    this.kfz = value;
                    continue;
                case "服务声明":
                    this.fwsm = value;
                    continue;
                case "帐号原始ID":
                    this.zhysid = value;
                    continue;
                case "更新时间":
                    this.gxsj = value;
                    continue;
                case "服务商":
                    this.fws = value;
                    continue;
                case "服务支持":
                    this.fwzc = value;
                    continue;
                default:
                    this.other = value;
            }
        }
    }
}
