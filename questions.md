## Questions

### 1. maven打包加入本地lib

参考文章：https://blog.csdn.net/csdn2193714269/article/details/78391274亲测可行，注意几点问题：

- 需要打包的本地lib为支付宝沙箱环境支付的jar包，下载后将对应的jar包放入到src/main/resources/lib下

- maven3.1之后<compilerArguments>标签改成 <compilerArgs>标签，项目使用Maven3.5，注意版本兼容问题
- 项目中使用另外一种打包方式：

```xml
<!--
	Maven打包本地lib jar
    参考网址：https://blog.csdn.net/csdn2193714269/article/details/78391274
-->
<dependency>
    <groupId>com.alipay</groupId>
    <artifactId>com-alipay</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/resources/lib/alipay-sdk-
        								java20161213173952.jar</systemPath>
</dependency>

<dependency>
    <groupId>com.alipay.demo</groupId>
    <artifactId>com-alipay-demo</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/src/main/resources/lib/alipay-trade-sdk-
        								20161215.jar</systemPath>
</dependency>
```

- 使用：mvn clean package install -Dmaven.test.skip=true测试是否通过



### 2. Cannot find .../setclasspath.sh

尝试在对应的Tomcat节点下执行对应的命令：

- unset CATALINA_HOME
- unset CATALINA_BASE
- unset CATALINA_2_HOME
- usset CATALINA_2_BASE



### 3. IDEA：Executing Maven Goal执行时间过长

在Settings -> Maven -> Runner中：取消勾选：Delegate IDE build/run actions to run



### 4. "An invalid domain [.sherman.com] was specified for this cookie"

- Tomcat8.5以前的版本中，domain格式是：".sherman.com"，前面需要加.
- Tomcat8.5及其以后的版本中，domain格式是："sherman.com"，前面不需要加.



### 5. Postman中login.do测试成功后浏览器中找不到Cookie，get_user_info.do显示用户未登录

用Postman进行测试是因为两个请求都是POST请求，在浏览器中不用表单或者其它插件（Geely视频中使用的是浏览器插件，在一个Session中）无法完成，但是Postman的请求就相当于一个新的浏览器，也就是说**Postman和浏览器是在两个不同的Session中**，因此在浏览器中找不到对应的Cookie。
解决方案：为了测试方便，可以将两个请求使用GET请求方式提交，就能够看到Cookie。



### 6. Could not get a resource since the pool is exhausted

注意服务器需要开放对应redis端口，阿里云配置对应端口安全组，protected-mode no以及各个redis节点不要开启主从复制、sentinel、集群模式。



