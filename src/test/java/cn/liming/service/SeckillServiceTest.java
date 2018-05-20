package cn.liming.service;

import cn.liming.dto.Exposer;
import cn.liming.dto.SeckillExecution;
import cn.liming.entity.Seckill;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author liming
 * @date 2018/5/16   10:19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"
        , "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list = seckillService.getSeckillList();
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillService.getById(1000);
        Assert.assertEquals(99, seckill.getNumber());
    }

    @Test
    public void exportSeckillUrl() throws Exception {
        Exposer exposer = seckillService.exportSeckillUrl(1001);
        System.out.println(exposer.getMd5());
    }

    @Test
    public void executeSeckill() throws Exception {
        Exposer exposer = seckillService.exportSeckillUrl(1000);
        SeckillExecution seckillExecution = seckillService.executeSeckill(1000, 15207114730L, exposer.getMd5());
        System.out.println(seckillExecution.getStateInfo());
    }

    @Test
    public void executeSeckillProcedure() {
        Exposer exposer = seckillService.exportSeckillUrl(1001);
        SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(1001, 15207114731L, exposer.getMd5());
        System.out.println(seckillExecution.getStateInfo());
    }

}