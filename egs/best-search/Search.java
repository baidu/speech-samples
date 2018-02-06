import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 本文件用来演示如何通过文本相似度进行纠错
 * <p>
 * <pre>
 * String[] list = new String[]{"张三", "张衫", "张丹", "张成", "李四", "李奎"};
 * Search d = new Search(list);
 * System.out.println(d.search("张三", 10));
 * System.out.println(d.search("李四", 10));
 *
 * 输出：
 * [{word=张三, score=0}, {word=张衫, score=1}, {word=张丹, score=1}, {word=张成, score=5}, {word=李四, score=9}, {word=李奎, score=10}]
 * [{word=李四, score=0}, {word=李奎, score=3}, {word=张三, score=9}, {word=张衫, score=10}, {word=张丹, score=10}, {word=张成, score=12}]
 * </pre>
 */
public class Search {
    final List<Word> targets = new ArrayList<Word>();

    public Search(String[] list) throws PinyinException {
        for (String s : list) {
            Word w = new Word(s);
            targets.add(w);
        }
    }

    public static void main(String[] args) throws PinyinException {
        String str = "陈宏子 秦婷婷 折蓉蓉 曹丽娜 李娜 姜钰 魏天霞 王官君 王红梅 郭淑慧 马海娟 孙洪山 胡丹丹 倪辰曦 胡书琴 冷静 李剑 范志坚 任娟娟 何圆 安瑞娟 汤敏树 史娟 王艳红 姚志奋 吴巧芬 陈墨 张琪 毛春华 王丽婧 曾顺 晏玉屏 彭玉林 曾盼丽 谭艳红 刘甜甜 王璨 刘彬彬 吴银红 徐晨曦 蔡婷 郑广荣 周蓓 陈小艳 丁欣如 谢辅仁 张嘉 李雯婷 宋玉红 毛晨霞 余少虹 余少华 周莹 卢兰凤 万腾龙 张博 何羽衣 林立 朱红艳 李允麒 魏霖 许超丽 何柳青 黄鸿江 孙婷 黎锦荣 谢剑萍 王峥 洪欣意 唐芳 肖舒 何元 杨嘉怡 张佳婧 王若峰 王丹青 胡海峰 任青 罗莹 曹雪 汤宇 郑涵 黎明 薛丽娜 肖青 罗茵 陈瑞微 陈逸群 陈思阳 李林臻 刘勇 周志光 周克涛 廖靖华 马慧霞 胡卫伟 陈藜文 张清华 邓莎 肖晓新 曹昆 罗振炜 钱敏 贵奂 杨诗琪 李雅文 唐行轲 周奕 谭丽 张小林 杨玲 屈鑫燕 蔡明灼 薛槐敏 陈小红 朱翼 潘千 张恒 谢文婷 冯瑾娜 张英 王琳 汪海源 孙燕冉 常小丽 王燕 李朝 李元宵 马丹丹 杜美娜 赵护林 朱寅竹 尹明 徐振方 罗玉婧 何朝燕 张荷 仇国林 冯敏敏 俞丽 李鸾 张林锋 薛珊珊 孙琳 罗敏 武彩霞 高倩 崔璨 王敏 景芳 张雯雯 阮浙临 王晶 肖风迎 冯超 郭霞 李艳茹 阎慧 胡毅秉 郭燕 陈红娟 王玉宁 周宇 周菊 贺楠 刘世晓 李乔 吴思锋 李梓 黄迎春 全娟 彭铮 邓天文 刘嘉佳 陈秀春 陈科生 刘雪映 石璞璞 李青妍 丘素莉 邝计嘉 刘先睿\n" +
                "\n" +
                "3级\n" +
                "\n明明 李子月 徐丹 张怡 向珍 卢颖萍 谢芳芳 史靖文 晁莹 宫昊 冯媛媛 王跃 李青 王国花 杨晓静 蓝静 宋文剑 刘伟 刘德凤 颜廷波 夏焕丽 杨会 韩宁 刘少广 刘兴欢 潘凌 张艳梅 杨兰兰 谭怡宁 李艳 张蕾 徐昕 朱志兴 牛晓渊 李椰潭 王媛媛 刘锦 吴娜 冯伟 郭楚 曹光辉 娄莉莉 宁兴琴 许海明 司丽想 白珊 费晨 李昕萌 鄢蓉 刘倩 康伟 陈芳 吴少杰 李四民 王莹 黄辉 邹昀 史晓明 任莉虹 潘晶燕 胡红枚 刘璇 谢赛 黄思 甘晓茵 刘琬婷 黄剑屏 励黎 王明钊 陈连香 任金凭 曾艳 林芸 谭齐慧 韦明 杨天舒 仇春灵 王璨 翁齐芳 朱燕媚 吴桦 代攀 赖嘉宁 赵晶晶 莫钻红 陈鹏 刘彦妤 余柳婵 何秀珍 林立银 孔朱磊 金果子 黄远飞 黎桂珍 叶冬琳 李晓莉 徐静怡 方春晓 刘政玮 侯点友 冯金妮 潘斯凯 胡文娅 刘晓燕 李亚祎 孙世珍 沈茜 王燕 罗丹 张早 程春华 高原 高媛 徐丽丽 王莹 秦玫 曹利娟 高澜栖 杨怡 蒋美燕 杨雨凡 卿洁 王登菊 赖文文 向芹 向婧宇 李丽 李红梅 朱艳丽 梁云 臧超楠 刘晓寰 任娇 杨春林 余彦 康瑜玲 毛微 胡谦倩 贺若龙 苗雨 朱利华 汤玉荣 尹聪 宋瑞雪 程家文 齐镭 林茜 龙旭 袁声瑜 张华仁 赵静 蔡艳 曹雪琰 车进 于佳 张奥 彭瑾 郑俊杰 李新美 何微微 孔德羲 段丽 段朝思 陈玉筠 刘飞 赵文平 王子云 朱红艳 代瑜 李伟嘉 刘文娟 沈艳丽 杨梦月 李星星 李燕梅 荣徽 唐瑶 王赛 童霏 胡小娟 刘珊宏 刘悦竹 苏天兰 陈蓓 何玉华 冉屹芳 侯倩 张宁 金晖 唐晓莉 黄槟榔 揭廷媛 谷雪阳 况冶 陈莉 罗标雄 盛方圆 王锦霞 罗宗保 魏贝贝 庄金樱 何庆奇 李志刚 刘宇 赵娜 苏畅 林明 孙婧 张倩 孟佳 赵竞 陈衡毅 黄玫玫 李敏 姜雨孜 李璐 毕雅静 刘柳 李娟 闫康 徐波 俞敏东 张慧玲 王希予 唐红菊 徐小婷 邱敏 宋艳艳 李惠英 于惠 李晓婷\n" +
                "\n" +
                "2级口译\n" +
                "\n" +
                "杨熠楠 马知一 李超然 侯晓萃 甘璐妲 罗红 李宛蔓 陈嘉宁 庞兴玉 田晴 杨洁磊 徐凤 杨阳 李天竹 郭金 吴珺 马昱 邹凌云 刘美隆 杨叶 沈家春 蔡丹青 石径 王欢欢 卢家辉 徐非姗 钱一辰 彭逸晨 冯琳 李天童 范潇潇 杨晓健 胡丹 刘欢 丁浩 陈蕾 张喆 胡明慧 曹珉 张珍岚 姚伟达 余少华 陈苏文 宋玉红 区沛仪 黄璨 张翔 梁若莲 许莹 郑静东 马龙 张璨 崔琳 谢丹丹 吴键洪 陈茹 桂莅鑫 朱虹 倪梁靓 王蔚\n" +
                "\n" +
                "3级口译\n" +
                "\n" +
                "刘恩彤 欧丽娜 刘丹芹 潘璐 段宜敏 付雯瑶 陈思伊 陈可 罗兰君 寇昕 赵媛 蔡妮芩 李聪聪 江璐 刘琪琛 孙奕 赵羽 王翰音 胡皓 王君凯 刘斯宁 高敏兰 孟薇 曹皓轩 王雷 周彦楠 王莉 田硕 任黎平 洪子云 王玮 折剑青 胡芝恒 周杰 白桦 张涵 章伟 王晗 刘思思 黄芙蓉 宋阳 肖锋 毛泽尧 徐夕武 张百玲 丁滢 苏笛笛 冯栋 于扬 王波 刘艺星 赵文娜 张舒婷 魏紫瑜 胡楚翘 刘易 王幼萍 张西子 黄琦 胡少莹 崔家骁 孔喆 王鹏 王丽娟 刁文洁 宋晓媛 徐远瑶 喻唯 林旭 李茜 邹媛媛 凌云志 冯晓婕 刘一 丛聪 王珍珠 余正成 单其莹 王文雨 李昕一 周羽姿 周莹 颜婷 高欢 蔡芸菲 宋令怡 李刚 蔡旻杨 韦薇 姜钰 刘馨 尤娟 袁媛 袁伟 叶亮 鲁瑶 杨月粉 张帆 周芳卿 张波 曾佳宁 李晶 彭望琼 黄鹏 赵文隽 张嘉 乔飞 刘颖 颜小玲 姚庭芳 林秀玲 张涵 张依然 刘瑞 李可仪 刘翔宇 何思婷 邹先莹 姚远 彭菲 吴纯英 李韵 林森 蒋喜玲 笪琳琳 汪岑 郑聪 张家旺 钟晶 张倩 朱虹博 毛萃 王竹 王晋 洪禹 周密 郑尧 谭子薇 李晓曦 周雯葭 董超 徐慧 许佳 梅宁琛 望艳妮 韩禹 葛睿洁 陈萌 闫玮婧 王丹萍 郭晓晨 张丽雯 毕建录 郭雨桐 马昆秀 张林锋 袁喆 苗媛媛 乔婧 武婧 孙海燕" +
                "陈迪 肖东菁 汪佳丽 傅双育 王聪 李璇 马舒滟 许青云 徐丹 张奕 田野 彭亚光 殷夏 黄欢 庞琳 张子腾 姜春阳 刘冰若 万菲 何丽 刘龙飞 顾天天 宣苗 周彦楠 朱小玲 毛雅清 江皎 王伊 黄登高 王莉 董润 谭雪 董方帅 黄明光 李凯生 黄乃康 贾玲 陈晓露 刘艳芳 刘丽艳 任黎平 王盼盼 杨欢 张舒婷 杨芳 茹然 许朝亮 邢洋 陆欣楠 刘易 王文霞 刘佳 刁海鹏 杜亚琼 程玲 高川 金鼎 苏旭 李永清 龙慧 柴扉 张琳 董薇 余婧 李亦然 徐歌 孔维源 冯栋 汪潇潇 张文婷 勘晓妹 张依弛 孙榕 廖丽霞 解欣 谭军红 张玲玲 傅姁妮 张润 郝蓓蓓 张青梅 乔亚楠 冯敬宇 黄莎莎 潘佼佼 李欣 龚旻 李丹 陈翔洁 李刚 吴思莹 罗兰君 李森 周莹 严岩 文妮 吕昊 李珏 潘小旋 赵晓敏 闫颖慧 何洁萍 韩香茹 徐婷婷 高彬 孙巍 薄坤坤 刘旭阳 王思敏 张蕾 卜文月 张媛媛 王鹏飞 黄颖 张琼 姚秋月 廉雁捷 周玮 马杰 郭佳 赵淑洁 李琴 谢海莲 刘京 李晓燕 王聪 范亚娟 徐娟 张硕 贾绰 范逊敏 杨丽 单雅丽 谢云生 韩冬 庞书娟 史乃原 孙雅念 李旗挺 李艳婷 马雁翔 尹姗姗 黄鼎 庞哲 朱佳婧 张珊珊 郝思雨 王鑫 马健 叶恒 杨帆 闫雪花 段晓雯 张子晶 郑婷婷 高淑贤 孟佳 黄琦 郭静 余小娟 贾彩娟 陈荣锦 于姝斐 阮盛婧 温洁 李英健 庞晓瑜 方娟 许晓凌 谢娜 张锐 孟竹 李欣 张红玲 张杰 贾翠晓 巩潇然 王琴 杨文田 韩志保 宋燕 张东群 赵爱香 赵雪培 李雪飞 范召琳 楚红梅 魏荣梅 赵晨 尚继鹏 张一珠 申志敏 殷晓婷 李慧芳 贾利利 刘欣 杨剑东 张变英 苏丹 邓罕攀 李焰 郑玉兰 李作健 宁晓蕾 张志诚 刘璐 李军林 陈敏杰 梁峰 肖蕾 李维奇 喻唯 王博 袁丹 曲芳 武妹 宫素香 刁文洁 王雪 范佳慧 周芸 刘婷婷 盛文凤 王雪 崔丹 刘颖 王玉洁 姜葳 董英灏 王侃 刘琨 冯超 宁升椿 陈光琦 陈晓晨 郭欣 李杨 徐冬蕾 王华梅 董达勇 闫卓琳 杨靖 刘丽丽 杨云 孙宇 刘晶 王永佳 徐铭潞 高群 刘雅真 曾祥玲 刘珊珊 赵鑫 解丽娟 刘建伟 陈珂瑾 周玲玲 宋宾 杨思 魏双艳 刘永杰 潘煜晨 罗宏达 凌云志 张文琳 向可嘉 卜臻敏 王文 陈佳敏 周颖雯 袁沁茹 宋明星 杨季赟 王颖群 周娟 熊辉 陆轶文 崔明华 颜婷 袁飞 崔映芬 赵天湖 徐磊 毕凯 常静娜 李凤 张茜 张晶晶 陆兰兰 俞旸 张婷婷 李昕一 李娜 叶紫薇 傅琦 李妍 谢微微 冬妮 刘羽扬 周悠悠 樊周依 武文嘉 徐珺 盛珺 刘嵩 梁艳 于敏 韩婧 黄彩萍 王伟鹏 曹兵 李贞 吴晓成 江松洁 鲁玉萍 周淑枫 张曼曼 张瑞娟 戚银燕 陈芸 顾鸿兰 毛玲燕 徐梦灵 陈铮 仲娜娜 王苑苑 陈思远 林倩倩 何培芬 步雅芸 皮金燕 余姗姗 何兰兰 范琴英 陈丹丽 唐奇 崔业萍 王二为 王莉淳 孙云云 袁嫚玲 杨颜 郭舒静 沈雨露 王立冕 周灿金 陈舒斌 朱晓妤 郑雅颖 张力甦 游成敏 王云玲 阮薇 曾培中 李桢 林芳 周倩 余英姿 黎菁菁";

        String[] list = str.split(" ");
        System.out.println(list.length);
//        String[] list = new String[]{"张三", "张衫", "张散", "张丹", "张成", "李四", "李奎"};
        Search d = new Search(list);
        System.out.println(d.search("张三", 10));
        System.out.println(d.search("张帆", 10));
        System.out.println(d.search("罗兰君", 10));
    }


