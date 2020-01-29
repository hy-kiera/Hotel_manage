package com.bambuser.examplebroadcaster;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.squareup.picasso.Picasso;


public class Fragment_player extends Fragment {
    private static String TAG = "Fragment_player";

    ViewPager viewPager;

    private ExamplePlayerActivity main_activity;
    private BroadcastPlayer mBroadcastPlayer;

    private TextView tv;
    private TextView liveTextView;
    private ImageView img_preview;

    private SurfaceViewWithAutoAR mVideoSurfaceView;
    public Fragment_player(){

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        tv = view.findViewById(R.id.PlaylistCount);
        liveTextView = view.findViewById(R.id.BroadcastLiveTextView);
        mVideoSurfaceView = view.findViewById(R.id.VideoSurfaceView);
        img_preview = view.findViewById(R.id.img_preview);
        return view;
    }

    public void setTempText(String str){
        tv.setText(str);
    }

    public SurfaceViewWithAutoAR getVideoSurfaceView(){
        return mVideoSurfaceView;
    }

    public void showPreview(){
        img_preview.setVisibility(View.VISIBLE);
    }

    public void playStart(String resourceUri,String id,final String previewUri){
        Picasso.with(getActivity()).load(previewUri).into(img_preview);
        img_preview.setVisibility(View.VISIBLE);
        if (mBroadcastPlayer != null)
            mBroadcastPlayer.close();


        mBroadcastPlayer = null;
        mBroadcastPlayer = new BroadcastPlayer(getContext(), resourceUri, id, mPlayerObserver);

        mBroadcastPlayer.setSurfaceView(getVideoSurfaceView());
        mBroadcastPlayer.setAcceptType(BroadcastPlayer.AcceptType.ANY);
        mBroadcastPlayer.setViewerCountObserver(mViewerCountObserver);

        mBroadcastPlayer.load();
    }

    public ExamplePlayerActivity getMain_activity(){
        return (ExamplePlayerActivity) getActivity();
    }

    public TextView getLiveTextView(){
        return liveTextView;
    }

    private final BroadcastPlayer.ViewerCountObserver mViewerCountObserver = new BroadcastPlayer.ViewerCountObserver() {
        @Override
        public void onCurrentViewersUpdated(long viewers) {
            Log.d(TAG,"ViewerCountObserver " + viewers);
        }
        @Override
        public void onTotalViewersUpdated(long viewers) {
        }
    };

    private final BroadcastPlayer.Observer mPlayerObserver = new BroadcastPlayer.Observer() {
        @Override
        public void onStateChange(PlayerState state) {
            Log.d(TAG,"state : " + state );
            boolean isPlayingLive = mBroadcastPlayer != null && mBroadcastPlayer.isTypeLive() && mBroadcastPlayer.isPlaying();
            TextView tvlive = getLiveTextView();
            if (tvlive != null && getMain_activity() != null) {

                if(isPlayingLive) {

                    getMain_activity().mRl_Live.setVisibility(View.VISIBLE);
                }else {
                    if(getMain_activity().isChatGone)
                        getMain_activity().mRl_Live.setVisibility(View.GONE);
                }

                tvlive.setVisibility(isPlayingLive ? View.VISIBLE : View.GONE);
            }
            if (state == PlayerState.PLAYING || state == PlayerState.PAUSED || state == PlayerState.COMPLETED) {
                if(state == PlayerState.PLAYING)
                    img_preview.setVisibility(View.GONE);
                if(state == PlayerState.COMPLETED)
                    img_preview.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onBroadcastLoaded(boolean live, int width, int height) {
            TextView tvlive = getLiveTextView();
            if (tvlive != null)
                tvlive.setVisibility(live ? View.VISIBLE : View.GONE);
        }


    };
}