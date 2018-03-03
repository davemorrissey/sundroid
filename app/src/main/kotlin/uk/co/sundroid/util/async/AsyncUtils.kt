package uk.co.sundroid.util.async

fun <O> async(inBackground: () -> O, onDone: ((O) -> Unit)? = null, onFail: ((Exception) -> Unit)? = null) {
    Async(inBackground = inBackground, onDone = onDone, onFail = onFail).execute()
}
