package cz.fit.cvut.feature.init.domain

import cz.fit.cvut.core.common.utils.ResultWrapper

internal interface InitRepository {
    suspend fun initFetching(): ResultWrapper<Unit>
} 