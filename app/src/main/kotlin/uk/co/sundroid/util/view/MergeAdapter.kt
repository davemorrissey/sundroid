/*
Copyright (c) 2008-2009 CommonsWare, LLC
Portions (c) 2009 Google, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package uk.co.sundroid.util.view

import java.util.ArrayList

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListAdapter

/**
 * Adapter that merges multiple child adapters and views* into a single contiguous whole.
 *
 * Adapters used as pieces within MergeAdapter must have view type IDs monotonically increasing from 0.
 * Ideally, adapters also have distinct ranges for their row ids, as
 * returned by getItemId().
 */
/**
 * Stock constructor, simply chaining to the superclass.
 */
class MergeAdapter : BaseAdapter() {

    private val pieces = ArrayList<ListAdapter>()

    /**
     * Adds a new adapter to the roster of things to appear in the aggregate list.
     * @param adapter Source for row views for this section
     */
    fun addAdapter(adapter: ListAdapter) {
        pieces.add(adapter)
    }

    /**
     * Adds a new View to the roster of things to appear in the aggregate list.
     * @param view Single view to add
     */
    fun addView(view: View) {
        val list = ArrayList<View>(1)
        list.add(view)
        addViews(list)
    }

    /**
     * Adds a list of views to the roster of things to appear in the aggregate list.
     * @param views List of views to add
     */
    private fun addViews(views: List<View>) {
        pieces.add(SackOfViewsAdapter(views))
    }

    /**
     * Get the data item associated with the specified position in the data set.
     * @param position Position of the item whose data we want
     */
    override fun getItem(position: Int): Any? {
        var pos = position
        for (piece in pieces) {
            val size = piece.count
            if (pos < size) {
                return piece.getItem(pos)
            }
            pos -= size
        }
        return null
    }

    /**
     * How many items are in the data set represented by this Adapter.
     */
    override fun getCount(): Int {
        return pieces.sumBy { it.count }
    }

    /**
     * Returns the number of types of Views that will be created by getView().
     */
    override fun getViewTypeCount(): Int {
        return Math.max(1, pieces.sumBy { it.viewTypeCount })
    }

    /**
     * Get the type of View that will be created by getView() for the specified item.
     * @param position Position of the item whose data we want
     */
    override fun getItemViewType(position: Int): Int {
        var pos = position
        var typeOffset = 0
        var result = -1
        for (piece in pieces) {
            val size = piece.count
            if (pos < size) {
                result = typeOffset + piece.getItemViewType(pos)
                break
            }
            pos -= size
            typeOffset += piece.viewTypeCount
        }
        return result
    }

    /**
     * Are all items in this ListAdapter enabled? If yes it means all items are selectable and clickable.
     */
    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    /**
     * Returns true if the item at the specified position is not a separator.
     * @param position Position of the item whose data we want
     */
    override fun isEnabled(position: Int): Boolean {
        var pos = position
        for (piece in pieces) {
            val size = piece.count
            if (pos < size) {
                return piece.isEnabled(pos)
            }
            pos -= size
        }
        return false
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * @param position    Position of the item whose data we want
     * @param convertView View to recycle, if not null
     * @param parent      ViewGroup containing the returned View
     */
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View? {
        var pos = position
        for (piece in pieces) {
            val size = piece.count
            if (pos < size) {
                return piece.getView(pos, convertView, parent)
            }
            pos -= size
        }
        return null
    }

    /**
     * Get the row id associated with the specified position in the list.
     * @param position Position of the item whose data we want
     */
    override fun getItemId(position: Int): Long {
        var pos = position
        for (piece in pieces) {
            val size = piece.count
            if (pos < size) {
                return piece.getItemId(pos)
            }
            pos -= size
        }
        return -1
    }
}