package uk.co.sundroid.util.async

import android.os.AsyncTask

class Async<O>(
        private val inBackground: () -> O,
        private val onDone: ((O) -> Unit)? = null,
        private val onFail: ((Exception) -> Unit)? = null) : AsyncTask<Void, Void, O>() {

    private var ex: Exception? = null

    override fun doInBackground(vararg params: Void): O? {
        return try {
            inBackground.invoke()
        } catch (ex: Exception) {
            this.ex = ex
            null
        }
    }

    override fun onPostExecute(result: O?) {
        this.ex?.let {
            onFail?.invoke(it)
        } ?: result?.let {
            // TODO May not invoke for expected null
            onDone?.invoke(it)
        }
    }

}
