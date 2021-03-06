package com.tainzhi.android.videoplayer.ui.movie

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.orhanobut.logger.Logger
import com.tainzhi.android.common.base.ui.LazyLoad
import com.tainzhi.android.common.base.ui.fragment.BaseVmBindingFragment
import com.tainzhi.android.videoplayer.R
import com.tainzhi.android.videoplayer.adapter.MovieChannelAdapter
import com.tainzhi.android.videoplayer.adapter.MovieChannelAdapterDecoration
import com.tainzhi.android.videoplayer.databinding.MovieChannelFragmentBinding
import com.tainzhi.android.videoplayer.network.ResultOf
import com.tainzhi.android.videoplayer.widget.CustomLoadMoreView
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MovieChannelFragment : BaseVmBindingFragment<MovieViewModel, MovieChannelFragmentBinding>(), LazyLoad {

    companion object {
        private const val CHANNEL_ID = "channel_id"
        fun newInstance(channelId: String): MovieChannelFragment {
            return MovieChannelFragment().apply {
                arguments = Bundle().apply {
                    putString(CHANNEL_ID, channelId)
                }
            }
        }
    }

    private var page = 1
    private val channelId by lazy { arguments?.getString(CHANNEL_ID) }
    private val movieChannelAdapter by lazy {
        MovieChannelAdapter { movie ->
            val bundle = bundleOf(MovieDetailFragment.MOVIE_ID to movie.id)
            findNavController().navigate(R.id.action_movieFragment_to_movieDetailFragment, bundle)

        }.apply {
            loadMoreModule.run {
                loadMoreView = CustomLoadMoreView()
                setOnLoadMoreListener {
                    loadMore()
                }
            }
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.movie_channel_fragment
    }

    override fun initData() {
        if (channelId != null) {
            refresh()
        } else {
            Logger.e("channelId is null")
        }
    }

    override fun initView() {
        with(mBinding) {
            movieChannelRefreshLayout.run {
                setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.color_secondary))
                setOnRefreshListener { refresh() }
            }
            movieChannelRecyclerView.run {
                adapter = movieChannelAdapter
                addItemDecoration(MovieChannelAdapterDecoration())
                layoutManager = GridLayoutManager(requireContext(), 3).apply {
                    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            // loadMoreView 横跨3列
                            if (movieChannelAdapter.getItemViewType(position) == BaseQuickAdapter.LOAD_MORE_VIEW)
                                return 3
                            else return 1
                        }
                    }

                }
            }
        }
    }

    override fun initVM(): MovieViewModel = getViewModel()

    override fun startObserve() {
        mViewModel.channelListLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultOf.Loading -> {
                    mBinding.movieChannelRefreshLayout.isRefreshing = true
                }
                is ResultOf.Success -> {
                    mBinding.movieChannelRefreshLayout.isRefreshing = false
                    movieChannelAdapter.run {
                        if (page == 1) setList(result.data)
                        else addData(result.data)
                        loadMoreModule.run {
                            isEnableLoadMore = true
                            loadMoreComplete()
                        }
                    }
                }
                is ResultOf.SuccessEndData -> {
                    mBinding.movieChannelRefreshLayout.isRefreshing = false
                    movieChannelAdapter.run {
                        addData(result.data)
                        loadMoreModule.loadMoreEnd()
                    }
                }
                is ResultOf.Error -> {
                    mBinding.movieChannelRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun loadMore() {
        page++
        mViewModel.getChannelData(channelId!!, page)
    }

    private fun refresh() {
        page = 1
        mViewModel.getChannelData(channelId!!, page)
    }
}
