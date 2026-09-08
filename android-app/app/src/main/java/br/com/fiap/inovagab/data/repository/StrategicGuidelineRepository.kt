package br.com.fiap.inovagab.data.repository

import br.com.fiap.inovagab.data.model.StrategicGuideline
import br.com.fiap.inovagab.data.dao.StrategicGuidelineDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface StrategicGuidelineRepository {
    fun getGuidelines(): Flow<List<StrategicGuideline>>
    suspend fun saveGuideline(guideline: StrategicGuideline): Result<Unit>
    suspend fun removeGuideline(guidelineId: String): Result<Unit>
}

class StrategicGuidelineRepositoryImpl(
    private val guidelineDao: StrategicGuidelineDao
) : StrategicGuidelineRepository {

    override fun getGuidelines(): Flow<List<StrategicGuideline>> {
        return guidelineDao.observeAllGuidelines().flowOn(Dispatchers.IO)
    }

    override suspend fun saveGuideline(guideline: StrategicGuideline): Result<Unit> = withContext(Dispatchers.IO) {
        guidelineDao.insertOrUpdateGuideline(guideline)
    }

    override suspend fun removeGuideline(guidelineId: String): Result<Unit> = withContext(Dispatchers.IO) {
        guidelineDao.deleteGuideline(guidelineId)
    }
}