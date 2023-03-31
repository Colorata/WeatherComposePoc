package model.core

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