package com.aos.core.domain.repository

import com.aos.core.domain.model.IconPack

interface IconPackRepository {
    suspend fun getInstalledIconPacks(): List<IconPack>
}
