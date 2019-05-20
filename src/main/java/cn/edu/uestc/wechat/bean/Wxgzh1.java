package cn.edu.uestc.wechat.bean;

import java.util.HashMap;

public class Wxgzh1 {

    private String biz;
    private String zt;
    private String qyqc;
    private String mtmc;
    private String jgmc;
    private String xb;
    private String dq;
    private String mcjl;
    private String rzsj;
    private String mtdwmc;
    private String gszzzch;
    private String jyfw;
    private String ybjyfw;
    private String qzxkjyfw;
    private String qylx;
    private String mtdwlx;
    private String jglx;
    private String qyclrq;
    private String qyyyqx;
    private String jgyxq;
    private String fws;
    private String other;

    public Wxgzh1(HashMap<String, String> map) {
        for (String key : map.keySet()) {
            String value = map.get(key);
            switch (key) {
                case "biz":
                    this.biz = value;
                    continue;
                case "主体":
                    this.zt = value;
                    continue;
                case "企业全称":
                    this.qyqc = value;
                    continue;
                case "媒体名称":
                    this.mtmc = value;
                    continue;
                case "机构名称":
                    this.jgmc = value;
                    continue;
                case "性别":
                    this.xb = value;
                    continue;
                case "地区":
                    this.dq = value;
                    continue;
                case "名称记录":
                    this.mcjl = value;
                    continue;
                case "认证时间于":
                    this.rzsj = value;
                    continue;
                case "媒体单位名称":
                    this.mtdwmc = value;
                    continue;
                case "工商执照注册号/统一社会信用代码":
                    this.gszzzch = value;
                    continue;
                case "经营范围":
                    this.jyfw = value;
                    continue;
                case "经营范围(一般经营范围)":
                    this.ybjyfw = value;
                    continue;
                case "经营范围(前置许可经营范围)":
                    this.qzxkjyfw = value;
                    continue;
                case "企业类型":
                    this.qylx = value;
                    continue;
                case "媒体单位类型":
                    this.mtdwlx = value;
                    continue;
                case "机构类型":
                    this.jglx = value;
                    continue;
                case "企业成立日期":
                    this.qyclrq = value;
                    continue;

                case "企业营业期限":
                    this.qyyyqx = value;
                    continue;
                case "机构有效期":
                    this.jgyxq = value;
                    continue;
                case "服务商":
                    this.fws = value;
                    continue;
                default:
                    this.other = value;
            }
        }
    }
}
