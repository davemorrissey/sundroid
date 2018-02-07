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

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * Adapter that simply returns row views from a list.
 *
 * If you supply a size, you must implement newView(), to create a required view. The adapter will
 * then cache these views.
 *
 * If you supply a list of views in the constructor, that list will be used directly. If any elements
 * in the list are null, then newView() will be called just for those slots.
 *
 * Subclasses may also wish to override areAllItemsEnabled() (default: false) and isEnabled()
 * (default: false), if some of their rows should be selectable.
 *
 * It is assumed each view is unique, and therefore will not get recycled.
 *
 * Note that this adapter is not designed for long lists. It is more for screens that should behave
 * like a list. This is particularly useful if you combine this with other adapters (e.g., SectionedAdapter)
 * that might have an arbitrary number of rows, so it all appears seamless.
 *
 * Constructor wraps a supplied list of views. Subclasses must override newView() if any of
 * the elements in the list are null.
 */
class SackOfViewsAdapter(private var views: List<View>) : BaseAdapter() {

    /**
     * Get the data item associated with the specified position in the data set.
     * @param position Position of the item whose data we want
     */
    override fun getItem(position: Int): Any {
        return views[position]
    }

    /**
     * How many items are in the data set represented by this Adapter.
     */
    override fun getCount(): Int {
        return views.size
    }

    /**
     * Returns the number of types of Views that will be created by getView().
     */
    override fun getViewTypeCount(): Int {
        return count
    }

    /**
     * Get the type of View that will be created by getView() for the specified item.
     * @param position Position of the item whose data we want
    */
    override fun getItemViewType(position: Int): Int {
        return position
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
        return false
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * @param position Position of the item whose data we want
     * @param convertView View to recycle, if not null
     * @param parent ViewGroup containing the returned View
     */
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        return views[position]
    }

    /**
     * Get the row id associated with the specified position in the list.
     * @param position Position of the item whose data we want
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    
}
