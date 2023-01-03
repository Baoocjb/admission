# admission
数据库课程设计-平行志愿模拟录取系统

# 关于项目

### 项目简介

该项目是基于SSM实现的高考平行志愿模拟录取系统，题目来源：广东工业大学-数据库系统 (谢锐老师) -2022年课程设计。



### 项目技术栈

**前端主要涉及技术**

- 前端三件套 - Html, Css, JavaScript
- Web端展示组件 - ElementUI
- 录取结果可视化展示 - echarts

**后端主要涉及技术**

- Java 开发框架 - Springboot + SpringMVC + Spring
- 持久层框架 - MyBatis-plus
- 持久层 - MySQL
- 依赖管理 - Maven
- 对象低代码组件 - Lombok
- Excel表管理组件 - Easyexcel
- 接口文档工具 - Knife4j



### 项目运行

#### 运行环境

windows 10/11

IDEA 2019 + JDK 1.8 + MySQL 8

#### 项目运行说明

1. 创建数据库名为 : `admission`

2. 将项目源文件 `/resources/startResource` 下的 `admission.sql` 文件导入数据库

3. 更改 `/resources` 下的 `application.yml` 中的以下内容

   ```
   datasource:
   username: MySQL用户名
   password: MySQL密码
   url: jdbc:mysql://localhost:3306/admission?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
   ```

4. 启动项目, 访问浏览器访问 localhost:8080/index.html



#### 项目附件说明

平行志愿考生测试数据.xlsx : 用于测试的广东省6800余名考生志愿信息

招生计划测试数据.xlsx : 用于测试的某大学各专业招生计划

课设选题.pdf : 题目说明

