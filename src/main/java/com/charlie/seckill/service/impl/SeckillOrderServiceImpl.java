package com.charlie.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charlie.seckill.mapper.SeckillOrderMapper;
import com.charlie.seckill.pojo.SeckillOrder;
import com.charlie.seckill.service.SeckillOrderService;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {

}
