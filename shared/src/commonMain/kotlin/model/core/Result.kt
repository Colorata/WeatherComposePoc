package model.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class Result<T> {
    object Loading : Result<Nothing>()
    class Success<T>(val value: T) : Result<T>() {
        override fun toString(): String {
            return "Result.Success[${value.toString()}]"
        }
    }
    class Failure<T>(val throwable: Throwable) : Result<T>() {
        override fun toString(): String {
            return "Result.Failure[$throwable]"
        }
    }
}

fun <T> successResult(value: T) = Result.Success(value)

@Suppress("UNCHECKED_CAST")
fun <T> loadingResult() = Result.Loading as Result<T>

fun <T> failureResult(throwable: Throwable) = Result.Failure<T>(throwable)

@OptIn(ExperimentalContracts::class)
fun <T> Result<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is Result.Success)
    }
    return this is Result.Success
}

@OptIn(ExperimentalContracts::class)
fun <T> Result<T>.isLoading(): Boolean {
    contract {
        returns(true) implies (this@isLoading is Result.Loading)
    }
    return this is Result.Loading
}

@OptIn(ExperimentalContracts::class)
fun <T> Result<T>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Result.Failure)
    }
    return this is Result.Failure
}

fun <T, R> Result<T>.asOther(converter: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> successResult(converter(value))
        is Result.Loading -> loadingResult()
        is Result.Failure -> failureResult(throwable)
    }
}

fun <T> Flow<Result<T>>.onSuccess(block: suspend (T) -> Unit) = onEach {
    if (it.isSuccess()) block(it.value)
}

fun <T> Flow<Result<T>>.onLoading(block: suspend () -> Unit) = onEach {
    if (it.isLoading()) block()
}

fun <T> Flow<Result<T>>.onFailure(block: suspend (Throwable) -> Unit) = onEach {
    if (it.isFailure()) block(it.throwable)
}

