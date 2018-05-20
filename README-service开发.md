
# Java高并发秒杀系统API(二)之Service层开发
开始Service层的编码之前，我们首先需要进行Dao层编码之后的思考:在Dao层我们只完成了针对表的相关操作包括写了接口方法和映射文件中的sql语句，并没有编写逻辑的代码，例如对多个Dao层方法的拼接，当我们用户成功秒杀商品时我们需要进行商品的减库存操作(调用SeckillDao接口)和增加用户明细(调用SuccessKilledDao接口)，这些逻辑我们都需要在Service层完成。这也是一些初学者容易出现的错误，他们喜欢在Dao层进行逻辑的编写，其实Dao就是数据访问的缩写，它只进行数据的访问操作，接下来我们便进行Service层代码的编写。
## 1.秒杀Service接口设计
在cn.codingxiaxw包下创建一个service包用于存放我们的Service接口和其实现类，创建一个exception包用于存放service层出现的异常例如重复秒杀商品异常、秒杀已关闭等异常，一个dto包作为传输层,dto和entity的区别在于:entity用于业务数据的封装，而dto用于完成web和service层的数据传递。
首先创建我们Service接口，里面的方法应该是按”使用者”(程序员)的角度去设计，SeckillService.java，代码如下:
public interface SeckillService {

    /**
     * 查询全部的秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     *查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);


    //再往下，是我们最重要的行为的一些接口

    /**
     * 在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);


    /**
     * 执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckill(long seckillId,long userPhone,String md5)
            throws SeckillException,RepeatKillException,SeckillCloseException;
}

该接口中前面两个方法返回的都是跟我们业务相关的对象，而后两个方法返回的对象与业务不相关，这两个对象我们用于封装service和web层传递的数据，方法的作用我们已在注释中给出。相应在的dto包中创建Exposer.java，用于封装秒杀的地址信息，各个属性的作用在代码中已给出注释，代码如下:

/**
 * 暴露秒杀地址(接口)DTO
 */
public class Exposer {
    
    //是否开启秒杀
    private boolean exposed;

    //对秒杀地址加密措施
    private String md5;

	//id为seckillId的商品的秒杀地址
    private long seckillId;

    //系统当前时间(毫秒)
    private long now;

    //秒杀的开启时间
    private long start;

    //秒杀的结束时间
    private long end;

