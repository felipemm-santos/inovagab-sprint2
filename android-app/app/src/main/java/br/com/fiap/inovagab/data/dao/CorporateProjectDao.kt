// data/dao/CorporateProjectDao.kt
package br.com.fiap.inovagab.data.dao

import br.com.fiap.inovagab.data.model.CorporateProject
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface CorporateProjectDao {
    fun observeAllProjects(): Flow<List<CorporateProject>>
    suspend fun insertOrUpdateProject(project: CorporateProject): Result<Unit>
    suspend fun deleteProject(projectId: String): Result<Unit>
}

class CorporateProjectDaoImpl(
    private val dbRef: DatabaseReference // Aponta para o nó "projects"
) : CorporateProjectDao {

    override fun observeAllProjects(): Flow<List<CorporateProject>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val projects = snapshot.children.mapNotNull { child ->
                    child.getValue(CorporateProject::class.java)
                }
                trySend(projects)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    override suspend fun insertOrUpdateProject(project: CorporateProject): Result<Unit> = try {
        dbRef.child(project.id).setValue(project).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> = try {
        dbRef.child(projectId).removeValue().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
