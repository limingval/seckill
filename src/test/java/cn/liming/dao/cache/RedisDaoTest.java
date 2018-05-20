package cn.liming.dao.cache;

import cn.liming.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author liming
 * @date 2018/5/19   20:51
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    @Autowired
    private  RedisDao redisDao;
    @Test
    public void getSeckill() throws Exception {
        Seckill seckill = redisDao.getSeckill(123);
        System.out.println(seckill);

    }

    @Test
    public void putSeckill() throws Exception {
        Seckill seckill =  new Seckill();
        seckill.setCreateTime(new Date());
        seckill.setEndTime(new Date());
        seckill.setName("手机");
        seckill.setNumber(100);
        seckill.setSeckillId(123);
        String str = redisDao.putSeckill(seckill);
        System.out.println(str);
    }

}