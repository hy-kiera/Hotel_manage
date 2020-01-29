package com.bambuser.examplebroadcaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.BroadcastStatus;
import com.bambuser.broadcaster.Broadcaster;
import com.bambuser.broadcaster.CameraError;
import com.bambuser.broadcaster.ConnectionError;
import com.bambuser.broadcaster.PlayerState;
import com.bambuser.broadcaster.SurfaceViewWithAutoAR;
import com.bambuser.broadcaster.TalkbackState;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.OpenChannelListQuery;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ExamplePlayerBack2Activity extends Activity implements Broadcaster.TalkbackObserver,Broadcaster.Observer,Broadcaster.UplinkSpeedObserver{

	private static final String APPLICATION_ID = "2SL5fqdb18G67ZkyaVpjLQ";
	private static final String API_KEY = "dugsz9kn1uc04nhls0s3y23eb";


//	private static final String APPLICATION_ID = "Cf8bhZ4bMl3rxv4R2D4g0g";
//	private static final String API_KEY = "bilg4sjnieo7t631swk2j95m6";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_OPEN_CHAT";
	private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_OPEN_CHAT";

	private static final int TALKBACK_DIALOG = 1;
	private static final String TALKBACK_DIALOG_CALLER = "caller";
	private static final String TALKBACK_DIALOG_REQUEST = "request";
	private static final String TALKBACK_DIALOG_SESSION_ID = "session_id";

	private static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23";
	private OpenChannel mChannel;
	private String mChannelUrl;

	private static final int CHANNEL_LIST_LIMIT = 30;

	private EditText mMessageEditText;
	private Button mMessageSendButton;
	private InputMethodManager mIMM;

	private PopupWindow mPopupWindow ;


	private boolean isChatGone = true;

	private static String TAG = "ExamplePlayerActivity";



	private final Handler mMainHandler = new Handler();
	private final OkHttpClient mOkHttpClient = new OkHttpClient();


	//	private ArrayList<BroadcastPlayer> mAryBroadcastPlayer = new ArrayList<>();
	private ArrayList<View> mAryView = new ArrayList<>();
	private ArrayList<SurfaceViewWithAutoAR> mAryVideoSurfaceView = new ArrayList<>();
	private ArrayList<BroadcastPlayer> mAryBroadcastPlayer = new ArrayList<>();

	private Broadcaster mBroadcaster;
	private BroadcastPlayer mBroadcastPlayer = null;
//	private MediaController mMediaController = null;
    private ExampleChatController mExampleChatController;

	private ArrayList<String> mPlayList = new ArrayList<>();
	private ArrayList<String> mPlaypreviewList = new ArrayList<>();

	private int nowposition = 0;

	private int playcount = 0;

	private Context context;

	private CustomViewPager viewPager ;
	private ViewPagerAdapter pagerAdapter ;

	private View mRl_Live;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.main_layout);
//		setContentView(R.layout.activity_example_player);

		init();
//		init2();
	}

	private void init(){

		mRl_Live = findViewById(R.id.rl_Live);

		if(isChatGone)
			mRl_Live.setVisibility(View.GONE);

		mAryVideoSurfaceView.add(null);
		mAryVideoSurfaceView.add(null);
		mAryVideoSurfaceView.add(null);
		mAryView.add(null);
		mAryView.add(null);
		mAryView.add(null);

		mBroadcaster = new Broadcaster(this, APPLICATION_ID, this);
		mBroadcaster.setRotation(getWindowManager().getDefaultDisplay().getRotation());
		mBroadcaster.setTalkbackObserver(this);
		mBroadcaster.setUplinkSpeedObserver(this);


		mExampleChatController = new ExampleChatController(this, (ListView) findViewById(R.id.ChatListView), R.layout.chatline, R.id.chat_line_textview, R.id.chat_line_timeview);
		mExampleChatController.show();
		mExampleChatController.add("test");
		viewPager = (CustomViewPager) findViewById(R.id.viewPager) ;


		pagerAdapter = new ViewPagerAdapter(this) ;
		viewPager.setAdapter(pagerAdapter) ;
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				Log.d(TAG,"onPageSelected " + i);
				nowposition = i;
				if(mPlayList.size() > i)
					playStart(i,mPlayList.get(i));

			}

			@Override
			public void onPageScrollStateChanged(int i) {
				Log.d(TAG,"onPageScrollStateChanged " + i);
			}
		});

		//SendBird

		mIMM = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

		SendBird.init(APP_ID, getApplicationContext());
		disconnect();








		// Set up chat box
		mMessageSendButton = (Button) findViewById(R.id.button_open_channel_chat_send);
		mMessageEditText = (EditText) findViewById(R.id.edittext_chat_message);

		mMessageSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendUserMessage(mMessageEditText.getText().toString());
				mExampleChatController.add(mMessageEditText.getText().toString());
				mMessageEditText.setText("");
				mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
			}
		});

