package cn.edu.uestc;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RETest {
    public static void main(String[] args) {

    }

    public static ArrayList<String> getName() {
        String str = " title=\"成语小秀才\" class=\"\"><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/9d51986d8468eea48dc9bfbe8f7cb6aa.png\"></span></span></span><span><span><span><span><span><span><span><span><span><span><span><span>成语小秀才</span></span></span></span></span></span></span></span></span></span></span></span></a></li><li>游戏</li><li>2</li><li>1万</li><li>-</li><li><span>8626</span><i class=\"top\"></i></li></ul></div><div><ul><li>7</li><li></li><li><a href=\"/app/87799790/index.html\" target=\"_blank\" title=\"全民K歌\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/cc315ed2dfe59eb335fb7ef539bbcfe8.png\"></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span>全民K歌</span></span></span></span></span></span></span></span></span></span></a></li><li>音频</li><li>5</li><li>1.2万</li><li>-</li><li><span>8324</span><i class=\"top\"></i></li></ul></div><div><ul><li>8</li><li></li><li><a href=\"/app/88689931/index.html\" target=\"_blank\" title=\"欢乐斗地主\" class=\"\"><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/954ceeac7811c4864cadfc15b4af53b3.png\"></span></span></span></span><span><span><span><span><span><span><span><span><span>欢乐斗地主</span></span></span></span></span></span></span></span></span></a></li><li>游戏</li><li>2</li><li>7.8万</li><li>-</li><li><span>8137</span><i class=\"top\"></i></li></ul></div><div><ul><li>9</li><li></li><li><a href=\"/app/88686225/index.html\" target=\"_blank\" title=\"步数宝\" class=\"\"><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/f3658c5105121f676ae9e911a8a65003.png\"></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span>步数宝</span></span></span></span></span></span></span></span></a></li><li>工具</li><li>2</li><li>1.1万</li><li>405</li><li><span>8129</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>10</li><li></li><li><a href=\"/app/87563880/index.html\" target=\"_blank\" title=\"消灭病毒\" class=\"\"><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/afbd87deb79f3b77fd514f84b0ac3cf8.png\"></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span><span>消灭病毒</span></span></span></span></span></span></span></span></span></span></span></a></li><li>游戏</li><li>4</li><li>0.5万</li><li>-</li><li><span>8116</span><i class=\"top\"></i></li></ul></div><div><ul><li>11</li><li></li><li><a href=\"/app/88687146/index.html\" target=\"_blank\" title=\"青桔单车\" class=\"\"><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/6bd3219ee0d9f3dfba2247eaf268f17c.png\"></span></span></span></span></span></span></span></span></span><span><span><span><span><span>青桔单车</span></span></span></span></span></a></li><li>生活服务</li><li>0</li><li>0</li><li>463</li><li><span>8108</span><i class=\"top\"></i></li></ul></div><div><ul><li>12</li><li></li><li><a href=\"/app/88690896/index.html\" target=\"_blank\" title=\"乘车码\" class=\"\"><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/354325a90085882b8ddc4daaacd3234b.png\"></span></span></span><span><span><span><span><span><span><span><span><span><span>乘车码</span></span></span></span></span></span></span></span></span></span></a></li><li>生活服务</li><li>35</li><li>32.6万</li><li>-</li><li><span>8030</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>13</li><li></li><li><a href=\"/app/88689983/index.html\" target=\"_blank\" title=\"猫眼电影演出I电影票演唱会话剧\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/67c908f47078e610b8c08db668972969.png\"></span></span></span></span></span></span></span></span><span><span><span>猫眼电影演出I电...</span></span></span></a></li><li>生活服务</li><li>45</li><li>28.2万</li><li>-</li><li><span>8021</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>14</li><li></li><li><a href=\"/app/88692635/index.html\" target=\"_blank\" title=\"车来了精准实时公交\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/d1ce7c4fa1d999c6852e9e0d20d10dce.png\"></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span>车来了精准实时公...</span></span></span></span></span></span></span></span></a></li><li>生活服务</li><li>70</li><li>21万</li><li>424</li><li><span>8005</span><i class=\"top\"></i></li></ul></div><div><ul><li>15</li><li></li><li><a href=\"/app/88691742/index.html\" target=\"_blank\" title=\"摩拜单车\" class=\"\"><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/02bdeff5c1b5813996864c3c274056c2.png\"></span></span><span><span><span><span><span><span><span><span><span><span><span>摩拜单车</span></span></span></span></span></span></span></span></span></span></span></a></li><li>生活服务</li><li>88</li><li>23万</li><li>-</li><li><span>7963</span><i class=\"top\"></i></li></ul></div><div><ul><li>16</li><li></li><li><a href=\"/app/88691933/index.html\" target=\"_blank\" title=\"大众点评美食电影运动旅游门票\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/e66c9de776c64c8068469375f64c0d62.png\"></span></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span>大众点评美食电影...</span></span></span></span></span></span></span></a></li><li>生活服务</li><li>51</li><li>34.8万</li><li>-</li><li><span>7960</span><i class=\"top\"></i></li></ul></div><div><ul><li>17</li><li></li><li><a href=\"/app/88687735/index.html\" target=\"_blank\" title=\"贝壳找房买房房价租房新房二手房\" class=\"\"><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/5ed1f13c3646a0c2f4d75a48286cab24.png\"></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span>贝壳找房买房房价...</span></span></span></span></span></span></span></span></span></a></li><li>生活服务</li><li>37</li><li>2.1万</li><li>637</li><li><span>7874</span><i class=\"top\"></i></li></ul></div><div><ul><li>18</li><li></li><li><a href=\"/app/88689612/index.html\" target=\"_blank\" title=\"知乎热榜\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/db19fcb7800b900e65144e8aaa3b9d2c.png\"></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span>知乎热榜</span></span></span></span></span></a></li><li>内容资讯</li><li>9</li><li>1.7万</li><li>413</li><li><span>7871</span><i class=\"top\"></i></li></ul></div><div><ul><li>19</li><li></li><li><a href=\"/app/88692818/index.html\" target=\"_blank\" title=\"糖豆广场舞\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/f170c36920884bf4c51d019c523bcff6.png\"></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span>糖豆广场舞</span></span></span></span></span></span></span></span></span></a></li><li>视频</li><li>4</li><li>2.3万</li><li>418</li><li><span>7852</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>20</li><li></li><li><a href=\"/app/88454702/index.html\" target=\"_blank\" title=\"开心消消乐\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/5d566006d2e80c5021fd5662bef0b5aa.png\"></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span>开心消消乐</span></span></span></span></span></span></span></a></li><li>游戏</li><li>3</li><li>&lt;0.1万</li><li>-</li><li><span>7845</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>21</li><li></li><li><a href=\"/app/88692942/index.html\" target=\"_blank\" title=\"美团丨外卖美食电影酒店门票健身\" class=\"\"><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/1a5795099245d71c07a03051be1ce19f.png\"></span></span></span></span></span><span><span><span><span><span><span><span><span>美团丨外卖美食电...</span></span></span></span></span></span></span></span></a></li><li>生活服务</li><li>16</li><li>27.2万</li><li>398</li><li><span>7837</span><i class=\"top\"></i></li></ul></div><div><ul><li>22</li><li></li><li><a href=\"/app/88692974/index.html\" target=\"_blank\" title=\"快递100\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/31f24c8f4047f6592ddf28b83db8d377.png\"></span></span></span></span></span></span></span></span><span><span><span><span><span><span>快递100</span></span></span></span></span></span></a></li><li>生活服务</li><li>447</li><li>10.6万</li><li>449</li><li><span>7789</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>23</li><li></li><li><a href=\"/app/88691092/index.html\" target=\"_blank\" title=\"每日优鲜 水果生鲜买菜外卖\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/7fea9f394c5c5544d2b261ddf7bdc490.png\"></span></span></span></span></span></span></span></span><span><span><span><span><span>每日优鲜 水果生...</span></span></span></span></span></a></li><li>网络购物</li><li>46</li><li>36.3万</li><li>475</li><li><span>7772</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>24</li><li></li><li><a href=\"/app/88692715/index.html\" target=\"_blank\" title=\"腾讯相册\" class=\"\"><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/ddfa088f13526d95d72af645a5b568b8.png\"></span></span></span></span></span><span><span><span><span><span><span><span><span><span>腾讯相册</span></span></span></span></span></span></span></span></span></a></li><li>图片摄影</li><li>6</li><li>9.5万</li><li>466</li><li><span>7761</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>25</li><li></li><li><a href=\"/app/87598215/index.html\" target=\"_blank\" title=\"金山文档\" class=\"\"><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/953eb9118d55a61f4f6a6909ad196568.png\"></span></span></span></span></span></span></span><span><span><span>金山文档</span></span></span></a></li><li>工具</li><li>6</li><li>20.8万</li><li>-</li><li><span>7760</span><i class=\"top\"></i></li></ul></div><div><ul><li>26</li><li></li><li><a href=\"/app/87927398/index.html\" target=\"_blank\" title=\"热门微博\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/4fd65074d1e25b508206a689ad82d331.png\"></span></span></span></span></span></span><span><span><span><span><span><span>热门微博</span></span></span></span></span></span></a></li><li>内容资讯</li><li>12</li><li>0.8万</li><li>-</li><li><span>7722</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>27</li><li></li><li><a href=\"/app/88191813/index.html\" target=\"_blank\" title=\"多客拼团\" class=\"\"><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/c0c9513de5c5cecf2fae6b982c44f5f5.png\"></span></span></span></span></span><span><span><span><span><span><span><span>多客拼团</span></span></span></span></span></span></span></a></li><li>网络购物</li><li>3</li><li>0.5万</li><li>501</li><li><span>7590</span><i class=\"top\"></i></li></ul></div><div><ul><li>28</li><li></li><li><a href=\"/app/88379423/index.html\" target=\"_blank\" title=\"小板凳群相册\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/bcd373de327866885fdec1115791ffca.png\"></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span><span>小板凳群相册</span></span></span></span></span></span></span></span></span></span></span></a></li><li>内容资讯</li><li>1</li><li>0.7万</li><li>-</li><li><span>7575</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>29</li><li></li><li><a href=\"/app/88692943/index.html\" target=\"_blank\" title=\"58同城\" class=\"\"><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/90649fe022897aa592dc57f90774eb32.png\"></span></span></span></span></span></span></span></span></span></span><span><span><span>58同城</span></span></span></a></li><li>生活服务</li><li>5</li><li>21万</li><li>439</li><li><span>7566</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>30</li><li></li><li><a href=\"/app/88690811/index.html\" target=\"_blank\" title=\"唯品会\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/60a257bbc7224f8c13bf9429076580ff.png\"></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span><span>唯品会</span></span></span></span></span></span></span></span></span></span></span></a></li><li>网络购物</li><li>783</li><li>421.1万</li><li>509</li><li><span>7536</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>31</li><li></li><li><a href=\"/app/87558765/index.html\" target=\"_blank\" title=\"成语中状元\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/ad_advertiser/1554279212.jpg\"></span></span></span></span></span></span><span><span><span><span>成语中状元</span></span></span></span></a></li><li>游戏</li><li>1</li><li>0.2万</li><li>-</li><li><span>7477</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>32</li><li></li><li><a href=\"/app/87559082/index.html\" target=\"_blank\" title=\"快手购物助手\" class=\"\"><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/ad_advertiser/1553841650.jpg\"></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span>快手购物助手</span></span></span></span></span></span></span></span></span></a></li><li>网络购物</li><li>0</li><li>0</li><li>513</li><li><span>7430</span><i class=\"top\"></i></li></ul></div><div><ul><li>33</li><li></li><li><a href=\"/app/88687621/index.html\" target=\"_blank\" title=\"票圈长视频\" class=\"\"><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/9224cdea996113db11fb101c0ac14da7.png\"></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span>票圈长视频</span></span></span></span></span></span></span></span></a></li><li>视频</li><li>361</li><li>78.9万</li><li>504</li><li><span>7429</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>34</li><li></li><li><a href=\"/app/88675737/index.html\" target=\"_blank\" title=\"天天斗地主真人版\" class=\"\"><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/e3810b066e8b60ba349549a43015758b.png\"></span></span></span></span><span><span><span><span><span><span><span><span><span><span>天天斗地主真人版</span></span></span></span></span></span></span></span></span></span></a></li><li>游戏</li><li>74</li><li>4.9万</li><li>297</li><li><span>7420</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>35</li><li></li><li><a href=\"/app/88560381/index.html\" target=\"_blank\" title=\"京东超级品牌日\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/b38aa585709dd73dd2386d7c9de8c808.png\"></span></span></span></span></span></span></span></span><span><span><span><span>京东超级品牌日</span></span></span></span></a></li><li>网络购物</li><li>22</li><li>25万</li><li>424</li><li><span>7405</span><i class=\"top\"></i></li></ul></div><div><ul><li>36</li><li></li><li><a href=\"/app/88692971/index.html\" target=\"_blank\" title=\"中通助手\" class=\"\"><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/7c431cef3f127dd4b36168d9fda99e47.png\"></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span><span>中通助手</span></span></span></span></span></span></span></span></span></span></span></a></li><li>生活服务</li><li>5</li><li>40.7万</li><li>-</li><li><span>7388</span><i class=\"top\"></i></li></ul></div><div><ul><li>37</li><li></li><li><a href=\"/app/88694342/index.html\" target=\"_blank\" title=\"携程订酒店机票火车票汽车票门票\" class=\"\"><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/ad_advertiser/1551248588.jpg\"></span></span><span><span><span>携程订酒店机票火...</span></span></span></a></li><li>旅游</li><li>159</li><li>18万</li><li>372</li><li><span>7323</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>38</li><li></li><li><a href=\"/app/88685669/index.html\" target=\"_blank\" title=\"优酷视频\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/ea698f420467c38b4463a62203358adc.png\"></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span><span>优酷视频</span></span></span></span></span></span></span></span></span></span></span></a></li><li>视频</li><li>33</li><li>3.1万</li><li>385</li><li><span>7302</span><i class=\"top\"></i></li></ul></div><div><ul><li>39</li><li></li><li><a href=\"/app/87927010/index.html\" target=\"_blank\" title=\"运动赚\" class=\"\"><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/e839162b8b83916acc9f7a08c318e6b6.png\"></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span><span>运动赚</span></span></span></span></span></span></span></span></span></span></span></a></li><li>工具</li><li>27</li><li>12.9万</li><li>-</li><li><span>7286</span><i class=\"top\"></i></li></ul></div><div><ul><li>40</li><li></li><li><a href=\"/app/88183882/index.html\" target=\"_blank\" title=\"食享会\" class=\"\"><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/63ff3133e412d07c3485db3b775ee6c0.png\"></span></span></span></span></span></span></span><span><span><span><span><span><span>食享会</span></span></span></span></span></span></a></li><li>网络购物</li><li>0</li><li>0</li><li>510</li><li><span>7263</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>41</li><li></li><li><a href=\"/app/87927245/index.html\" target=\"_blank\" title=\"绝地求生刺激战场\" class=\"\"><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/08287fe8b7d849e6c48732885dc79cd3.png\"></span></span><span><span><span><span><span><span><span>绝地求生刺激战场</span></span></span></span></span></span></span></a></li><li>游戏</li><li>4</li><li>0.1万</li><li>-</li><li><span>7254</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>42</li><li></li><li><a href=\"/app/88687054/index.html\" target=\"_blank\" title=\"女王新款+\" class=\"\"><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/be979568d68a03f4f0229221bd3bb4d2.png\"></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span>女王新款+</span></span></span></span></span></span></span></span></span></a></li><li>网络购物</li><li>448</li><li>194.5万</li><li>438</li><li><span>7244</span><i class=\"top\"></i></li></ul></div><div><ul><li>43</li><li></li><li><a href=\"/app/88691111/index.html\" target=\"_blank\" title=\"小电充电\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/77a144ec05deaa173bb726d7a6279e93.png\"></span></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span>小电充电</span></span></span></span></span></a></li><li>工具</li><li>3</li><li>5.3万</li><li>433</li><li><span>7241</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>44</li><li></li><li><a href=\"/app/88686637/index.html\" target=\"_blank\" title=\"米多乐\" class=\"\"><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/a8d9b57548629bc558ae227f3ee97cca.png\"></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span>米多乐</span></span></span></span></span></span></a></li><li>社交</li><li>27</li><li>1.5万</li><li>485</li><li><span>7237</span><i class=\"top\"></i></li></ul></div><div><ul><li>45</li><li></li><li><a href=\"/app/87814983/index.html\" target=\"_blank\" title=\"泡泡龙大师\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/9049560894072221bdb00c2f32a136de.png\"></span></span></span></span></span></span><span><span><span><span>泡泡龙大师</span></span></span></span></a></li><li>游戏</li><li>1</li><li>&lt;0.1万</li><li>-</li><li><span>7232</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>46</li><li></li><li><a href=\"/app/88686924/index.html\" target=\"_blank\" title=\"京东优惠\" class=\"\"><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/78c4eba0b57efc11a418220173a72a54.png\"></span></span></span></span><span><span><span><span><span><span><span>京东优惠</span></span></span></span></span></span></span></a></li><li>网络购物</li><li>84</li><li>138.3万</li><li>406</li><li><span>7222</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>47</li><li></li><li><a href=\"/app/88694345/index.html\" target=\"_blank\" title=\"汽车之家\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/57d112e245f4f29fb4774b0c45f415c7.png\"></span></span></span></span></span></span></span></span><span><span><span><span><span><span>汽车之家</span></span></span></span></span></span></a></li><li>内容资讯</li><li>11</li><li>19.1万</li><li>456</li><li><span>7218</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>48</li><li></li><li><a href=\"/app/88691023/index.html\" target=\"_blank\" title=\"微店+\" class=\"\"><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/f104125d0130ed9d080024ec734a389b.png\"></span></span></span></span></span></span><span><span><span><span><span><span><span><span>微店+</span></span></span></span></span></span></span></span></a></li><li>网络购物</li><li>150</li><li>55.6万</li><li>427</li><li><span>7166</span><i class=\"top\"></i></li></ul></div><div><ul><li>49</li><li></li><li><a href=\"/app/87927011/index.html\" target=\"_blank\" title=\"兴盛优选\" class=\"\"><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/9a66e65600475c763f49bb243ae85eea.png\"></span></span></span></span></span></span></span></span><span><span><span><span><span><span>兴盛优选</span></span></span></span></span></span></a></li><li>线下零售</li><li>1</li><li>0</li><li>-</li><li><span>7166</span><i class=\"bottom\"></i></li></ul></div><div><ul><li>50</li><li></li><li><a href=\"/app/88691069/index.html\" target=\"_blank\" title=\"转转二手交易网\" class=\"\"><span><span><span><span><span><span><span><span><span><span><span><img src=\"http://aldpicsh-1252823355.cossh.myqcloud.com/caijicons/4a43ffb3247a991989411324399410af.png\"></span></span></span></span></span></span></span></span></span></span></span><span><span><span><span><span><span><span><span><span><span>转转二手交易网</span></span></span></span></span></span></span></span></span></span></a></li><li>网络购物</li><li>13</li><li>9.6万</li><li>-</li><li><span>7162</span><i class=\"top\"></i></li></ul></div></div><div class=\"base-miniapp-rank-tbody miniapp-no-list\" style=\"display:none;\"><span>该分类榜单暂未开放，请查看<em>小程序总榜</em></span></div><a href=\"/toplist?type=0&amp;typeid=0&amp;date=1&amp;tabsactive=0\" target=\"_blank\" class=\"base-miniapp-rank-tfoot\" style=\"display:none;\">点击查看更多小程序排行</a></div>";
        Pattern pattern = Pattern.compile("title=\"(.*?)\" class=");
        Matcher matcher = pattern.matcher(str);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }
}