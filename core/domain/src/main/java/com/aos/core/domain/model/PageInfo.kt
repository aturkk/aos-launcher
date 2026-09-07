package com.aos.core.domain.model

data class PageInfo(
    val pageId: Long = 0L,
    val pageIndex: Int = 0,
    val isHomePage: Boolean = false
)
