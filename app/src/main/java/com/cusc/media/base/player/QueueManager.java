package com.cusc.media.base.player;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class QueueManager {
    private static final String TAG = "QueueManager";
    private final List<MediaSessionCompat.QueueItem> mPlayingQueue = new ArrayList<>();
    private final MusicService mMusicService;

    private static final MediaSessionCompat.QueueItem TEST_QUEUE_ITEM;
    static {
        // 创建固定ID为0的插件信息
        MediaDescriptionCompat testDescription = new MediaDescriptionCompat.Builder()
                .setMediaId("0")
                .setTitle("PSA工具箱 多媒体插件")
                .setSubtitle("V1.0")
                .setIconUri(null)
                .build();
        TEST_QUEUE_ITEM = new MediaSessionCompat.QueueItem(testDescription, 0);
    }


    public QueueManager(MusicService musicService) {
        this.mMusicService = musicService;
    }

    public void updateCurrentSong(MediaBrowserCompat.MediaItem mediaItem) {
        if (mediaItem == null) {
            Log.w(TAG, "updateCurrentSong: media item is null");
            return;
        }

        // 1. 转换为 MediaSession.QueueItem（使用mediaId对应的时间戳作为queueId）
        List<MediaSessionCompat.QueueItem> queueItems = new ArrayList<>();

        queueItems.add(TEST_QUEUE_ITEM);

        String mediaId = mediaItem.getDescription().getMediaId();
        try {
            // 从mediaId解析时间戳作为queueId，实现两者一致
            long queueId = Long.parseLong(mediaId);
            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(
                    mediaItem.getDescription(),
                    queueId
            );
            queueItems.add(queueItem);
        } catch (NumberFormatException e) {
            Log.e(TAG, "mediaId is not a valid timestamp format: " + mediaId, e);
        }

        // 2. 设置队列
        setQueue(queueItems);

        Log.d(TAG, "updateCurrentSong: Update current song to - " + mediaItem.getDescription().getTitle());
    }

    private void setQueue(List<MediaSessionCompat.QueueItem> queueItems) {
        synchronized (mPlayingQueue) {
            mPlayingQueue.clear();
            mPlayingQueue.addAll(queueItems);
        }

        MediaSessionCompat mediaSession = mMusicService.getMediaSession();
        if (mediaSession != null) {
            mediaSession.setQueue(mPlayingQueue);
            Log.d(TAG, "Queue updated, current song count=" + queueItems.size());
        } else {
            Log.e(TAG, "setQueue: mediaSession is null, cannot update queue");
        }
    }
}