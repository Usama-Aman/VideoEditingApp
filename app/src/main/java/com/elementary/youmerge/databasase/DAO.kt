package com.elementary.youmerge.databasase

import androidx.room.*

@Dao
interface DAO {

    /*Inserting all import paths */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveImportPaths(productsList: List<ImportFilesModel>)

    @Query("DELETE FROM ImportFilesModel WHERE id = :id")
    suspend fun deletePath(id: Int)

    @Query("UPDATE ImportFilesModel SET name = :name  WHERE id = :id")
    suspend fun updateName(name: String, id: Int)

//    @Query("UPDATE ImportFilesModel SET path = :path WHERE id = :id")
//    suspend fun updatePath(path: String, id: Int)

    /*Fetching all saved paths */
    @Query("SELECT * FROM ImportFilesModel")
    suspend fun fetchAllPaths(): List<ImportFilesModel>

}