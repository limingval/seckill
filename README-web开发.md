# Java高并发秒杀系统API(三)之Web层开发
本篇文章总结自己开发秒杀系统Web层的过程，主要介绍前端交互设计、Restful:url满足Restful设计规范、Spring MVC、bootstrap+jquery这四个方面的开发。
## 1.前端交互流程设计

对于一个系统，需要产品经理、前端工程师和后端工程师的参数，产品经理将用户的需求做成一个开发文档交给前端工程师和后端工程师，前端工程师为系统完成页面的开发，后端工程师为系统完成业务逻辑的开发。对于我们这个秒杀系统，它的前端交互流程设计如下图:

![image](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-28%20%E4%B8%8B%E5%8D%886.13.55.png)


这个流程图就告诉了我们详情页的流程逻辑，前端工程师根据这个流程图设计页面，而我们后端工程师根据这个流程图开发我们对应的代码。前端交互流程是系统开发中很重要的一部分，接下来进行Restful接口设计的学习。
这个流程图就告诉了我们详情页的流程逻辑，前端工程师根据这个流程图设计页面，而我们后端工程师根据这个流程图开发我们对应的代码。前端交互流程是系统开发中很重要的一部分，接下来进行Restful接口设计的学习。

## 2.Restful接口设计学习

什么是Restful?它就是一种优雅的URI表述方式，用来设计我们资源的访问URL。通过这个URL的设计，我们就可以很自然的感知到这个URL代表的是哪种业务场景或者什么样的数据或资源。基于Restful设计的URL，对于我们接口的使用者、前端、web系统或者搜索引擎甚至是我们的用户，都是非常友好的。关于Restful的了解大家去网上一搜一大把，我这里就不再做介绍了。下面看看我们这个秒杀系统的URL设计:
![image](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-28%20%E4%B8%8B%E5%8D%886.49.47.png)
接下来基于上述资源接口来开始我们对Spring MVC框架的使用。

## 3.整合配置Spring MVC框架

首先在WEB-INF的web.xml中进行我们前端控制器DispatcherServlet的配置，如下:

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                      http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0"
         metadata-complete="true">
<!--用maven创建的web-app需要修改servlet的版本为3.1-->
<!--配置DispatcherServlet-->
    <servlet>
        <servlet-name>seckill-dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!--
            配置SpringMVC 需要配置的文件
            spring-dao.xml，spring-service.xml,spring-web.xml
            Mybites -> spring -> springMvc
        -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:spring/spring-*.xml</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>seckill-dispatcher</servlet-name>
        <!--默认匹配所有请求-->
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>

然后在spring容器中进行web层相关bean(即Controller)的配置，在spring包下创建一个spring-web.xml，内容如下:

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context 
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/mvc 
        http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <!--配置spring mvc-->
    <!--1,开启springmvc注解模式
    a.自动注册DefaultAnnotationHandlerMapping,AnnotationMethodHandlerAdapter
    b.默认提供一系列的功能:数据绑定，数字和日期的format@NumberFormat,@DateTimeFormat
    c:xml,json的默认读写支持-->
    <mvc:annotation-driven/>

    <!--2.静态资源默认servlet配置-->
    <!--
        1).加入对静态资源处理：js,gif,png
        2).允许使用 "/" 做整体映射
    -->
    <mvc:default-servlet-handler/>

    <!--3：配置JSP 显示ViewResolver-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>

    <!--4:扫描web相关的bean-->
    <context:component-scan base-package="cn.codingxiaxw.web"/>
</beans>

这样我们便完成了Spring MVC的相关配置(即将Spring MVC框架整合到了我们的项目中)，接下来就要基于Restful接口进行我们项目的Controller开发工作了。

## 4.Controller开发
Controller中的每一个方法都对应我们系统中的一个资源URL，其设计应该遵循Restful接口的设计风格。在cn.codingxiaxw包下创建一个web包用于放web层Controller开发的代码，在该包下创建一个SeckillController.java，内容如下:
@Component
@RequestMapping("/seckill")//url:模块/资源/{}/细分
public class SeckillController
{
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model)
    {
        //list.jsp+mode=ModelAndView
        //获取列表页
        List<Seckill> list=seckillService.getSeckillList();
        model.addAttribute("list",list);
        return "list";
    }

    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model)
    {
        if (seckillId == null)
        {
            return "redirect:/seckill/list";
        }

        Seckill seckill=seckillService.getById(seckillId);
        if (seckill==null)
        {
            return "forward:/seckill/list";
        }

        model.addAttribute("seckill",seckill);

        return "detail";
    }

    //ajax ,json暴露秒杀接口的方法
    @RequestMapping(value = "/{seckillId}/exposer",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(Long seckillId)
    {
        SeckillResult<Exposer> result;
        try{
            Exposer exposer=seckillService.exportSeckillUrl(seckillId);
            result=new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e)
        {
            e.printStackTrace();
            result=new SeckillResult<Exposer>(false,e.getMessage());
        }

        return result;
    }
    
     @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "killPhone",required = false) Long phone)
    {
        if (phone==null)
        {
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }
        SeckillResult<SeckillExecution> result;

        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch (RepeatKillException e1)
        {
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(false,execution);
        }catch (SeckillCloseException e2)
        {
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(false,execution);
        }
        catch (Exception e)
        {
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(false,execution);
        }

    }

    //获取系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    public SeckillResult<Long> time()
    {
        Date now=new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }
}

Controller开发中的方法完全是对照Service接口方法进行开发的，第一个方法用于访问我们商品的列表页，第二个方法访问商品的详情页，第三个方法用于返回一个json数据，数据中封装了我们商品的秒杀地址，第四个方法用于封装用户是否秒杀成功的信息，第五个方法用于返回系统当前时间。代码中涉及到一个将返回秒杀商品地址封装为json数据的一个Vo类，即SeckillResult.java，在dto包中创建它，内容如下:

//将所有的ajax请求返回类型，全部封装成json数据
public class SeckillResult<T> {

    private boolean success;
    private T data;
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

## 5.页面开发
页面由前端工程师完成，这里直接拷贝我github上源代码中jsp的代码(webapp包下的所有资源)即可。
然后运行Tomcat服务器,在浏览器中输入http://localhost:8080/seckill/list，即可访问我们的秒杀列表页面:
:
![image](http://od2xrf8gr.bkt.clouddn.com/%E5%B1%8F%E5%B9%95%E5%BF%AB%E7%85%A7%202016-11-28%20%E4%B8%8B%E5%8D%889.14.36.png)

点击相应商品后面的详情页链接即可查看该商品是否开启秒杀、秒杀该商品等活动。到此，web层的开发也结束，我们的系统开发便告一段落。但往往这样一个秒杀系统，往往是会有成千上万的人进行参与，我们目前的系统是抗不起多少高并发操作的，所以后面我们会对本系统进行高并发的优化。请查看我的下篇文章java高并发秒杀API之高并发优化