//		((Button) findViewById(R.id.button_edit_popup)).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				openEditPopup();
////				Intent intent = new Intent(context, EditPopupActivity.class);
////				startActivity(intent);
//			}
//		});
	}


	private void disconnect() {
		SendBird.unregisterPushTokenAllForCurrentUser(new SendBird.UnregisterPushTokenHandler() {
			@Override
			public void onUnregistered(SendBirdException e) {
				if (e != null) {
					// Error!
					e.printStackTrace();

					// Don't return because we still need to disconnect.
				} else {
//                    Toast.makeText(MainActivity.this, "All push tokens unregistered.", Toast.LENGTH_SHORT).show();
				}

				ConnectionManager.logout(new SendBird.DisconnectHandler() {
					@Override
					public void onDisconnected() {
						try {
							PreferenceUtils.setConnected(false);
						}catch (Exception e){
							e.printStackTrace();
						}
						SendBird.connect("testdr", new SendBird.ConnectHandler() {
							@Override
							public void onConnected(User user, SendBirdException e) {
								Log.d(TAG,"connect : " );
								if (e != null) {    // Error.
									Log.d(TAG,"connect : 1" );

									return;
								}else
									createChannel();

							}
						});
						Log.d(TAG,"connect : onDisconnected : " );
					}
				});
			}
		});
	}

	private void openEditPopup(){
		View popupView = getLayoutInflater().inflate(R.layout.popup_edit_layout, null);
		mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		//popupView 에서 (LinearLayout 을 사용) 레이아웃이 둘러싸고 있는 컨텐츠의 크기 만큼 팝업 크기를 지정

		mPopupWindow.setFocusable(true);
		// 외부 영역 선택히 PopUp 종료

		mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
	}

	private void createChannel() {
		onResume();
		Log.d(TAG,"connect : createChannel1 : " );
		createChannel2("22944be6-7005-5d5d-47be-ae1d2ab61900");
//		OpenChannel.createChannel(new OpenChannel.OpenChannelCreateHandler() {
//			@Override
//			public void onResult(OpenChannel openChannel, SendBirdException e) {
//				Log.d(TAG,"connect : createChannel2 : " + openChannel.getUrl());
//
//				if (e != null) {    // Error.
//					return;
//				}
//				else
//					getChannel();
//			}
//		});
	}
	private void createChannel2(String name) {
		OpenChannelListQuery channelListQuery = OpenChannel.createOpenChannelListQuery();
		channelListQuery.setNameKeyword(name);

		channelListQuery.next(new OpenChannelListQuery.OpenChannelListQueryResultHandler() {
			@Override
			public void onResult(List<OpenChannel> openChannels, SendBirdException e) {
				Log.d(TAG,"connect : createChannel2 size : " + openChannels.size());
				if(openChannels.size() > 0){
					Log.d(TAG,"connect : createChannel2 : " + openChannels.get(0).getUrl());
					getChannel(openChannels.get(0).getUrl());
				}else{
					createChannel3("22944be6-7005-5d5d-47be-ae1d2ab61900");
				}
				for(int i = 0;i < openChannels.size();i++)
					Log.d(TAG,"connect : createChannel2 : " + openChannels.get(i).getUrl());
				if (e != null) {    // Error.
					return;
				}


				// A list of open channels that have "SendBird" in their names is returned.
			}
		});
	}
	private void createChannel3(String name) {
		Log.d(TAG,"connect : createChannel3 size : " + name);
		OpenChannel.createChannel(name, "", "", "", null, new OpenChannel.OpenChannelCreateHandler() {
			@Override
			public void onResult(OpenChannel openChannel, SendBirdException e) {
				if (e != null) {    // Error.
					return;
				}
				Log.d(TAG,"connect : createChannel3 size : " + openChannel.getUrl());
				getChannel(openChannel.getUrl());
			}
		});
	}

	private void getChannel(String channelUrl){

		mChannelUrl = channelUrl;
		OpenChannel.getChannel(mChannelUrl, new OpenChannel.OpenChannelGetHandler() {
			@Override
			public void onResult(final OpenChannel openChannel, SendBirdException e) {
				Log.d(TAG,"connect : getChannel 1 : " + mChannelUrl);
				if (e != null) {    // Error.
					return;
				}
				Log.d(TAG,"connect : getChannel 2 : " + mChannelUrl);
				openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
					@Override
					public void onResult(SendBirdException e) {
						Log.d(TAG,"connect : getChannel 3 : " + mChannelUrl);
						if (e != null) {    // Error.
							return;
						}
						openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
							@Override
							public void onResult(SendBirdException e) {
								if (e != null) {
									// Error!
									e.printStackTrace();
									return;
								}

								mChannel = openChannel;


							}
						});
					}
				});
			}
		});
	}

	private void sendUserMessage(String text) {
		mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
			@Override
			public void onSent(UserMessage userMessage, SendBirdException e) {
				if (e != null) {
					// Error!

					Toast.makeText(
							context,
							"Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
							.show();
					return;
				}

				// Display sent message to RecyclerView
//				mChatAdapter.addFirst(userMessage);
			}
		});
	}

	@Override
	public void onTalkbackStateChanged(final TalkbackState state, final int id, final String caller, final String request) {
		Log.d(TAG,"onTalkbackStateChanged");
		try {
			removeDialog(TALKBACK_DIALOG);
		} catch (Exception ignored) {
		}
		switch (state) {
			case IDLE:
//				mTalkbackStopButton.setVisibility(View.GONE);
//				mTalkbackStatus.setText("");
				break;
			case NEEDS_ACCEPT:
//				mTalkbackStatus.setText("talkback pending");
				Bundle args = new Bundle();
				args.putInt(TALKBACK_DIALOG_SESSION_ID, id);
				args.putString(TALKBACK_DIALOG_CALLER, caller);
				args.putString(TALKBACK_DIALOG_REQUEST, request);
				showDialog(TALKBACK_DIALOG, args);
				break;
			case ACCEPTED:
			case READY:

//				mTalkbackStopButton.setVisibility(View.VISIBLE);
//				mTalkbackStatus.setText("talkback connecting");
				break;
			case PLAYING:
//				mTalkbackStatus.setText("talkback active");
				break;
		}
	}

	@Override
	public void onConnectionStatusChange(BroadcastStatus broadcastStatus) {

	}

	@Override
	public void onStreamHealthUpdate(int i) {

	}

	@Override
	public void onConnectionError(ConnectionError connectionError, String s) {

	}

	@Override
	public void onCameraError(CameraError cameraError) {

	}

	@Override
	public void onChatMessage(String message) {
		Log.d(TAG,"onChatMessage " + message);
		if (mExampleChatController != null) {
			mExampleChatController.add(message);
			mExampleChatController.show();
		}
	}

	@Override
	public void onResolutionsScanned() {

	}

	@Override
	public void onCameraPreviewStateChanged() {

	}

	@Override
	public void onBroadcastInfoAvailable(String s, String s1) {

	}

	@Override
	public void onBroadcastIdAvailable(String s) {

	}

	@Override
	public void onUplinkTestComplete(long l, boolean b) {

	}

	public class ViewPagerAdapter extends PagerAdapter {

		// LayoutInflater 서비스 사용을 위한 Context 참조 저장.
		private Context mContext = null ;

		public ViewPagerAdapter() {

		}

		// Context를 전달받아 mContext에 저장하는 생성자 추가.
		public ViewPagerAdapter(Context context) {
			mContext = context ;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d(TAG,"ViewPagerAdapter instantiateItem "+ position);
			View view = null ;


			if (mContext != null) {
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.fragment_player, container, false);
				mAryView.add(position%3,view);
				mAryVideoSurfaceView.add(position%3,(SurfaceViewWithAutoAR) mAryView.get(position%3).findViewById(R.id.VideoSurfaceView));
			}



			// 뷰페이저에 추가.
			container.addView(mAryView.get(position%3)) ;

			return view ;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d(TAG,"ViewPagerAdapter destroyItem "+ position);
			// 뷰페이저에서 삭제.
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			// 전체 페이지 수는 10개로 고정.
			return 10;
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			Log.d(TAG,"ViewPagerAdapter isViewFromObject ");
			return (view == (View)object);
		}
	}

	private void init2(){
//		mVideoSurfaceView = findViewById(R.id.VideoSurfaceView);
	}

	private void refresh() {
		loadInitialMessageList(CHANNEL_LIST_LIMIT);
	}

	private void loadInitialMessageList(int numMessages) {

		PreviousMessageListQuery mPrevMessageListQuery = mChannel.createPreviousMessageListQuery();
		mPrevMessageListQuery.load(numMessages, true, new PreviousMessageListQuery.MessageListQueryResult() {
			@Override
			public void onResult(List<BaseMessage> list, SendBirdException e) {
				if (e != null) {
					// Error!
					e.printStackTrace();
					return;
				}

				//mChatAdapter.setMessageList(list);
			}
		});

	}

	private void refreshFirst() {
		getChannel(mChannelUrl);
	}

	private void setPreViewVisibility(int position,int show){
		//getPreView(position).setVisibility(show);
	}

	private ImageView getPreView(int position){
		return (ImageView) mAryView.get(position%3).findViewById(R.id.img_preview);
	}

	private void setImgUrlInPreview(int position){
		Picasso.with(context).load(mPlaypreviewList.get(position)).into((getPreView(position)));
	}




	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"connect : onResume 1");
