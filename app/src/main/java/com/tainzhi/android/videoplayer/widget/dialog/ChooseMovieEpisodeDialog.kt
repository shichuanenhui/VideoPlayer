package com.tainzhi.android.videoplayer.widget.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tainzhi.android.videoplayer.R
import com.tainzhi.mediaspider.film.bean.Video

/**
 * File:     ChooseMovieEpisode
 * Author:   tainzhi
 * Created:  2020/12/27 19:24
 * Mail:     QFQ61@qq.com
 * Description: 选择电影集数
 */
class ChooseMovieEpisodeDialog(val episodes: List<Video>) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_movie_choose_episode, container)
        return view
    }

    // 回调 Episode到 MovieDetailActivity
    var episodeCallback: ((Video) -> Unit)? = null

    private val clickCallback: ((Video) -> Unit) = { video ->
        // 关闭dialog
        dismiss()
        episodeCallback?.invoke(video)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<RecyclerView>(R.id.episodeRecyclerView).run {
            adapter = ChooseMovieEpisodeAdapter(episodes as MutableList<Video>, clickCallback)
            layoutManager = GridLayoutManager(context, 3)
        }

    }
}

class ChooseMovieEpisodeAdapter(episodes: MutableList<Video>, click: ((Video) -> Unit)? = null) :
        BaseQuickAdapter<Video, BaseViewHolder>(R.layout.item_dialog_movie_choose_episode, episodes) {

    init {
        setOnItemClickListener { _, _, position ->
            click?.invoke(data[position])
        }
    }

    override fun convert(holder: BaseViewHolder, item: Video) {
        holder.getView<TextView>(R.id.episodeTv).text = item.name
    }

}