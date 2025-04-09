package cz.fit.cvut.core.common.utils

internal sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : ResultWrapper<Nothing>()
}