//		mVideoSurfaceView = findViewById(R.id.VideoSurfaceView);
		mBroadcaster.onActivityResume();
		getLatestResourceUri();
		try {
			ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, new ConnectionManager.ConnectionManagementHandler() {
				@Override
				public void onConnected(boolean reconnect) {
					if (reconnect) {
						refresh();
					} else {
						refreshFirst();
					}
				}
			});
			SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
			SendBird.removeAllChannelHandlers();

			SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
				@Override
				public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
					Log.d(TAG, "connect : onMessageReceived 1" + mChannelUrl);
					// Add new message to view
					if (baseChannel.getUrl().equals(mChannelUrl)) {
						Log.d(TAG, "connect : onMessageReceived 2");
						mExampleChatController.add(((UserMessage) baseMessage).getMessage());
					}
				}

				@Override
				public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
					super.onMessageDeleted(baseChannel, msgId);
					if (baseChannel.getUrl().equals(mChannelUrl)) {

					}
				}

				@Override
				public void onMessageUpdated(BaseChannel channel, BaseMessage message) {
					super.onMessageUpdated(channel, message);
					if (channel.getUrl().equals(mChannelUrl)) {

					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mBroadcaster.onActivityPause();
//		mOkHttpClient.dispatcher().cancelAll();
//		setLatencyTimer(false);
//		if (mBroadcastPlayer != null)
//			mBroadcastPlayer.close();
//		mBroadcastPlayer = null;
//		mVideoSurfaceView = null;
//		if (mMediaController != null)
//			mMediaController.hide();
//		mMediaController = null;
//		if (mBroadcastLiveTextView != null)
//			mBroadcastLiveTextView.setVisibility(View.GONE);
		Log.d(TAG,"connect : onPause 1");
		ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
		SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		getMenuInflater().inflate(R.menu.main_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.about_menu_item:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		if (ev.getActionMasked() == MotionEvent.ACTION_UP && mBroadcastPlayer != null && mMediaController != null) {
//			PlayerState state = mBroadcastPlayer.getState();
//			if (state == PlayerState.PLAYING ||
//				state == PlayerState.BUFFERING ||
//				state == PlayerState.PAUSED ||
//				state == PlayerState.COMPLETED) {
//				if (mMediaController.isShowing())
//					mMediaController.hide();
//				else
//					mMediaController.show();
//			} else {
//				mMediaController.hide();
//			}
//		}
		return false;
	}

	private void getLatestResourceUri() {
		Request request = new Request.Builder()
			.url("https://api.bambuser.com/broadcasts")
			.addHeader("Accept", "application/vnd.bambuser.v1+json")
			.addHeader("Content-Type", "application/json")
			.addHeader("Authorization", "Bearer " + API_KEY)
			.get()
			.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(final Call call, final IOException e) {
				runOnUiThread(new Runnable() { @Override public void run() {

				}});
			}
			@Override
			public void onResponse(final Call call, final Response response) throws IOException {
				String resourceUri = null;
				String body= null;
				try {
					body = response.body().string();
					JSONObject json = new JSONObject(body);
					JSONArray results = json.getJSONArray("results");
					JSONObject latestBroadcast = results.optJSONObject(0);
					resourceUri = latestBroadcast.optString("resourceUri");
				} catch (Exception ignored) {}
				final String uri = resourceUri;
				final String allbody = body;
				runOnUiThread(new Runnable() { @Override public void run() {
					initPlayer(uri,allbody);
				}});
			}
		});
	}

	private void initPlayer(String resourceUri,String body) {
		Log.d(TAG,"initPlayer resourceUri : " + resourceUri);
		Log.d(TAG,"initPlayer body : " + body);
		if(!body.isEmpty()){
			try {
				mPlayList.clear();
				mPlaypreviewList.clear();
				JSONObject json = new JSONObject(body);
				JSONArray results = json.getJSONArray("results");
				for(int i = 0 ; i < results.length();i++){
					JSONObject latestBroadcast = results.optJSONObject(i);
					resourceUri = latestBroadcast.optString("resourceUri");
					resourceUri = latestBroadcast.optString("resourceUri");
					Log.d(TAG,"initPlayer resourceUri : "+i+" : " + resourceUri);
					mPlayList.add(resourceUri);
					mPlaypreviewList.add(latestBroadcast.optString("preview"));
				}

			} catch (Exception ignored) {

			}

		}

		if (resourceUri == null) {
			return;
		}

		playStart(0,mPlayList.get(0));
	}

	private void playStart(final int position, final String resourceUri) {
		int avsSize = mAryVideoSurfaceView.size();


		Log.d(TAG,"playStart0 avsSize : "+ avsSize) ;


		Log.d(TAG,"playStart0 : "+ position) ;

		Log.d(TAG,"playStart1 : "+ position) ;
		if (mAryVideoSurfaceView.get(position%3) == null) {
			return;
		}
		Log.d(TAG,"playStart2 : "+ position) ;


		Log.d(TAG,"playStart3 : "+ position) ;
		setPreViewVisibility(position,View.VISIBLE);
		setImgUrlInPreview(position);
		SurfaceViewWithAutoAR videoSurfaceView = (SurfaceViewWithAutoAR) mAryVideoSurfaceView.get(position%3);

		if (mBroadcastPlayer != null)
			mBroadcastPlayer.close();

		Log.d(TAG,"playStart4 : "+ position) ;
		mBroadcastPlayer = null;
		mBroadcastPlayer = new BroadcastPlayer(this, resourceUri, APPLICATION_ID, mPlayerObserver);

		mBroadcastPlayer.setSurfaceView(videoSurfaceView);
		mBroadcastPlayer.setAcceptType(BroadcastPlayer.AcceptType.ANY);
		mBroadcastPlayer.setViewerCountObserver(mViewerCountObserver);
		Log.d(TAG,"playStart5 : "+ position);
		mBroadcastPlayer.load();
	}

	public static class AboutActivity extends Activity {
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			WebView webView = new WebView(this);
			webView.loadUrl("file:///android_asset/licenses.html");
			// WebViewClient necessary since Android N to handle links in the license document
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					return false;
				}
			});
			setContentView(webView);
		}
	}

	private void updateVolume(float progress) {
		// Output volume should optimally increase logarithmically, but Android media player APIs
		// respond linearly. Producing non-linear scaling between 0.0 and 1.0 by using x^4.
		// Not exactly logarithmic, but has the benefit of satisfying the end points exactly.
//		if (mBroadcastPlayer != null)
//			mBroadcastPlayer.setAudioVolume(progress * progress * progress * progress);
	}

	private final SeekBar.OnSeekBarChangeListener mVolumeSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			updateVolume(seekBar.getProgress() / (float) seekBar.getMax());
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	};

	private final BroadcastPlayer.Observer mPlayerObserver = new BroadcastPlayer.Observer() {
		@Override
		public void onStateChange(PlayerState state) {
			boolean isPlayingLive = mBroadcastPlayer != null && mBroadcastPlayer.isTypeLive() && mBroadcastPlayer.isPlaying();
			TextView tvlive = mAryView.get(nowposition%3).findViewById(R.id.BroadcastLiveTextView);
			if (tvlive != null) {

				if(isPlayingLive) {
					mRl_Live.setVisibility(View.VISIBLE);
					mBroadcaster.startBroadcast();
				}else {
					if(isChatGone)
						mRl_Live.setVisibility(View.GONE);

				}

				tvlive.setVisibility(isPlayingLive ? View.VISIBLE : View.GONE);
			}
//			updateLatencyView();
			Log.d(TAG,"state : " + state + "   nowposition : " + nowposition);
			if (state == PlayerState.PLAYING || state == PlayerState.PAUSED || state == PlayerState.COMPLETED) {
				if(state == PlayerState.PLAYING){
					setPreViewVisibility(nowposition,View.GONE);
				}else{
					setPreViewVisibility(nowposition,View.VISIBLE);
				}
//				if (mMediaController == null && mBroadcastPlayer != null && !mBroadcastPlayer.isTypeLive()) {
//					mMediaController = new MediaController(ExamplePlayerActivity.this);
//					mMediaController.setAnchorView(mVideoSurfaceView);
//					mMediaController.setMediaPlayer(mBroadcastPlayer);
//				}
//				if (mMediaController != null) {
//					mMediaController.setEnabled(true);
//					mMediaController.show();
//				}
			} else if (state == PlayerState.ERROR || state == PlayerState.CLOSED) {
				setPreViewVisibility(nowposition,View.VISIBLE);
//				if (mMediaController != null) {
//					mMediaController.setEnabled(false);
//					mMediaController.hide();
//				}
//				mMediaController = null;
			}
		}
		@Override
		public void onBroadcastLoaded(boolean live, int width, int height) {
			Log.d(TAG,"drdr onBroadcastLoaded");
			TextView tvlive = mAryView.get(nowposition%3).findViewById(R.id.BroadcastLiveTextView);
			if(live)
				mRl_Live.setVisibility(View.VISIBLE);
			else {
				if(isChatGone)
					mRl_Live.setVisibility(View.GONE);
			}

			if (tvlive != null)
				tvlive.setVisibility(live ? View.VISIBLE : View.GONE);
		}
	};

	private void updateLatencyView() {
//		if (mBroadcastLatencyTextView != null) {
//			LatencyMeasurement lm = mBroadcastPlayer != null ? mBroadcastPlayer.getEndToEndLatency() : null;
//			if (lm != null)
//				mBroadcastLatencyTextView.setText("Latency: " + (lm.latency / 1000.0) + " s");
//			mBroadcastLatencyTextView.setVisibility(lm != null ? View.VISIBLE : View.GONE);
//		}
	}

	private void setLatencyTimer(boolean enable) {
		mMainHandler.removeCallbacks(mLatencyUpdateRunnable);
		if (enable)
			mMainHandler.postDelayed(mLatencyUpdateRunnable, 1000);
	}

	private final Runnable mLatencyUpdateRunnable = new Runnable() {
		@Override
		public void run() {
			updateLatencyView();
			mMainHandler.postDelayed(this, 1000);
		}
	};

	private final BroadcastPlayer.ViewerCountObserver mViewerCountObserver = new BroadcastPlayer.ViewerCountObserver() {
		@Override
		public void onCurrentViewersUpdated(long viewers) {
			Log.d(TAG,"ViewerCountObserver " + viewers);
		}
		@Override
		public void onTotalViewersUpdated(long viewers) {
		}
	};

	@Override
	public void onDestroy() {
		Log.d(TAG,"connect : onDestroy " );
		if (mChannel != null) {
			ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
			SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);

			ConnectionManager.logout(new SendBird.DisconnectHandler() {
				@Override
				public void onDisconnected() {
					try {
						PreferenceUtils.setConnected(false);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
//			mChannel.exit(new OpenChannel.OpenChannelExitHandler() {
//				@Override
//				public void onResult(SendBirdException e) {
//					Log.d(TAG,"connect : onDestroy " + 1);
//					if (e != null) {
//						// Error!
//						e.printStackTrace();
//						return;
//
//					}
//					Log.d(TAG,"connect : onDestroy " + 2);
//				}
//			});
		}

		super.onDestroy();
	}

}