    public Exposer(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public Exposer(boolean exposed, long seckillId,long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId=seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public Exposer(boolean exposed, long seckillId) {
        this.exposed = exposed;
        this.seckillId = seckillId;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}

和SeckillExecution.java，用于判断秒杀是否成功，成功就返回秒杀成功的所有信息(包括秒杀的商品id、秒杀成功状态、成功信息、用户明细)，失败就抛出一个我们允许的异常(重复秒杀异常、秒杀结束异常),代码如下:

/**
 * 封装执行秒杀后的结果:是否秒杀成功

 */
public class SeckillExecution {

    private long seckillId;

    //秒杀执行结果的状态
    private int state;

    //状态的明文标识
    private String stateInfo;

    //当秒杀成功时，需要传递秒杀成功的对象回去
    private SuccessKilled successKilled;

    //秒杀成功返回所有信息
    public SeckillExecution(long seckillId, int state, String stateInfo, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = state;
        this.stateInfo = stateInfo;
        this.successKilled = successKilled;
    }

    //秒杀失败
    public SeckillExecution(long seckillId, int state, String stateInfo) {
        this.seckillId = seckillId;
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }
}

然后需要创建我们在秒杀业务过程中允许的异常，重复秒杀异常RepeatKillException.java:

/**
 * 重复秒杀异常，是一个运行期异常，不需要我们手动try catch
 * Mysql只支持运行期异常的回滚操作

 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}

秒杀关闭异常SeckillCloseException.java:

/**
 * 秒杀关闭异常，当秒杀结束时用户还要进行秒杀就会出现这个异常

 */
public class SeckillCloseException extends SeckillException{
    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}

和一个异常包含与秒杀业务所有出现的异常SeckillException.java:

/**
 * 秒杀相关的所有业务异常
 */
public class SeckillException extends RuntimeException {
    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}

到此，接口的工作便完成，接下来进行接口实现类的编码工作。

## 2.秒杀Service接口的实现

在service包下创建impl包存放它的实现类，SeckillServiceImpl.java，内容如下:

public class SeckillServiceImpl implements SeckillService
{
    //日志对象
    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //加入一个混淆字符串(秒杀接口)的salt，为了我避免用户猜出我们的md5值，值任意给，越复杂越好
    private final String salt="shsdssljdd'l.";

    //注入Service依赖
    @Autowired //@Resource
    private SeckillDao seckillDao;

    @Autowired //@Resource
    private SuccessKilledDao successKilledDao;

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill=seckillDao.queryById(seckillId);
        if (seckill==null) //说明查不到这个秒杀产品的记录
        {
            return new Exposer(false,seckillId);
        }

        //若是秒杀未开启
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        //系统当前时间
        Date nowTime=new Date();
        if (startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime())
        {
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        //秒杀开启，返回秒杀商品的id、用给接口加密的md5
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId)
    {
        String base=seckillId+"/"+salt;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    //秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {

        if (md5==null||!md5.equals(getMD5(seckillId)))
        {
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime=new Date();

        try{
            //减库存
            int updateCount=seckillDao.reduceNumber(seckillId,nowTime);
            if (updateCount<=0)
            {
                //没有更新库存记录，说明秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //否则更新了库存，秒杀成功,增加明细
                int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
                //看是否该明细被重复插入，即用户是否重复秒杀
                if (insertCount<=0)
                {
                    throw new RepeatKillException("seckill repeated");
                }else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,1,"秒杀成功",successKilled);
                }
            }

        }catch (SeckillCloseException e1)
        {
            throw e1;
        }catch (RepeatKillException e2)
        {
            throw e2;
        }catch (Exception e)
        {
            logger.error(e.getMessage(),e);
            //所以编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }

    }
}

对上述代码进行分析一下，在return new SeckillExecution(seckillId,1,"秒杀成功",successKilled);代码中，我们返回的state和stateInfo参数信息应该是输出给前端的，但是我们不想在我们的return代码中硬编码这两个参数，所以我们应该考虑用枚举的方式将这些常量封装起来，在cn.codingxiaxw包下新建一个枚举包enums，创建一个枚举类型SeckillStatEnum.java，内容如下:

public enum SeckillStatEnum {

    SUCCESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATE_REWRITE(-3,"数据篡改");

    private int state;
    private String info;

    SeckillStatEnum(int state, String info) {
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }


    public String getInfo() {
        return info;
    }


    public static SeckillStatEnum stateOf(int index)
    {
        for (SeckillStatEnum state : values())
        {
            if (state.getState()==index)
            {
                return state;
            }
        }
        return null;
    }
}

然后修改执行秒杀操作的非业务类SeckillExecution.java里面涉及到state和stateInfo参数的构造方法:

//秒杀成功返回所有信息
 public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SuccessKilled successKilled) {
     this.seckillId = seckillId;
     this.state = statEnum.getState();
     this.stateInfo = statEnum.getInfo();
     this.successKilled = successKilled;
 }

 //秒杀失败
 public SeckillExecution(long seckillId, SeckillStatEnum statEnum) {
     this.seckillId = seckillId;
     this.state = statEnum.getState();
     this.stateInfo = statEnum.getInfo();
 }

然后便可修改实现类方法中的返回语句为:return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,successKilled);，保证了一些常用常量数据被封装在枚举类型里。
目前为止我们Service的实现全部完成，接下来要将Service交给Spring的容器托管，进行一些配置。

## 3.使用Spring托管Service依赖配置
在spring包下创建一个spring-service.xml文件，内容如下

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd">

    <!--扫描service包下所有使用注解的类型-->
    <context:component-scan base-package="cn.codingxiaxw.service"/>
    
</beans>

然后采用注解的方式将Service的实现类加入到Spring IOC容器中:

//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService

## 4.使用Spring声明式事务配置

声明式事务的使用方式:1.早期使用的方式:ProxyFactoryBean+XMl.2.tx:advice+aop命名空间，这种配置的好处就是一次配置永久生效。3.注解@Transactional的方式。在实际开发中，建议使用第三种对我们的事务进行控制，优点见下面代码中的注释。下面让我们来配置声明式事务，在spring-service.xml中添加对事务的配置:
!--配置事务管理器-->
  <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
      <!--注入数据库连接池-->
      <property name="dataSource" ref="dataSource"/>

