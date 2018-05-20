package cn.liming.service;

import cn.liming.dto.Exposer;
import cn.liming.dto.SeckillExecution;
import cn.liming.entity.Seckill;
import cn.liming.exception.RepeatKillException;
import cn.liming.exception.SeckillCloseException;
import cn.liming.exception.SeckillException;

import java.util.List;

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
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException,RepeatKillException,SeckillCloseException;

    SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5);
}
