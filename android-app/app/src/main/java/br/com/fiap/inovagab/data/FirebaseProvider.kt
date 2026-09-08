// data/FirebaseProvider.kt
package br.com.fiap.inovagab.data

import br.com.fiap.inovagab.data.dao.*
import br.com.fiap.inovagab.data.repository.*
import com.google.firebase.database.FirebaseDatabase

object FirebaseProvider {

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance("https://inovagab-8af61-default-rtdb.firebaseio.com/")
    }

    val innovationIdeaRepository: InnovationIdeaRepository by lazy {
        InnovationIdeaRepositoryImpl(InnovationIdeaDaoImpl(database.getReference("ideas")))
    }

    val strategicGuidelineRepository: StrategicGuidelineRepository by lazy {
        StrategicGuidelineRepositoryImpl(StrategicGuidelineDaoImpl(database.getReference("guidelines")))
    }

    val corporateProjectRepository: CorporateProjectRepository by lazy {
        CorporateProjectRepositoryImpl(CorporateProjectDaoImpl(database.getReference("projects")))
    }
}