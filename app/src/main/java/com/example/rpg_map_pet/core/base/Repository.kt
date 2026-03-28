package com.example.rpg_map_pet.core.base

/**
 * Base interface for all repositories in the application.
 * Repositories provide a clean API for data operations to the domain layer.
 */
interface Repository

/**
 * Base interface for repositories with CRUD operations.
 * @param T The domain model type
 * @param ID The type of the entity identifier
 */
interface BaseRepository<T, ID> : Repository {
    suspend fun getById(id: ID): T?
    suspend fun getAll(): List<T>
    suspend fun insert(item: T)
    suspend fun update(item: T)
    suspend fun delete(id: ID)
}
