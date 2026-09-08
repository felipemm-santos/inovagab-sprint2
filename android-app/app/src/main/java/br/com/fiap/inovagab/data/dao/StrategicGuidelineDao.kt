// data/dao/StrategicGuidelineDao.kt
package br.com.fiap.inovagab.data.dao

import br.com.fiap.inovagab.data.model.StrategicGuideline
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface StrategicGuidelineDao {
    fun observeAllGuidelines(): Flow<List<StrategicGuideline>>
    suspend fun insertOrUpdateGuideline(guideline: StrategicGuideline): Result<Unit>
    suspend fun deleteGuideline(guidelineId: String): Result<Unit>
}

class StrategicGuidelineDaoImpl(
    private val dbRef: DatabaseReference // Aponta para o nó "guidelines"
) : StrategicGuidelineDao {

    override fun observeAllGuidelines(): Flow<List<StrategicGuideline>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val guidelines = snapshot.children.mapNotNull { child ->
                    child.getValue(StrategicGuideline::class.java)
                }
                trySend(guidelines)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    override suspend fun insertOrUpdateGuideline(guideline: StrategicGuideline): Result<Unit> = try {
        dbRef.child(guideline.id).setValue(guideline).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteGuideline(guidelineId: String): Result<Unit> = try {
        dbRef.child(guidelineId).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
