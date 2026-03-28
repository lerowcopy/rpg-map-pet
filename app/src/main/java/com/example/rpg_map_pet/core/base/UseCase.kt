package com.example.rpg_map_pet.core.base

import com.example.rpg_map_pet.core.result.Result

/**
 * Base interface for all use cases in the application.
 * A use case represents a single, specific business operation.
 */
interface UseCase {
    /**
     * Validates the input parameters before executing the use case.
     * Override this method if validation is needed.
     */
    fun validate(params: Any?): Boolean = true
}

/**
 * Base interface for use cases that return a Result wrapper.
 * @param T The type of the result data
 * @param P The type of the input parameters
 */
interface ResultUseCase<T, in P> : UseCase {
    suspend operator fun invoke(params: P): Result<T>
}

/**
 * Base interface for use cases that return a Result wrapper without parameters.
 * @param T The type of the result data
 */
interface ResultUseCaseNoParams<T> : UseCase {
    suspend operator fun invoke(): Result<T>
}

/**
 * Base interface for use cases that return Flow without parameters.
 * @param T The type of the emitted data
 */
interface FlowUseCase<T> : UseCase {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<T>
}

/**
 * Base interface for use cases that return Flow with parameters.
 * @param T The type of the emitted data
 * @param P The type of the input parameters
 */
interface FlowUseCaseWithParams<T, in P> : UseCase {
    operator fun invoke(params: P): kotlinx.coroutines.flow.Flow<T>
}
