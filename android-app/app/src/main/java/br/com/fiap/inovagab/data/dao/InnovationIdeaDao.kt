// data/dao/InnovationIdeaDao.kt
package br.com.fiap.inovagab.data.dao

import br.com.fiap.inovagab.data.model.InnovationIdea
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError   
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface InnovationIdeaDao {
    fun observeAllIdeas(): Flow<List<InnovationIdea>>
    suspend fun insertOrUpdateIdea(idea: InnovationIdea): Result<Unit>
    suspend fun deleteIdea(ideaId: String): Result<Unit>
}

class InnovationIdeaDaoImpl(
    private val dbRef: DatabaseReference // Aponta para o nó "ideas"
) : InnovationIdeaDao {

    override fun observeAllIdeas(): Flow<List<InnovationIdea>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ideas = snapshot.children.mapNotNull { child ->
                    child.getValue(InnovationIdea::class.java)
                }
                trySend(ideas)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    override suspend fun insertOrUpdateIdea(idea: InnovationIdea): Result<Unit> = try {
        dbRef.child(idea.id).setValue(idea).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteIdea(ideaId: String): Result<Unit> = try {
        dbRef.child(ideaId).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
