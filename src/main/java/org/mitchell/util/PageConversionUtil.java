package com.polarizon.cdb.iot.common.utils;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Description mybatis-plus Page 和 Spring Page 互相转换的工具类
 * @create 2024-02-29 11:29
 */
public class PageConversionUtil {

    /**
     * 将Spring Data的Pageable转换为MyBatis-Plus的Page
     *
     * @param springPageable Spring Pageable类
     * @return mybatis-plus Page类
     */
    public static <T> Page<T> toMyBatisPlusPage(Pageable springPageable) {
        if (springPageable == null) {
            return new Page<>();
        }

        Page<T> page = new Page<>(springPageable.getPageNumber() + 1, springPageable.getPageSize());

        // 转换排序信息，区分ASC和DESC
        if (springPageable.getSort().isSorted()) {
            List<OrderItem> orderItems = new ArrayList<>();
            springPageable.getSort().forEach(order -> {
                // 根据排序方向决定使用asc还是desc
                if (order.getDirection() == Sort.Direction.ASC) {
                    orderItems.add(OrderItem.asc(order.getProperty()));
                } else {
                    orderItems.add(OrderItem.desc(order.getProperty()));
                }
            });
            page.addOrder(orderItems); // 添加排序条件到Page对象
        }

        return page;
    }

    /**
     * （需要外部传入Pageable对象）将 MyBatis-Plus 的 Page 转换为 Spring Data 的 PageImpl
     * 注意，这里需要传入Pageable，用来保留原始 Spring Pageable 的元数据
     *
     * @param mybatisPlusPage mybatis-plus Page类
     * @param springPageable  原始 Spring Pageable类
     * @return Spring Page
     */
    public static <T> org.springframework.data.domain.Page<T> toSpringDataPage(Page<T> mybatisPlusPage, Pageable springPageable) {
        if (mybatisPlusPage == null) {
            return new PageImpl<>(Collections.emptyList(), springPageable, 0);
        }
        return new PageImpl<>(mybatisPlusPage.getRecords(), springPageable, mybatisPlusPage.getTotal());
    }

    /**
     * （在方法内部构造Pageable对象）将 MyBatis-Plus 的 Page 转换为 Spring Data 的 PageImpl
     *
     * @param mybatisPlusPage mybatis-plus Page类
     * @return Spring Page
     */
    public static <T> org.springframework.data.domain.Page<T> toSpringDataPage(Page<T> mybatisPlusPage) {
        if (mybatisPlusPage == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        // 默认无排序
        Sort sort = Sort.unsorted();
        if (!mybatisPlusPage.orders().isEmpty()) {
            Sort.Order[] orders = mybatisPlusPage.orders().stream()
                .map(order -> new Sort.Order(Sort.Direction.fromString(order.isAsc() ? "ASC" : "DESC"), order.getColumn()))
                .toArray(Sort.Order[]::new);
            sort = Sort.by(orders);
        }

        PageRequest pageRequest = PageRequest.of((int) mybatisPlusPage.getCurrent() - 1, (int) mybatisPlusPage.getSize(), sort);
        return new PageImpl<>(mybatisPlusPage.getRecords(), pageRequest, mybatisPlusPage.getTotal());
    }

}
