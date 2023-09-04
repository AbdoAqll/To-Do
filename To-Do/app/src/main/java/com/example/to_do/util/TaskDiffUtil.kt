package com.example.to_do.util

import androidx.recyclerview.widget.DiffUtil
import com.example.to_do.data.domain.Task

class TaskDiffUtil(private val mOldList : List<Task>, private val mNewList: List<Task>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return mOldList.size
    }

    override fun getNewListSize(): Int {
        return mNewList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return(mOldList[oldItemPosition].title == mNewList[newItemPosition].title
                &&
                mOldList[oldItemPosition].description == mNewList[newItemPosition].description
                &&
                mOldList[oldItemPosition].completed == mNewList[newItemPosition].completed)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // in case of facebook if the likes changed then the content changed
        // so we will return false if the likes or comments changed
        return true
    }
}
