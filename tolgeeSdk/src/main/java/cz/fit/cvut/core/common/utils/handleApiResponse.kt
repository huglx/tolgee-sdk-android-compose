package cz.fit.cvut.core.common.utils

internal suspend fun <T> handleApiResponse(
    apiCall: suspend () -> ResultWrapper<T>,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    when (val result = apiCall()) {
        is ResultWrapper.Success -> {
            onSuccess(result.data)
        }
        is ResultWrapper.Error -> {
            onError(result.message)
        }
    }
}