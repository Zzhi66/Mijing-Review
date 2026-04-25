package com.mj.mijing.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.mijing.dto.Result;
import com.mj.mijing.entity.Shop;
import com.mj.mijing.mapper.ShopMapper;
import com.mj.mijing.service.ShopService;
import com.mj.mijing.utils.CacheClient;
import com.mj.mijing.utils.RedisConstants;
import com.mj.mijing.utils.SystemConstants;
import com.mj.mijing.utils.TwoLevelCacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商铺 Service 实现
 * 特性：Caffeine+Redis 二级缓存、缓存穿透/击穿保护、Redis GEO 附近商铺
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private TwoLevelCacheClient twoLevelCacheClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 优先使用二级缓存（Caffeine + Redis），缓存穿透保护
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        Shop shop = twoLevelCacheClient.get(key, Shop.class,
                k -> getById(id),
                RedisConstants.CACHE_SHOP_TTL);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("商铺 ID 不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 双删：Caffeine + Redis（保证最终一致性）
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        twoLevelCacheClient.invalidate(key);
        log.info("商铺缓存双删：{}", key);
        return Result.ok();
    }

    /**
     * GeoHash 空间检索：附近商铺
     * 使用 Redis GEOSEARCH 查询指定坐标附近 3km 内的商铺，按距离排序分页
     */
    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 无坐标则直接分页查DB
        if (x == null || y == null) {
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }

        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String geoKey = RedisConstants.SHOP_GEO_KEY + typeId;

        // Redis GEO 范围查询（附近3公里，返回距离）
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                stringRedisTemplate.opsForGeo().search(
                        geoKey,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(SystemConstants.DEFAULT_GEO_RADIUS_KM, Metrics.KILOMETERS),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                                .includeDistance()
                                .limit(end)
                );

        if (results == null) {
            return Result.ok(Collections.emptyList());
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();
        if (content.size() <= from) {
            return Result.ok(Collections.emptyList());
        }

        // 截取分页部分
        List<Long> ids = new ArrayList<>();
        Map<String, Distance> distanceMap = new HashMap<>();
        content.stream().skip(from).forEach(result -> {
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            distanceMap.put(shopIdStr, result.getDistance());
        });

        // 按 GEO 结果顺序查商铺
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Shop> shops = query()
                .in("id", ids)
                .last("ORDER BY FIELD(id," + idStr + ")")
                .list();
        shops.forEach(shop ->
                shop.setDistance(distanceMap.get(String.valueOf(shop.getId()))
                        .getValue()));
        return Result.ok(shops);
    }
}
