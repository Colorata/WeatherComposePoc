package model.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

sealed class Result<T> {
    class Loading<T> : Result<T>()
    class Success<T>(val value: T) : Result<T>()
    class Failure<T>(val throwable: Throwable) : Result<T>()
}

fun <T, R> Result<T>.asOther(converter: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(converter(value))
        is Result.Loading -> Result.Loading()
        is Result.Failure -> Result.Failure(throwable)
    }
}

fun <T> Flow<Result<T>>.onSuccess(block: suspend (T) -> Unit) = onEach {
    if (it is Result.Success) block(it.value)
}

fun <T> Flow<Result<T>>.onLoading(block: suspend () -> Unit) = onEach {
    if (it is Result.Loading) block()
}

fun <T> Flow<Result<T>>.onFailure(block: suspend (Throwable) -> Unit) = onEach {
    if (it is Result.Failure) block(it.throwable)
}

