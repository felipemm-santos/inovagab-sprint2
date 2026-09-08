package br.com.fiap.inovagab.data.repository

import br.com.fiap.inovagab.data.model.InnovationIdea
import br.com.fiap.inovagab.data.dao.InnovationIdeaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface InnovationIdeaRepository {
    fun getIdeas(): Flow<List<InnovationIdea>>
    suspend fun saveIdea(idea: InnovationIdea): Result<Unit>
    suspend fun removeIdea(ideaId: String): Result<Unit>
}

class InnovationIdeaRepositoryImpl(
    private val ideaDao: InnovationIdeaDao
) : InnovationIdeaRepository {

    // Redireciona a escuta em tempo real para a Thread de IO (segurança de UI)
    override fun getIdeas(): Flow<List<InnovationIdea>> {
        return ideaDao.observeAllIdeas().flowOn(Dispatchers.IO)
    }

    // Executa a escrita de forma assíncrona garantindo a thread correta
    override suspend fun saveIdea(idea: InnovationIdea): Result<Unit> = withContext(Dispatchers.IO) {
        ideaDao.insertOrUpdateIdea(idea)
    }

    override suspend fun removeIdea(ideaId: String): Result<Unit> = withContext(Dispatchers.IO) {
        ideaDao.deleteIdea(ideaId)
    }
}