    public List<Score> search(String input, int limit) throws PinyinException {
        Word w = new Word(input);

        TreeSet<Score> set = new TreeSet<Score>();
        for (Word x : targets) {
            Score s = new Score();
            s.word = x;
            s.score = x.compareTo(w);
            set.add(s);
        }

        ArrayList<Score> list = new ArrayList<Score>();
        for (Score s : set) {
            if (list.size() >= limit) {
                break;
            }
            list.add(s);
        }
        return list;

//        return targets.stream().map(x -> {
//            Score s = new Score();
//            s.word = x;
//            s.score = x.compareTo(w);
//            return s;
//        }).sorted().limit(limit).collect(Collectors.toList());
    }


    public static int getEditDistance(String s, String t) {
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost
        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];

        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt(i - 1);
            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                // Step 5
                cost = (s_i == t_j) ? 0 : 1;
                // Step 6
                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
                        d[i - 1][j - 1] + cost);
            }
        }
        // Step 7
        return d[n][m];
    }

    private static int Minimum(int a, int b, int c) {
        int im = a < b ? a : b;
        return im < c ? im : c;
    }

    class Word implements Comparable {
        final String word;
        final String pinyin1;
        final String pinyin2;

        Word(String word) throws PinyinException {
            this.word = word;
            this.pinyin1 = PinyinHelper.convertToPinyinString(word, ",", PinyinFormat.WITH_TONE_NUMBER);
            this.pinyin2 = PinyinHelper.convertToPinyinString(word, ",", PinyinFormat.WITHOUT_TONE);
        }

        @Override
        public String toString() {
            return word;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Word) {
                Word o1 = (Word) o;
                int score1 = getEditDistance(this.pinyin1, o1.pinyin1);
                int score2 = getEditDistance(this.pinyin2, o1.pinyin2);
                return score1 + score2;
            }
            return 0;
        }
    }

    class Score implements Comparable {
        Word word;
        int score;

        @Override
        public int compareTo(Object o) {
            if (o instanceof Score) {
                return score - ((Score) o).score;
            }
            return 0;
        }

        @Override
        public String toString() {
            return "{" +
                    "word=" + word +
                    ", score=" + score +
                    '}';
        }
    }
}
