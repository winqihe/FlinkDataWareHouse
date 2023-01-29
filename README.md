# Flink实时数据项目

#### 介绍
这个项目是基于Flink+Kafka的一个实时的数据流系统，基于Flink流数仓的趋势，进行数仓建模分层处理，数据是一些mock的数据，分为两类：日志和业务数据。

#### 项目说明

- 数据采集说明：

日志数据采集：使用了模拟生成数据的jar包，将日志发送到一个指定的端口，这中间使用了nginx，做负载均衡
通过SpringBoot整合Kafka来使用，将数据传输到Kafka的topic中

业务数据采集：将建表数据导入，生成一系列的表，注意这里使用FlinkCDC进行采集，在Mysql的my.cnf(面试被问到)中配置binlog，使用模拟数据的jar包和properties，生成数据。在Mysql中查看数据

- 项目分层说明：

ODS：原始数据，日志和业务数据 
DWD：根据数据对象为单位进行分流，比如订单、页面访问等等 
DIM：维度数据 
DWM：对于部分数据对象进行进一步加工，比如独立访问、跳出行为，也可以和维度 进行关联，形成宽表，依旧是明细数据。
DWS：根据某个主题将多个事实数据轻度聚合，形成主题宽表。 
ADS：把ClickHouse中的数据根据可视化需进行筛选聚合

#### 软件架构
软件架构说明

**存储：**
Kafka
HBase(Phoenix)
HDFS
Redis

**计算：**

Flink

**调度分配**：

YARN
Zookeeper
**分析：**
ClickHouse

#### 安装教程
下载导入IDEA即可，但是大数据的相关使用环境需要自己手动搭建，搭建可以参考一些文章，减少出错率。

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
