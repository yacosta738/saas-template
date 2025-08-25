package com.loomify.spring.boot.presentation.sort

import com.loomify.common.domain.presentation.sort.Direction
import com.loomify.common.domain.presentation.sort.Sort
import org.springframework.data.domain.Sort as SpringSort

fun Sort?.toSpringSort(): SpringSort {
    return this?.let { sort ->
        val orders = sort.orders.map { order ->
            when (order.direction) {
                Direction.ASC -> SpringSort.Order.asc(order.property)
                Direction.DESC -> SpringSort.Order.desc(order.property)
            }
        }
        SpringSort.by(orders)
    } ?: SpringSort.unsorted()
}
