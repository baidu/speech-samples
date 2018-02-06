## 识别结果纠错示例

Search.java是一个简单纠错演示demo


### 适用场景
1. 文本纠错
2. 结果打分

### 使用
1. 拷贝Search.java这个类到您的工程，非java环境的朋友可以跟进这个类翻译成您的语言
2. 调用演示
Search engine = Search(["", "", ...]); // 通过一个“正确”结果的字符串数组构造纠错引擎
List<Score> nbest = engine.search("张三", 10); // 输入待纠错的文本“张三”返回纠错后的结果，结果会按照相似进行排序。

### 依赖

maven, pom.xml中添加
```
        <dependency>
            <groupId>com.github.stuxuhai</groupId>
            <artifactId>jpinyin</artifactId>
            <version>1.1.8</version>
        </dependency>
```

gradle或其它框架，请根据maven的配置自行处理
