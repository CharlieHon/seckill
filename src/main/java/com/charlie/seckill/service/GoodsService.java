package com.charlie.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.charlie.seckill.pojo.Goods;
import com.charlie.seckill.vo.GoodsVo;

import java.util.List;

public interface GoodsService extends IService<Goods> {

    // 秒杀商品列表
    List<GoodsVo> findGoodsVo();

    // 根据商品id-获取商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
