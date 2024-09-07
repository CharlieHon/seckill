package com.charlie.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charlie.seckill.mapper.SeckillGoodsMapper;
import com.charlie.seckill.pojo.SeckillGoods;
import com.charlie.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;

@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
        implements SeckillGoodsService {
}
