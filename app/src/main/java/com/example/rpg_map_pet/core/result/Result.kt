package com.example.rpg_map_pet.core.result

/**
 * Represents the result of an operation in the domain layer.
 * This is a sealed class that encapsulates either a success with data or an error.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(
        val exception: Throwable,
        val message: String? = exception.message,
        val code: Int? = null
    ) : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }

    fun onError(action: (Throwable, String?) -> Unit): Result<T> = apply {
        if (this is Error) action(exception, message)
    }

    fun onSuccess(action: (T) -> Unit): Result<T> = apply {
        if (this is Success) action(data)
    }

    fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Throwable, String?) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception, message)
    }

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Throwable, message: String? = exception.message): Result<Nothing> =
            Error(exception, message)
        fun <T> failure(message: String): Result<T> =
            Error(Exception(message), message)
    }
}

/**
 * Extension function to convert from kotlin.Result to domain Result
 */
fun <T> kotlin.Result<T>.toDomainResult(): Result<T> =
    fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.error(it, it.message) }
    )