  </bean>

  <!--配置基于注解的声明式事务
  默认使用注解来管理事务行为-->
  <tx:annotation-driven transaction-manager="transactionManager"/>

然后在Service实现类的方法中，在需要进行事务声明的方法上加上事务的注解:

//秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
   @Transactional
   /**
    * 使用注解控制事务方法的优点:
    * 1.开发团队达成一致约定，明确标注事务方法的编程风格
    * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
    * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
    */
   public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
           throws SeckillException, RepeatKillException, SeckillCloseException {}

下面针对我们之前做的业务实现类来做集成测试。

## 5.使用集成测试Service逻辑

在SeckillService接口中使用IDEA快捷键shift+command+T，快速生成junit测试类。Service实现类中前面两个方法很好实现，获取列表或者列表中的一个商品的信息即可，测试如下:

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml",
                        "classpath:spring/spring-service.xml"})

public class SeckillServiceTest {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckills=seckillService.getSeckillList();
        System.out.println(seckills);

    }

    @Test
    public void getById() throws Exception {

        long seckillId=1000;
        Seckill seckill=seckillService.getById(seckillId);
        System.out.println(seckill);
    }
}

重点就是exportSeckillUrl()方法和executeSeckill()方法的测试，接下来我们进行exportSeckillUrl()方法的测试，如下:

@Test
   public void exportSeckillUrl() throws Exception {

       long seckillId=1000;
       Exposer exposer=seckillService.exportSeckillUrl(seckillId);
       System.out.println(exposer);

   }

控制台中输入如下信息:

Exposer{exposed=false, md5='null', seckillId=1000, now=1480322072410, start=1451577600000, end=1451664000000}

证明电话为13476191876的用户成功秒杀到了该商品，查看数据库，该用户秒杀商品的明细信息已经被插入明细表，说明我们的业务逻辑没有问题。但其实这样写测试方法还有点问题，此时再次执行该方法，控制台报错，因为用户重复秒杀了。我们应该在该测试方法中添加try catch,将程序允许的异常包起来而不去向上抛给junit，更改测试代码如下:

@Test
 public void executeSeckill() throws Exception {

     long seckillId=1000;
     long userPhone=13476191876L;
     String md5="bf204e2683e7452aa7db1a50b5713bae";

     try {
         SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);

         System.out.println(seckillExecution);
     }catch (RepeatKillException e)
     {
         e.printStackTrace();
     }catch (SeckillCloseException e1)
     {
         e1.printStackTrace();
     }
 }

这样再测试该方法，junit便不会再在控制台中报错，而是认为这是我们系统允许出现的异常。由上分析可知，第四个方法只有拿到了第三个方法暴露的秒杀商品的地址后才能进行测试，也就是说只有在第三个方法运行后才能运行测试第四个方法，而实际开发中我们不是这样的，需要将第三个测试方法和第四个方法合并到一个方法从而组成一个完整的逻辑流程:

@Test//完整逻辑代码测试，注意可重复执行
    public void testSeckillLogic() throws Exception {
        long seckillId=1000;
        Exposer exposer=seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed())
        {

            System.out.println(exposer);

            long userPhone=13476191876L;
            String md5=exposer.getMd5();

            try {
                SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
                System.out.println(seckillExecution);
            }catch (RepeatKillException e)
            {
                e.printStackTrace();
            }catch (SeckillCloseException e1)
            {
                e1.printStackTrace();
            }
        }else {
            //秒杀未开启
            System.out.println(exposer);
        }
    }
运行该测试类，控制台成功输出信息，库存会减少，明细表也会增加内容。重复执行，控制台不会报错，只是会抛出一个允许的重复秒杀异常。
目前为止，Dao层和Service层的集成测试我们都已经完成，接下来进行Web层的开发编码工作，请查看我的下篇文章java高并发秒杀API之Web层开发。