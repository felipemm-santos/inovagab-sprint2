package br.com.fiap.inovagab.data.repository

import br.com.fiap.inovagab.data.model.CorporateProject
import br.com.fiap.inovagab.data.dao.CorporateProjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface CorporateProjectRepository {
    fun getProjects(): Flow<List<CorporateProject>>
    suspend fun saveProject(project: CorporateProject): Result<Unit>
    suspend fun removeProject(projectId: String): Result<Unit>
}

class CorporateProjectRepositoryImpl(
    private val projectDao: CorporateProjectDao
) : CorporateProjectRepository {

    override fun getProjects(): Flow<List<CorporateProject>> {
        return projectDao.observeAllProjects().flowOn(Dispatchers.IO)
    }

    override suspend fun saveProject(project: CorporateProject): Result<Unit> = withContext(Dispatchers.IO) {
        projectDao.insertOrUpdateProject(project)
    }

    override suspend fun removeProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        projectDao.deleteProject(projectId)
    }
}